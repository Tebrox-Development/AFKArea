package de.tebrox.afkarea.display;

import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.error.Mark;

import java.util.*;
import java.util.function.Supplier;

public final class TabListService {
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;

    private final Map<UUID, MarkerState> markers = new HashMap<>();

    public TabListService(Supplier<MessageConfig> messages, MessageService messageService) {
        this.messages = messages;
        this.messageService = messageService;
    }

    public void applyAfk(Player player) {
        applyMarker(player, messages.get().afkTabFormat, MarkerType.AFK);
    }

    public void applyAfkArea(Player player) {
        applyMarker(player, messages.get().afkAreaTabFormat, MarkerType.AFK_AREA);
    }

    private void applyMarker(Player player, String format, MarkerType type) {
        if(format == null || format.isBlank()) {
            clearMarker(player, type);
            return;
        }

        UUID playerId = player.getUniqueId();
        MarkerState current = markers.get(playerId);

        Component original = current != null ? current.original : player.playerListName();
        Component applied = messageService.parse(format, Placeholder.component("player", original));

        markers.put(playerId, new MarkerState(original, applied, type));
        player.playerListName(applied);
    }

    public void clearAfk(Player player) {
        clearMarker(player, MarkerType.AFK);
    }

    public void clearAfkArea(Player player) {
        clearMarker(player, MarkerType.AFK_AREA);
    }

    private void clearMarker(Player player, MarkerType type) {
        MarkerState state = markers.remove(player.getUniqueId());

        if(state == null || state.type != type) return;

        if(player.playerListName().equals(state.applied())) {
            player.playerListName(state.original());
        }
    }

    public void refreshAfk(Player player) {
        applyAfk(player);
    }

    public void forget(UUID playerId) {
        markers.remove(playerId);
    }

    public void restoreAll(Collection<? extends Player> players) {
        for(Player player : players) {
            MarkerState state = markers.remove(player.getUniqueId());

            if(state == null) continue;

            if(player.playerListName().equals(state.applied)) {
                player.playerListName(state.original);
            }
        }
        markers.clear();
    }

    private enum MarkerType {
        AFK,
        AFK_AREA
    }
    private record MarkerState(Component original, Component applied, MarkerType type) {}
}
