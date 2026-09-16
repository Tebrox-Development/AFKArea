package de.tebrox.afkarea.reward;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.config.MessageConfig;
import de.tebrox.afkarea.message.MessageService;
import de.tebrox.afkarea.reward.data.CommandRewardData;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.function.Supplier;

public final class RewardService {
    private static final String BYPASS_PERMISSION = "afkarea.bypass.rewards";
    private final JavaPlugin plugin;
    private final RewardRoller rewardRoller;

    private final Supplier<MessageConfig> messages;
    private final MessageService messageService;

    public RewardService(JavaPlugin plugin, Supplier<MessageConfig> messages, MessageService messageService) {
        this(plugin, new RewardRoller(), messages, messageService);
    }

    public RewardService(JavaPlugin plugin, RewardRoller rewardRoller, Supplier<MessageConfig> messages, MessageService messageService) {
        this.plugin = plugin;
        this.rewardRoller = rewardRoller;
        this.messages = messages;
        this.messageService = messageService;
    }

    public List<CommandRewardData> grant(Player player, AreaData area, int rolls) {
        if(player == null || area == null || rolls <= 0) return List.of();
        if(player.hasPermission(BYPASS_PERMISSION)) return List.of();

        RewardConfigData config = area.getRewards();

        if(config == null) return List.of();

        List<CommandRewardData> selected = rewardRoller.roll(config.getRewards(), rolls, config.isAllowDuplicates(), reward -> canReceive(player, reward));
        for(CommandRewardData reward : selected) {
            boolean executed = executeReward(player, area, reward);

            if(executed) {
                sendRewardMessage(player, area, config, reward);
            }
        }
        return selected;
    }

    public boolean hasEligibleReward(Player player, AreaData area) {
        if(player == null || area == null) return false;
        if(player.hasPermission(BYPASS_PERMISSION)) return false;

        RewardConfigData config = area.getRewards();
        if(config == null || config.getRewards() == null) return false;

        for(CommandRewardData reward : config.getRewards()) {
            if(reward == null || !reward.isEnabled()) continue;
            double weight = reward.getWeight();

            if(!Double.isFinite(weight) || weight <= 0.0) continue;

            if(canReceive(player, reward)) return true;
        }

        return false;
    }

    private boolean canReceive(Player player, CommandRewardData reward) {
        String permission = reward.getPermission();

        return permission == null || permission.isBlank() || player.hasPermission(permission);
    }

    private boolean executeReward(Player player, AreaData area, CommandRewardData reward) {
        List<String> commands = reward.getCommands();

        if(commands == null) return false;
        boolean executed = false;

        for(String rawCommand : commands) {
            if(rawCommand == null || rawCommand.isBlank()) continue;

            String command = renderCommand(rawCommand, player, area, reward).trim();

            if(command.startsWith("/")) command = command.substring(1);
            if(command.isBlank()) continue;

            try {
                boolean handled = plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                if(!handled) {
                    logFailure(player, area, reward, "command was not handled");
                }else{
                    executed = true;
                }
            }catch(CommandException exception) {
                logFailure(player, area, reward, exception.getMessage());
            }
        }

        return executed;
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

    private void sendRewardMessage(Player player, AreaData area, RewardConfigData config, CommandRewardData reward) {
        String message = resolveRewardMessage(config, reward);
        messageService.send(player, message, Placeholder.unparsed("player", player.getName()), Placeholder.unparsed("area", area.getUniqueId()), Placeholder.unparsed("area_name", displayName(area)), Placeholder.unparsed("reward", reward.getId() == null ? "" : reward.getId()));
    }

    private String resolveRewardMessage(RewardConfigData config, CommandRewardData reward) {
        if(reward.getMessage() != null) return reward.getMessage();
        if(config.getMessage() != null) return config.getMessage();

        return messages.get().rewardReceived;
    }

    private String displayName(AreaData area) {
        String name = area.getName();

        return name == null || name.isBlank() ? area.getUniqueId() : name;
    }
}
