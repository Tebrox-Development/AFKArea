package de.tebrox.afkarea.integration;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class WorldGuardIntegration implements PluginIntegration{
    private static final String PLUGIN_NAME = "WorldGuard";
    private final Plugin plugin;

    public WorldGuardIntegration(Plugin plugin) {
        this.plugin = plugin;
    }

    public static WorldGuardIntegration detect(JavaPlugin owner) {
        Objects.requireNonNull(owner, "plugin");

        Plugin plugin = owner.getServer().getPluginManager().getPlugin("WorldGuard");

        return new WorldGuardIntegration(plugin);
    }

    @Override
    public String pluginName() {
        return PLUGIN_NAME;
    }

    public boolean isAvailable() {
        return plugin != null && plugin.isEnabled();
    }
}
