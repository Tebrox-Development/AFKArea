package de.tebrox.afkarea.integration;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class TabIntegration implements PluginIntegration {
    private static final String PLUGIN_NAME = "TAB";
    private final Plugin plugin;

    public TabIntegration(Plugin plugin) {
        this.plugin = plugin;
    }

    public static TabIntegration detec(JavaPlugin owner) {
        Objects.requireNonNull(owner, "plugin");
        Plugin plugin = owner.getServer().getPluginManager().getPlugin(PLUGIN_NAME);

        return new TabIntegration(plugin);
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
