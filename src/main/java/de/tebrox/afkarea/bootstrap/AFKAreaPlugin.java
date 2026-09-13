package de.tebrox.afkarea.bootstrap;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class AFKAreaPlugin extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 34037;

    @Override
    public void onEnable() {
        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("AFKArea has been enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("AFKArea has been disabled");
    }
}
