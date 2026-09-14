package de.tebrox.afkarea.display;

import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
        String format = messages.get().afkTabFormat;

        if(format == null || format.isBlank()) {
            clearAfk(player);
            return;
        }

        UUID playerId = player.getUniqueId();

        MarkerState current = markers.get(playerId);

        Component original = current != null ? current.original() : player.playerListName();
        Component applied = messageService.parse(format, Placeholder.component("player", original));

        markers.put(playerId, new MarkerState(original, applied));
        player.playerListName(applied);
    }

    public void clearAfk(Player player) {
        MarkerState state = markers.remove(player.getUniqueId());

        if(state == null) return;

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
            clearAfk(player);
        }
        markers.clear();
    }

    private record MarkerState(Component original, Component applied) {}
}
