package de.tebrox.afkarea.display;

import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.reward.RewardSessionService;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Supplier;

public final class BossBarDisplayService {
    private final JavaPlugin plugin;
    private final RewardSessionService rewardSessionService;
    private final Supplier<AFKAreaConfig> config;
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;

    private final Map<UUID, BossBar> bossBars = new HashMap<>();
    private BukkitTask task;

    public BossBarDisplayService(JavaPlugin plugin, RewardSessionService rewardSessionService, Supplier<AFKAreaConfig> config, Supplier<MessageConfig> messages, MessageService messageService) {
        this.plugin = plugin;
        this.rewardSessionService = rewardSessionService;
        this.config = config;
        this.messages = messages;
        this.messageService = messageService;
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
        hideAll();
    }

    public void refreshAll() {
        tick();
    }

    private void tick() {
        cleanupOfflinePlayers();

        AFKAreaConfig currentConfig = config.get();

        if(!currentConfig.bossBarEnabled) {
            hideAll();
            return;
        }

        for(Player player : plugin.getServer().getOnlinePlayers()) {
            Optional<RewardSessionService.RewardProgress> progress = rewardSessionService.getRewardProgress(player.getUniqueId());
            if(progress.isEmpty()) {
                hide(player);
                continue;
            }

            showOrUpdate(player, progress.get(), currentConfig);
        }
    }

    private void showOrUpdate(Player player, RewardSessionService.RewardProgress progress, AFKAreaConfig currentConfig) {
        Component title = messageService.parse(messages.get().bossBarText, Placeholder.unparsed("session", formatDuration(progress.sessionSeconds())), Placeholder.unparsed("next_reward", formatDuration(progress.remainingSeconds())), Placeholder.unparsed("progress_percent", Long.toString(Math.round(progress.progress() * 100.0D))));
        float barProgress = (float) Math.max(0.0D, Math.min(1.0D, progress.progress()));
        BossBar.Color color = parseColor(currentConfig.bossBarColor);
        BossBar.Overlay overlay = parseOverlay(currentConfig.bossBarStyle);
        BossBar bossBar = bossBars.get(player.getUniqueId());

        if(bossBar == null) {
            bossBar = BossBar.bossBar(title, barProgress, color, overlay);
            bossBars.put(player.getUniqueId(), bossBar);
            player.showBossBar(bossBar);

            return;
        }

        bossBar.name(title);
        bossBar.progress(barProgress);
        bossBar.color(color);
        bossBar.overlay(overlay);
    }

    private void hide(Player player) {
        BossBar bossBar = bossBars.remove(player.getUniqueId());
        if(bossBar != null) player.hideBossBar(bossBar);
    }

    private void hideAll() {
        for(Map.Entry<UUID, BossBar> entry : new ArrayList<>(bossBars.entrySet())) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if(player != null) {
                player.hideBossBar(entry.getValue());
            }
        }
        bossBars.clear();
    }

    private void cleanupOfflinePlayers() {
        bossBars.keySet().removeIf(playerId -> plugin.getServer().getPlayer(playerId) == null);
    }

    private BossBar.Color parseColor(String value) {
        if(value == null) return BossBar.Color.YELLOW;

        try {
            return BossBar.Color.valueOf(value.trim().toUpperCase(Locale.ROOT));
        }catch(IllegalArgumentException exception) {
            return BossBar.Color.YELLOW;
        }
    }

    private BossBar.Overlay parseOverlay(String value) {
        if(value == null) return BossBar.Overlay.PROGRESS;

        String normalized = value.trim().replace('-', '_').toUpperCase(Locale.ROOT);

        try {
            return BossBar.Overlay.valueOf(normalized);
        }catch(IllegalArgumentException exception) {
            return BossBar.Overlay.PROGRESS;
        }
    }

    private String formatDuration(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format(Locale.ROOT, "%02d:%02d:%02d", hours, minutes, secs);
    }
}
