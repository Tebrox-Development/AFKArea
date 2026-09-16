package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.reward.data.CommandRewardData;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class RewardService {
    private static final String BYPASS_PERMISSION = "afkarea.bypass.rewards";
    private final JavaPlugin plugin;
    private final RewardRoller rewardRoller;

    public RewardService(JavaPlugin plugin) {
        this(plugin, new RewardRoller());
    }

    public RewardService(JavaPlugin plugin, RewardRoller rewardRoller) {
        this.plugin = plugin;
        this.rewardRoller = rewardRoller;
    }

    public List<CommandRewardData> grant(Player player, AreaData area, int rolls) {
        if(player == null || area == null || rolls <= 0) return List.of();
        if(player.hasPermission(BYPASS_PERMISSION)) return List.of();

        RewardConfigData config = area.getRewards();

        if(config == null) return List.of();

        List<CommandRewardData> selected = rewardRoller.roll(config.getRewards(), rolls, config.isAllowDuplicates(), reward -> canReceive(player, reward));
        for(CommandRewardData reward : selected) {
            executeReward(player, area, reward);
        }
        return selected;
    }

    private boolean canReceive(Player player, CommandRewardData reward) {
        String permission = reward.getPermission();

        return permission == null || permission.isBlank() || player.hasPermission(permission);
    }

    private void executeReward(Player player, AreaData area, CommandRewardData reward) {
        List<String> commands = reward.getCommands();

        if(commands == null) return;

        for(String rawCommand : commands) {
            if(rawCommand == null || rawCommand.isBlank()) continue;

            String command = renderCommand(rawCommand, player, area, reward).trim();

            if(command.startsWith("/")) command = command.substring(1);
            if(command.isBlank()) continue;

            try {
                boolean handled = plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                if(!handled) logFailure(player, area, reward, "command was not handled");
            }catch(CommandException exception) {
                logFailure(player, area, reward, exception.getMessage());
            }
        }
    }

    private String renderCommand(String command, Player player, AreaData area, CommandRewardData reward) {
        String areaName = area.getName() == null ? area.getUniqueId() : area.getName();
        String rewardId = reward.getId() == null ? "" : reward.getId();

        return command
                .replace("<player>", player.getName())
                .replace("<uuid>", player.getUniqueId().toString())
                .replace("<area>", area.getUniqueId())
                .replace("<area_name>", areaName)
                .replace("<reward>", rewardId);
    }

    private void logFailure(Player player, AreaData area, CommandRewardData reward, String reason) {
        plugin.getLogger().warning("Failed to execute reward '" + reward.getId() + "' for player '" + player.getName() + "' in AFK area '" + area.getUniqueId() + "': " + reason);
    }
}
