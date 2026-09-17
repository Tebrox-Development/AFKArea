package de.tebrox.afkarea.integration;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class PlaceholderApiIntegration implements PluginIntegration {
    private static final String PLUGIN_NAME = "PlaceholderAPI";
    private final Plugin plugin;

    public PlaceholderApiIntegration(Plugin plugin) {
        this.plugin = plugin;
    }

    public static PlaceholderApiIntegration detect(JavaPlugin owner) {
        Objects.requireNonNull(owner, "plugin");

        Plugin plugin = owner.getServer().getPluginManager().getPlugin(PLUGIN_NAME);
        return new PlaceholderApiIntegration(plugin);
    }

    @Override
    public String pluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public boolean isAvailable() {
        return plugin != null && plugin.isEnabled();
    }
}
