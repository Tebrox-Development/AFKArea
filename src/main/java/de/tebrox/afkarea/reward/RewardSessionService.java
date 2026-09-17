package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.AreaManager;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.household.HouseholdManager;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import de.tebrox.afkarea.reward.data.RewardMilestoneData;
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

    private static final String IP_LIMIT_BYPASS_PERMISSION = "afkarea.bypass.ip-limit";
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
        sessions.put(player.getUniqueId(), new Session(area.getUniqueId(), System.nanoTime()));
    }

    public void clear(UUID playerId) {
        sessions.remove(playerId);
    }

    public void clearAll() {
        sessions.clear();
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

        long elapsedNanos = Math.max(0L, System.nanoTime() - session.startedAt);
        return OptionalLong.of(TimeUnit.NANOSECONDS.toSeconds(elapsedNanos));
    }

    public OptionalLong getNextRewardSeconds(UUID playerId) {
        Session session = sessions.get(playerId);
        if(session == null) return OptionalLong.empty();

        AreaData area = areaManager.getArea(session.areaId);
        if(area == null || area.getRewards() == null) return OptionalLong.empty();

        RewardConfigData rewardConfig = area.getRewards();
        long now = System.nanoTime();

        if("interval".equalsIgnoreCase(rewardConfig.getScheduleType())) {
            int intervalSeconds = rewardConfig.getIntervalSeconds();
            if(intervalSeconds <= 0) return OptionalLong.empty();

            long reference = session.lastRewardAt == 0L ? session.startedAt : session.lastRewardAt;
            long remaining = TimeUnit.SECONDS.toNanos(intervalSeconds) - (now - reference);

            return OptionalLong.of(Math.max(0L, TimeUnit.NANOSECONDS.toSeconds(remaining)));
        }

        if("milestones".equalsIgnoreCase(rewardConfig.getScheduleType())) {
            long elapsed = now - session.startedAt;
            long nearest = Long.MAX_VALUE;

            List<RewardMilestoneData> milestones = rewardConfig.getMilestones();

            for(int index = 0; index < milestones.size(); index++) {
                if(session.completedMilestones.contains(index)) continue;

                RewardMilestoneData milestone = milestones.get(index);
                if(milestone == null || milestone.getAfterSeconds() <= 0) continue;

                long remaining = TimeUnit.SECONDS.toNanos(milestone.getAfterSeconds()) - elapsed;
                nearest = Math.min(nearest, Math.max(0L, remaining));

                if(nearest == Long.MAX_VALUE) return OptionalLong.empty();

                return OptionalLong.of(TimeUnit.NANOSECONDS.toSeconds(nearest));
            }
        }
        return OptionalLong.empty();
    }

    private static final class Session {
        private final String areaId;
        private final long startedAt;
        private long lastRewardAt;

        private final Set<Integer> completedMilestones = new HashSet<>();

        private Session(String areaId, long startedAt) {
            this.areaId = areaId;
            this.startedAt = startedAt;
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
}
