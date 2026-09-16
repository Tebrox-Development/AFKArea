package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.area.AreaManager;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class RewardSessionService {
    private final JavaPlugin plugin;
    private final AreaManager areaManager;
    private final RewardService rewardService;

    private final Map<UUID, Session> sessions = new HashMap<>();

    private BukkitTask task;

    public RewardSessionService(JavaPlugin plugin, AreaManager areaManager, RewardService rewardService) {
        this.plugin = plugin;
        this.areaManager = areaManager;
        this.rewardService = rewardService;
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

            if(!"interval".equalsIgnoreCase(config.getScheduleType())) continue;

            int intervalSeconds = config.getIntervalSeconds();

            if(intervalSeconds <= 0) continue;

            long intervalNanos = TimeUnit.SECONDS.toNanos(intervalSeconds);
            long reference = session.lastRewardAt == 0L ? session.startedAt : session.lastRewardAt;

            if(now - reference <intervalNanos) continue;
            session.lastRewardAt = now;
            rewardService.grant(player, area, config.getRolls());
        }
    }

    private static final class Session {
        private final String areaId;
        private final long startedAt;
        private long lastRewardAt;

        private Session(String areaId, long startedAt) {
            this.areaId = areaId;
            this.startedAt = startedAt;
        }
    }
}
