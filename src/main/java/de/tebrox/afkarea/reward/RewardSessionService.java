package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.AreaManager;
import de.tebrox.afkarea.bootstrap.AFKAreaPermissions;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.household.HouseholdManager;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import de.tebrox.afkarea.reward.data.RewardMilestoneData;
import de.tebrox.afkarea.session.CompletedSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public final class RewardSessionService {
    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final RewardService rewardService;

    private final Map<UUID, Session> sessions = new HashMap<>();

    private static final String IP_LIMIT_BYPASS_PERMISSION = AFKAreaPermissions.BYPASS_IP_LIMIT;
    private final Supplier<AFKAreaConfig> config;
    private final HouseholdManager householdManager;

    private BukkitTask task;

    public RewardSessionService(JavaPlugin plugin, AreaManager areaManager, RewardService rewardService, Supplier<AFKAreaConfig> config, HouseholdManager householdManager) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.rewardService = rewardService;
        this.config = config;
        this.householdManager = householdManager;
    }

    public void start() {
        if(task != null) return;

        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if(task != null) {
            task.cancel();
            task = null;
        }

        sessions.clear();
    }

    public void begin(Player player, AreaData area) {
        sessions.put(player.getUniqueId(), new Session(area.getUniqueId(), System.nanoTime(), System.currentTimeMillis()));
    }

    public void clear(UUID playerId) {
        sessions.remove(playerId);
    }

    public void clearAll() {
        sessions.clear();
    }

    public Optional<CompletedSession> finish(UUID playerId) {
        Session session = sessions.remove(playerId);
        if(session == null) return Optional.empty();

        return Optional.of(new CompletedSession(session.areaId, elapsedSeconds(session), session.startedAtEpochMillis, System.currentTimeMillis()));
    }

    private long elapsedSeconds(Session session) {
        return TimeUnit.NANOSECONDS.toSeconds(Math.max(0L, System.nanoTime() - session.startedAt));
    }


    private void tick() {
        long now = System.nanoTime();

        for(Map.Entry<UUID, Session> entry : new ArrayList<>(sessions.entrySet())) {
            UUID playerId = entry.getKey();
            Session session = entry.getValue();

            Player player = plugin.getServer().getPlayer(playerId);

            if(player == null || !player.isOnline()) {
                sessions.remove(playerId);
                continue;
            }

            AreaData area = areaManager.getArea(session.areaId);

            if(area == null) {
                sessions.remove(playerId);
                continue;
            }

            RewardConfigData config = area.getRewards();

            if(config == null) continue;

            String scheduleType = config.getScheduleType();
            if("interval".equalsIgnoreCase(scheduleType)) {
                tickInterval(player, area, config, session, now);
                continue;
            }

            if("milestones".equalsIgnoreCase(scheduleType)) {
                tickMilestones(player, area, config, session, now);
            }
        }
    }

    private void tickInterval(Player player, AreaData area, RewardConfigData config, Session session, long now) {
        int intervalSeconds = config.getIntervalSeconds();
        if(intervalSeconds <= 0) return;

        long intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds);
        long reference = session.lastRewardAt == 0L ? session.startedAt : session.lastRewardAt;

        if(now - reference < intervalNanos) return;
        session.lastRewardAt = now;
        if(canRewardByIpLimit(player)) rewardService.grant(player, area, config.getRolls());
    }

    private void tickMilestones(Player player, AreaData area, RewardConfigData config, Session session, long now) {
        List<RewardMilestoneData> milestones = config.getMilestones();
        if(milestones == null || milestones.isEmpty()) return;

        long elapsed = now - session.startedAt;
        for(int index = 0; index < milestones.size(); index++) {
            if(session.completedMilestones.contains(index)) continue;
            RewardMilestoneData milestone = milestones.get(index);

            if(milestone == null || milestone.getAfterSeconds() <= 0) {
                session.completedMilestones.add(index);
                continue;
            }

            long required = TimeUnit.SECONDS.toNanos(milestone.getAfterSeconds());
            if(elapsed < required) continue;

            session.completedMilestones.add(index);
            if(canRewardByIpLimit(player)) rewardService.grant(player, area, milestone.getRolls());

            if(sessions.get(player.getUniqueId()) != session) break;
        }
    }

    private boolean canRewardByIpLimit(Player player) {
        return getIpRewardStatus(player).allowed();
    }

    public IpRewardStatus getIpRewardStatus(Player player) {
        if(player.hasPermission(IP_LIMIT_BYPASS_PERMISSION)) return new IpRewardStatus(0, 0, true, IpRewardStatus.Mode.BYPASS);

        int limit = config.get().maxRewardingPlayersPerIp;
        if(limit == -1) return new IpRewardStatus(0, -1, true, IpRewardStatus.Mode.UNLIMITED);
        if(limit <= 0) return new IpRewardStatus(0, limit, false, IpRewardStatus.Mode.LIMITED);

        InetAddress address = addressOf(player);
        if(address == null) return new IpRewardStatus(0, limit, true, IpRewardStatus.Mode.ADDRESS_UNAVAILABLE);

        Map<String, Long> identities = new HashMap<>();
        for(Map.Entry<UUID, Session> entry : sessions.entrySet()) {
            UUID playerId = entry.getKey();
            Player other = player.getServer().getPlayer(playerId);

            if(other == null || !other.isOnline()) continue;
            if(other.hasPermission(IP_LIMIT_BYPASS_PERMISSION)) continue;

            InetAddress otherAddress = addressOf(other);
            if(!address.equals(otherAddress)) continue;

            AreaData otherArea = areaManager.getArea(entry.getValue().areaId);
            if(!rewardService.hasEligibleReward(other, otherArea)) continue;

            String identity = householdManager.householdIdentity(playerId);
            identities.merge(identity, entry.getValue().startedAt, Math::min);
        }

        List<Map.Entry<String, Long>> candidates = new ArrayList<>(identities.entrySet());
        candidates.sort(Map.Entry.<String, Long> comparingByValue().thenComparing(Map.Entry::getKey));

        String playerIdentity = householdManager.householdIdentity(player.getUniqueId());
        boolean allowed = false;

        for(int index = 0; index < candidates.size(); index++) {
            if(candidates.get(index).getKey().equalsIgnoreCase(playerIdentity)) {
                allowed = index < limit;
                break;
            }
        }

        return new IpRewardStatus(candidates.size(), limit, allowed, IpRewardStatus.Mode.LIMITED);
    }

    private InetAddress addressOf(Player player) {
        InetSocketAddress address = player.getAddress();

        return address == null ? null : address.getAddress();
    }

    public OptionalLong getSessionSeconds(UUID playerId) {
        Session session = sessions.get(playerId);
        if(session == null) return OptionalLong.empty();

        return OptionalLong.of(elapsedSeconds(session));
    }

    public OptionalLong getNextRewardSeconds(UUID playerId) {
        Optional<RewardProgress> progress = getRewardProgress(playerId);
        if(progress.isEmpty()) return OptionalLong.empty();

        return OptionalLong.of(progress.get().remainingSeconds);
    }

    public Optional<RewardProgress> getRewardProgress(UUID playerId) {
        Session session = sessions.get(playerId);
        if(session == null) return Optional.empty();

        AreaData area = areaManager.getArea(session.areaId);
        if(area == null || area.getRewards() == null) return Optional.empty();

        RewardConfigData rewardConfig = area.getRewards();
        long now = System.nanoTime();
        long sessionElapsedNanos = Math.max(0L, now - session.startedAt);
        long sessionSeconds = TimeUnit.NANOSECONDS.toSeconds(sessionElapsedNanos);

        if("interval".equalsIgnoreCase(rewardConfig.getScheduleType())) {
            return intervalProgress(session, rewardConfig, now, sessionSeconds);
        }

        if("milestones".equalsIgnoreCase(rewardConfig.getScheduleType())) {
            return milestoneProgress(session, rewardConfig, sessionElapsedNanos, sessionSeconds);
        }
        return Optional.empty();
    }

    private Optional<RewardProgress> intervalProgress(Session session, RewardConfigData config, long now, long sessionSeconds) {
        int intervalSeconds = config.getIntervalSeconds();
        if(intervalSeconds <= 0) return Optional.empty();

        long intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds);
        long reference = session.lastRewardAt == 0L ? session.startedAt : session.lastRewardAt;
        long elapsedInCycle = Math.max(0L, now - reference);
        long remainingNanos = Math.max(0L, intervalNanos - elapsedInCycle);
        double progress = clampProgress((double) elapsedInCycle / (double) intervalNanos);

        return Optional.of(new RewardProgress(sessionSeconds, TimeUnit.NANOSECONDS.toSeconds(remainingNanos), intervalSeconds, progress));
    }

    private Optional<RewardProgress> milestoneProgress(Session session, RewardConfigData config, long sessionElapsedNanos, long sessionSeconds) {
        List<RewardMilestoneData> milestones = config.getMilestones();
        if(milestones == null ||milestones.isEmpty()) return Optional.empty();

        long previousMilestoneNanos = 0L;
        long nextMilestoneNanos = Long.MAX_VALUE;

        for(int index = 0; index < milestones.size(); index++) {
            RewardMilestoneData milestone = milestones.get(index);
            if(milestone == null || milestone.getAfterSeconds() <= 0) continue;

            long requiredNanos = TimeUnit.SECONDS.toNanos(milestone.getAfterSeconds());
            if(session.completedMilestones.contains(index)) {
                previousMilestoneNanos = Math.max(previousMilestoneNanos, requiredNanos);
                continue;
            }

            nextMilestoneNanos = Math.min(nextMilestoneNanos, requiredNanos);
        }

        if(nextMilestoneNanos == Long.MAX_VALUE) return Optional.empty();

        long cycleNanos = Math.max(1L, nextMilestoneNanos - previousMilestoneNanos);
        long elapsedInCycle = Math.max(0L, sessionElapsedNanos - previousMilestoneNanos);
        long remainingNanos = Math.max(0L, nextMilestoneNanos - sessionElapsedNanos);
        double progress = clampProgress((double) elapsedInCycle / (double) cycleNanos);

        return Optional.of(new RewardProgress(sessionSeconds, TimeUnit.NANOSECONDS.toSeconds(remainingNanos), Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(cycleNanos)), progress));
    }

    private double clampProgress(double progress) {
        return Math.max(0.0D, Math.min(1.0D, progress));
    }

    private static final class Session {
        private final String areaId;
        private final long startedAt;
        private final long startedAtEpochMillis;
        private long lastRewardAt;

        private final Set<Integer> completedMilestones = new HashSet<>();

        private Session(String areaId, long startedAt, long startedAtEpochMillis) {
            this.areaId = areaId;
            this.startedAt = startedAt;
            this.startedAtEpochMillis = startedAtEpochMillis;
        }
    }

    public record IpRewardStatus(int activeIdentities, int limit, boolean allowed, Mode mode) {
        public enum Mode {
            LIMITED,
            UNLIMITED,
            BYPASS,
            ADDRESS_UNAVAILABLE
        }
    }

    public record RewardProgress(long sessionSeconds, long remainingSeconds, long cycleSeconds, double progress) {}
}
