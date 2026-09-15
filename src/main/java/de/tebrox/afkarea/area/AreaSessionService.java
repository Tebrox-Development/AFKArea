package de.tebrox.afkarea.area;

import de.tebrox.afkarea.activity.ActivityService;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.display.TabListService;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class AreaSessionService {
    private final AreaManager areaManager;
    private final PlayerStateService stateService;
    private final ActivityService activityService;
    private final TabListService tabListService;
    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;

    private final Map<UUID, String> currentAreas = new HashMap<>();

    public AreaSessionService(AreaManager areaManager, PlayerStateService stateService, ActivityService activityService, TabListService tabListService, Supplier<MessageConfig> messages, MessageService messageService) {
        this.areaManager = areaManager;
        this.stateService = stateService;
        this.activityService = activityService;
        this.tabListService = tabListService;
        this.messages = messages;
        this.messageService = messageService;
    }

    public String getAreaId(UUID playerId) {
        return currentAreas.get(playerId);
    }

    public void sync(Player player) {
        sync(player, player.getLocation());
    }

    public void sync(Player player, Location location) {
        if (!areaManager.isLoaded() || location == null) return;

        UUID playerId = player.getUniqueId();
        String previousId = currentAreas.get(playerId);

        AreaData nextArea = areaManager.findArea(location).orElse(null);
        String nextId = nextArea == null ? null : nextArea.getUniqueId();

        if (Objects.equals(previousId, nextId)) return;
        if (previousId != null) leaveArea(player, previousId);
        if (nextArea != null) enterArea(player, nextArea);
    }

    private void enterArea(Player player, AreaData area) {
        UUID playerId = player.getUniqueId();

        if (stateService.getState(playerId) == PlayerState.AFK) {
            tabListService.clearAfk(player);
        }

        currentAreas.put(playerId, area.getUniqueId());
        stateService.setState(playerId, PlayerState.AFK_AREA);

        messageService.send(player, messages.get().areaEntered, Placeholder.unparsed("area", displayName(area)));
    }

    private void leaveArea(Player player, String areaId) {
        UUID playerId = player.getUniqueId();

        currentAreas.remove(playerId);
        stateService.setState(playerId, PlayerState.ACTIVE);
        activityService.recordActivity(playerId);

        AreaData previous = areaManager.getArea(areaId);

        messageService.send(player, messages.get().areaLeft, Placeholder.unparsed("area", previous == null ? areaId : displayName(previous)));
    }

    private String displayName(AreaData area) {
        String name = area.getName();

        return name == null || name.isBlank() ? area.getUniqueId() : name;
    }

    public void clear(UUID playerId) {
        currentAreas.remove(playerId);
    }

    public void clearAll() {
        currentAreas.clear();
    }
}
