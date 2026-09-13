package de.tebrox.afkarea.bootstrap;

import de.tebrox.afkarea.command.AFKAreaCommands;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.vertexCore.VertexCoreApi;
import de.tebrox.vertexCore.config.Config;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class AFKAreaPlugin extends JavaPlugin {
    private static final int BSTATS_PLUGIN_ID = 34037;

    private Config<AFKAreaConfig> configFile;
    private AFKAreaConfig config;

    private Config<MessageConfig> messageFile;
    private MessageConfig messages;

    private MessageService messageService;

    @Override
    public void onEnable() {
        configFile = new Config<>(this, AFKAreaConfig.class);
        config = configFile.loadConfigObject();

        messageFile = new Config<>(this, MessageConfig.class);
        messages = messageFile.loadConfigObject();

        messageService = new MessageService(() -> messages);

        VertexCoreApi.get().commands().register(this, new AFKAreaCommands(this));

        new Metrics(this, BSTATS_PLUGIN_ID);
        getLogger().info("AFKArea has been enabled");
    }

    @Override
    public void onDisable() {
        VertexCoreApi.get().commands().unregisterAll(this);

        getLogger().info("AFKArea has been disabled");
    }

    public void reloadConfigs() {
        config = configFile.loadConfigObject();
        messages = messageFile.loadConfigObject();
    }

    public MessageConfig messages() {
        return messages;
    }

    public MessageService messageService() {
        return messageService;
    }
}
