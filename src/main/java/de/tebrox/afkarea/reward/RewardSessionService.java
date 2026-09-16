package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.AreaManager;
import de.tebrox.afkarea.config.AFKAreaConfig;
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

    private BukkitTask task;

    public RewardSessionService(JavaPlugin plugin, AreaManager areaManager, RewardService rewardService, Supplier<AFKAreaConfig> config) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.rewardService = rewardService;
        this.config = config;
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
        if(player.hasPermission(IP_LIMIT_BYPASS_PERMISSION)) return true;

        int limit = config.get().maxRewardingPlayersPerIp;

        if(limit == -1) return true;
        if(limit <= 0) return false;

        InetAddress address = addressOf(player);
        if(address == null) return true;

        List<Map.Entry<UUID, Session>> candidates = new ArrayList<>();
        for(Map.Entry<UUID, Session> entry : sessions.entrySet()) {
            Player other = plugin.getServer().getPlayer(entry.getKey());

            if(other == null || !other.isOnline()) continue;
            if(other.hasPermission(IP_LIMIT_BYPASS_PERMISSION)) continue;

            InetAddress otherAddress = addressOf(other);
            if(!address.equals(otherAddress)) continue;

            AreaData otherArea = areaManager.getArea(entry.getValue().areaId);
            if(!rewardService.hasEligibleReward(other, otherArea)) continue;

            candidates.add(entry);
        }

        candidates.sort(Comparator.<Map.Entry<UUID, Session>> comparingLong(entry -> entry.getValue().startedAt).thenComparing(entry -> entry.getKey().toString()));

        for(int index = 0; index < candidates.size(); index++) {
            if(candidates.get(index).getKey().equals(player.getUniqueId())) return index < limit;
        }

        return false;
    }

    private InetAddress addressOf(Player player) {
        InetSocketAddress address = player.getAddress();

        return address == null ? null : address.getAddress();
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
}
