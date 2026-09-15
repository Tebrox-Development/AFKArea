package de.tebrox.afkarea.display;

import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.state.PlayerStateService;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class AreaVisibilityService {
    private final JavaPlugin plugin;
    private final PlayerStateService stateService;
    private final Supplier<AFKAreaConfig> config;

    private final Set<VisibilityPair> tabHiddenByUs = new HashSet<>();

    public AreaVisibilityService(JavaPlugin plugin, PlayerStateService stateService, Supplier<AFKAreaConfig> config) {
        this.plugin = plugin;
        this.stateService = stateService;
        this.config = config;
    }

    public void refreshTarget(Player target) {
        for(Player viewer : plugin.getServer().getOnlinePlayers()) {
            if(viewer.equals(target)) continue;;
            syncPair(viewer, target);
        }
    }

    public void syncViewer(Player viewer) {
        for(Player target : plugin.getServer().getOnlinePlayers()) {
            if(viewer.equals(target)) continue;
            syncPair(viewer, target);
        }
    }

    public void refreshAll(Collection<? extends Player> players) {
        for(Player viewer : players) {
            for(Player target : players) {
                if(viewer.equals(target)) continue;
                syncPair(viewer, target);
            }
        }
    }

    private void syncPair(Player viewer, Player target) {
        restorePair(viewer, target);

        if(stateService.getState(target.getUniqueId()) != PlayerState.AFK_AREA) return;
        if(canSeeHidden(viewer)) return;

        AFKAreaConfig current = config.get();

        boolean hideWorld = current.hideFromPlayers;
        boolean hideTab = current.hideFromTablist;

        if(hideWorld && hideTab) {
            viewer.hidePlayer(plugin, target);
        }

        if(hideWorld) {
            viewer.hideEntity(plugin, target);
        }

        if(hideTab) {
            hideFromTab(viewer, target);
        }
    }

    private void restorePair(Player viewer, Player target) {
        viewer.showPlayer(plugin, target);
        viewer.showEntity(plugin, target);

        restoreTab(viewer, target);
    }

    private void hideFromTab(Player viewer, Player target) {
        VisibilityPair pair = new VisibilityPair(viewer.getUniqueId(), target.getUniqueId());

        if(!viewer.isListed(target)) return;

        if(viewer.unlistPlayer(target)) {
            tabHiddenByUs.add(pair);
        }
    }

    private void restoreTab(Player viewer, Player target) {
        VisibilityPair pair = new VisibilityPair(viewer.getUniqueId(), target.getUniqueId());

        if(!tabHiddenByUs.contains(pair)) return;
        if(!viewer.canSee((Entity) target)) return;

        try {
            viewer.listPlayer(target);
            tabHiddenByUs.remove(pair);
        }catch(IllegalStateException ignored) {}
    }

    private boolean canSeeHidden(Player viewer) {
        String permission= config.get().staffViewPermission;

        return permission != null && !permission.isBlank() && viewer.hasPermission(permission);
    }

    public void forget(UUID playerId) {
        tabHiddenByUs.removeIf(pair -> pair.viewerId().equals(playerId) || pair.targetId().equals(playerId));
    }

    public void restoreAll(Collection<? extends Player> players) {
        for(Player viewer : players) {
            for(Player target : players) {
                if(viewer.equals(target)) continue;
                restorePair(viewer, target);
            }
        }
        tabHiddenByUs.clear();
    }

    private record VisibilityPair(UUID viewerId, UUID targetId) {}
}
