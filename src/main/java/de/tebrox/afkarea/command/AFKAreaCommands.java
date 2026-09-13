package de.tebrox.afkarea.command;

import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.config.AFKAreaConfig;
import de.tebrox.vertexCore.command.annotation.VCommand;
import de.tebrox.vertexCore.command.annotation.VDesc;
import de.tebrox.vertexCore.command.annotation.VPerm;
import de.tebrox.vertexCore.command.annotation.VSub;
import de.tebrox.vertexCore.command.api.CommandContext;

public final class AFKAreaCommands {
    private final AFKAreaPlugin plugin;

    public AFKAreaCommands(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    @VCommand("afkarea")
    @VDesc("AFKArea commands")
    public void root(CommandContext ctx) {
        ctx.reply("/afkarea reload");
    }

    @VSub("afkarea reload")
    @VDesc("Reload AFKArea configuration")
    @VPerm("afkarea.admin.reload")
    public void reload(CommandContext ctx) {
        plugin.reloadConfigs();

        plugin.messageService().send(ctx.sender(), plugin.messages().reloadSuccess);
    }
}
