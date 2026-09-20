package de.tebrox.afkarea.command.handler;

import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Locale;

public final class DisplayCommandHandler {
    private final AFKAreaPlugin plugin;
    public DisplayCommandHandler(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void display(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().displayAdminUsage);
            return;
        }

        String display = args[0].toLowerCase(Locale.ROOT);
        String state = args[1].toLowerCase(Locale.ROOT);
        boolean enabled;

        if("on".equals(state)) {
            enabled = true;
        }else if("off".equals(state)) {
            enabled = false;
        }else{
            plugin.messageService().send(ctx.sender(), plugin.messages().displayAdminInvalidState);
            return;
        }

        switch(display) {
            case "bossbar" -> updateBossBar(ctx, enabled);
            case "actionbar" -> updateActionBar(ctx, enabled);
            default -> plugin.messageService().send(ctx.sender(), plugin.messages().displayAdminInvalidType);
        }
    }

    private void updateBossBar(CommandContext ctx, boolean enabled) {
        boolean previous = plugin.config().bossBarEnabled;
        plugin.config().bossBarEnabled = enabled;

        if(!plugin.persistConfig()) {
            plugin.config().bossBarEnabled = previous;

            sendSaveFailed(ctx);
            return;
        }

        plugin.bossBarDisplayService().refreshAll();

        sendUpdated(ctx, "bossbar", enabled);
    }

    private void updateActionBar(CommandContext ctx, boolean enabled) {
        boolean previous = plugin.config().actionBarEnabled;
        plugin.config().actionBarEnabled = enabled;

        if(!plugin.persistConfig()) {
            plugin.config().actionBarEnabled = previous;
            sendSaveFailed(ctx);
            return;
        }

        plugin.actionBarDisplayService().refreshAll();
        sendUpdated(ctx, "actionbar", enabled);
    }

    private void sendUpdated(CommandContext ctx, String display, boolean enabled) {
        plugin.messageService().send(ctx.sender(), plugin.messages().displayAdminUpdated, Placeholder.unparsed("display", display), Placeholder.unparsed("state", enabled ? "on" : "off"));
    }

    private void sendSaveFailed(CommandContext ctx) {
        plugin.messageService().send(ctx.sender(), plugin.messages().displayAdminSaveFailed);
    }
}
