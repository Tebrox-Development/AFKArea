package de.tebrox.afkarea.display;

import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.reward.RewardSessionService;
import de.tebrox.afkarea.util.DurationFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Supplier;

import static de.tebrox.afkarea.util.DurationFormatter.formatDuration;

public final class ActionBarDisplayService {

    private final JavaPlugin plugin;
    private final RewardSessionService rewardSessionService;
    private final Supplier<AFKAreaConfig> config;
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;

    private final Set<UUID> visiblePlayers = new HashSet<>();

    private BukkitTask task;

    public ActionBarDisplayService(JavaPlugin plugin, RewardSessionService rewardSessionService, Supplier<AFKAreaConfig> config, Supplier<MessageConfig> messages, MessageService messageService) {
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

        clearAll();
    }

    public void refreshAll() {
        tick();
    }

    private void tick() {
        cleanupOfflinePlayers();

        if(!config.get().actionBarEnabled) {
            clearAll();
            return;
        }

        for(Player player : plugin.getServer().getOnlinePlayers()) {
            Optional<AFKDisplayContext> context = AFKDisplayContext.resolve(rewardSessionService, player.getUniqueId());

            if(context.isEmpty()) {
                clear(player);
                continue;
            }

            AFKDisplayContext display = context.get();

            String text = display.hasNextReward() ? messages.get().actionBarText : messages.get().actionBarNoRewardText;

            if(text == null || text.isBlank()) {
                clear(player);
                continue;
            }

            show(player, display, text);
        }

    }

    private void show(Player player, AFKDisplayContext context, String text) {
        player.sendActionBar(messageService.parse(text, context.placeholders()));
        visiblePlayers.add(player.getUniqueId());
    }

    private void clear(Player player) {
        if(!visiblePlayers.remove(player.getUniqueId())) return;
        player.sendActionBar(Component.empty());
    }

    private void clearAll() {
        for(UUID playerId : Set.copyOf(visiblePlayers)) {
            Player player = plugin.getServer().getPlayer(playerId);
            if(player != null) player.sendActionBar(Component.empty());
        }

        visiblePlayers.clear();
    }

    private void cleanupOfflinePlayers() {
        visiblePlayers.removeIf(playerId -> plugin.getServer().getPlayer(playerId) == null);
    }
}