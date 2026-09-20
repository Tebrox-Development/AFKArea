package de.tebrox.afkarea.command.handler;

import de.tebrox.afkarea.area.AreaData;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.reward.data.CommandRewardData;
import de.tebrox.afkarea.reward.data.RewardConfigData;
import de.tebrox.afkarea.reward.data.RewardMilestoneData;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.*;

public final class RewardCommandHandler {
    private final AFKAreaPlugin plugin;

    public RewardCommandHandler(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void info(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInfoUsage);
            return;
        }

        String areaId = args[0];
        AreaData area = plugin.areaManager().getArea(areaId);
        if(area == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return;
        }

        RewardConfigData config = area.getRewards();
        if(config == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminNoConfig, Placeholder.unparsed("area", areaId));
            return;
        }

        int rewardCount = config.getRewards() == null ? 0 : config.getRewards().size();
        int milestoneCount = config.getMilestones() == null ? 0 : config.getMilestones().size();
        boolean interval = "interval".equalsIgnoreCase(config.getScheduleType());

        plugin.messageService().send(
                ctx.sender(),
                plugin.messages().rewardAdminInfo,
                Placeholder.unparsed("area", areaId),
                Placeholder.unparsed("schedule", config.getScheduleType() == null ? "-" : config.getScheduleType()),
                Placeholder.unparsed("interval", interval ? config.getIntervalSeconds() + "s" : "-"),
                Placeholder.unparsed("rolls", interval ? Integer.toString(config.getRolls()) : "-"),
                Placeholder.unparsed("duplicates", Boolean.toString(config.isAllowDuplicates())),
                Placeholder.unparsed("reward_count", Integer.toString(rewardCount)),
                Placeholder.unparsed("milestone_count", Integer.toString(milestoneCount))
        );
    }

    public void list(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 1) {plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminListUsage);
            return;
        }

        String areaId = args[0];
        AreaData area = plugin.areaManager().getArea(areaId);
        if(area == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return;
        }

        RewardConfigData config = area.getRewards();
        if(config == null || config.getRewards() == null || config.getRewards().isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminListEmpty, Placeholder.unparsed("area", areaId));
            return;
        }

        List<String> entries = config.getRewards().stream().filter(Objects::nonNull).sorted(Comparator.comparing(reward -> reward.getId() == null ? "" : reward.getId(), String.CASE_INSENSITIVE_ORDER)).map(this::describeReward).toList();

        plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminList, Placeholder.unparsed("area", areaId), Placeholder.unparsed("count", Integer.toString(entries.size())), Placeholder.unparsed("rewards", String.join("\n", entries)));
    }

    public void add(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminAddUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];
        if(!rewardId.matches("[a-z0-9][a-z0-9_-]*")) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidId);
            return;
        }

        AreaData current = plugin.areaManager().getArea(areaId);
        if(current == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return;
        }

        if(findReward(current.getRewards(), rewardId) != null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminAlreadyExists, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId));
            return;
        }

        AreaData updated = current.copy();
        RewardConfigData config = updated.getRewards();

        if(config == null) {
            config = new RewardConfigData();
            updated.setRewards(config);
        }

        List<CommandRewardData> rewards = new ArrayList<>(config.getRewards() == null ? List.of() : config.getRewards());
        CommandRewardData reward = new CommandRewardData(rewardId);

        reward.setEnabled(false);
        rewards.add(reward);
        config.setRewards(rewards);

        save(ctx, updated, areaId, () -> plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminAdded, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId)));
    }

    public void remove(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminRemoveUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];

        AreaData current = plugin.areaManager().getArea(areaId);

        if(current == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return;
        }

        if(findReward(current.getRewards(), rewardId) == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminUnknown, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId));
            return;
        }

        AreaData updated = current.copy();
        RewardConfigData config = updated.getRewards();

        List<CommandRewardData> rewards = new ArrayList<>(config.getRewards());
        rewards.removeIf(reward -> reward != null && rewardId.equalsIgnoreCase(reward.getId()));
        config.setRewards(rewards);

        save(ctx, updated, areaId, () -> plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminRemoved, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId)));
    }

    private CommandRewardData findReward(RewardConfigData config, String rewardId) {
        if(config == null || config.getRewards() == null) {
            return null;
        }

        return config.getRewards().stream().filter(Objects::nonNull).filter(reward -> reward.getId() != null && rewardId.equalsIgnoreCase(reward.getId())).findFirst().orElse(null);
    }

    private String describeReward(CommandRewardData reward) {
        String id = reward.getId() == null ? "<unnamed>" : reward.getId();
        int commandCount = reward.getCommands() == null ? 0 : reward.getCommands().size();
        String permission = reward.getPermission() == null || reward.getPermission().isBlank() ? "-" : reward.getPermission();

        return String.format(Locale.ROOT, "- %s | %s | weight %.2f | commands %d | permission %s", id, reward.isEnabled() ? "enabled" : "disabled", reward.getWeight(), commandCount, permission);
    }

    private void save(CommandContext ctx, AreaData area, String areaId, Runnable onSuccess) {
        plugin.areaManager().saveArea(area, onSuccess,
                error -> {
                    plugin.getLogger().severe("Failed to update rewards for AFK area '" + areaId + "': " + error.getMessage());
                    error.printStackTrace();
                    plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminSaveFailed, Placeholder.unparsed("area", areaId));
                }
        );
    }

    public void enable(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 3) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminEnableUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];

        boolean enabled;

        if("true".equalsIgnoreCase(args[2])) {
            enabled = true;
        }else if("false".equalsIgnoreCase(args[2])) {
            enabled = false;
        }else{
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidEnabled);
            return;
        }

        RewardEditTarget target = editTarget(ctx, areaId, rewardId);
        if(target == null) return;

        if(enabled && !hasCommands(target.reward())) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCannotEnable, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId));
            return;
        }

        target.reward().setEnabled(enabled);
        save(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminEnabled,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("reward", rewardId),
                        Placeholder.unparsed("enabled", Boolean.toString(enabled))
                )
        );
    }

    public void commandList(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandListUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];

        AreaData area = plugin.areaManager().getArea(areaId);
        if(area == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return;
        }

        CommandRewardData reward = findReward(area.getRewards(), rewardId);
        if(reward == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminUnknown, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId));
            return;
        }

        if(!hasCommands(reward)) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandListEmpty, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId));
            return;
        }

        List<String> entries = new ArrayList<>();
        for(int index = 0; index < reward.getCommands().size(); index++) {
            entries.add((index + 1) + ". " + reward.getCommands().get(index));
        }

        plugin.messageService().send(
                ctx.sender(),
                plugin.messages().rewardAdminCommandList,
                Placeholder.unparsed("area", areaId),
                Placeholder.unparsed("reward", rewardId),
                Placeholder.unparsed("commands", String.join("\n", entries)
                )
        );
    }

    public void commandAdd(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 3) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandAddUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];
        String command = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

        if(command.isBlank()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandAddUsage);
            return;
        }

        RewardEditTarget target = editTarget(ctx, areaId, rewardId);

        if(target == null) return;
        List<String> commands = new ArrayList<>(target.reward().getCommands() == null ? List.of() : target.reward().getCommands());

        commands.add(command);
        target.reward().setCommands(commands);
        save(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminCommandAdded,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("reward", rewardId),
                        Placeholder.unparsed("command", command)
                )
        );
    }

    public void commandRemove(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 3) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandRemoveUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];
        int index;

        try {
            index = Integer.parseInt(args[2]);
        }catch(NumberFormatException exception) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandInvalidIndex);
            return;
        }

        RewardEditTarget target = editTarget(ctx, areaId, rewardId);

        if(target == null) return;
        List<String> commands = new ArrayList<>(target.reward().getCommands() == null ? List.of() : target.reward().getCommands());
        if(index < 1 || index > commands.size()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminCommandInvalidIndex);
            return;
        }

        String removed = commands.remove(index - 1);
        target.reward().setCommands(commands);
        if(commands.isEmpty()) target.reward().setEnabled(false);

        save(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminCommandRemoved,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("reward", rewardId),
                        Placeholder.unparsed("command", removed))
        );
    }

    public void weight(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 3) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminWeightUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];
        double weight;

        try {
            weight = Double.parseDouble(args[2]);
        }catch(NumberFormatException exception) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidWeight);
            return;
        }

        if(!Double.isFinite(weight) || weight <= 0.0D) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidWeight
            );
            return;
        }

        RewardEditTarget target = editTarget(ctx, areaId, rewardId);

        if(target == null) return;
        target.reward().setWeight(weight);

        save(ctx, target.area(), areaId, () -> plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminWeightSet, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId), Placeholder.unparsed("weight", Double.toString(weight)))
        );
    }

    public void permission(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 3) {plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminPermissionUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];
        String permission = args[2].trim();

        if(permission.isBlank()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminPermissionUsage);
            return;
        }

        RewardEditTarget target = editTarget(ctx, areaId, rewardId);
        if(target == null) return;

        String storedPermission = "none".equalsIgnoreCase(permission) ? null : permission;
        target.reward().setPermission(storedPermission);

        save(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminPermissionSet,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("reward", rewardId),
                        Placeholder.unparsed("permission", storedPermission == null ? "none" : storedPermission))
        );
    }

    public void message(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 3) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMessageUsage);
            return;
        }

        String areaId = args[0];
        String rewardId = args[1];

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();

        if(message.isBlank()) {plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMessageUsage);
            return;
        }

        RewardEditTarget target = editTarget(ctx, areaId, rewardId);
        if(target == null) return;

        String storedMessage = "none".equalsIgnoreCase(message) ? null : message;
        target.reward().setMessage(storedMessage);

        save(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminMessageSet,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("reward", rewardId),
                        Placeholder.unparsed("message", storedMessage == null ? "default" : storedMessage))
        );
    }

    private RewardEditTarget editTarget(CommandContext ctx, String areaId, String rewardId) {
        AreaData current = plugin.areaManager().getArea(areaId);
        if(current == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return null;
        }

        if(findReward(current.getRewards(), rewardId) == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminUnknown, Placeholder.unparsed("area", areaId), Placeholder.unparsed("reward", rewardId));
            return null;
        }

        AreaData updated = current.copy();
        CommandRewardData reward = findReward(updated.getRewards(), rewardId);

        return new RewardEditTarget(updated, reward);
    }

    private boolean hasCommands(CommandRewardData reward) {
        if(reward.getCommands() == null) return false;
        return reward.getCommands().stream().anyMatch(command -> command != null && !command.isBlank());
    }

    public void interval(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminIntervalUsage);
            return;
        }

        String areaId = args[0];
        int seconds;

        try {
            seconds = Integer.parseInt(args[1]);
        }catch(NumberFormatException exception) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidSeconds);
            return;
        }

        if(seconds <= 0) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidSeconds);
            return;
        }

        RewardConfigEditTarget target = editConfigTarget(ctx, areaId);

        if(target == null) return;
        int rolls = target.config().getRolls();
        if(rolls <= 0) rolls = 1;

        if(args.length >= 3) {
            try {
                rolls = Integer.parseInt(args[2]);
            }catch(NumberFormatException exception) {
                plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidRolls);
                return;
            }

            if(rolls <= 0) {
                plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidRolls);
                return;
            }
        }

        target.config().setScheduleType("interval");
        target.config().setIntervalSeconds(seconds);
        target.config().setRolls(rolls);

        int finalRolls = rolls;

        saveSchedule(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminIntervalSet,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("seconds", Integer.toString(seconds)),
                        Placeholder.unparsed("rolls", Integer.toString(finalRolls)))
        );
    }

    public void milestoneList(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneListUsage);
            return;
        }

        String areaId = args[0];

        AreaData area = plugin.areaManager().getArea(areaId);
        if(area == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return;
        }

        RewardConfigData config = area.getRewards();
        if(config == null || config.getMilestones() == null || config.getMilestones().isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneListEmpty, Placeholder.unparsed("area", areaId));
            return;
        }

        List<String> entries =
                config.getMilestones()
                        .stream()
                        .filter(Objects::nonNull)
                        .sorted(Comparator.comparingInt(RewardMilestoneData::getAfterSeconds))
                        .map(milestone -> "- " + milestone.getAfterSeconds() + "s | rolls " + milestone.getRolls())
                        .toList();

        plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneList,
                Placeholder.unparsed("area", areaId),
                Placeholder.unparsed("count", Integer.toString(entries.size())),
                Placeholder.unparsed("milestones", String.join("\n", entries))
        );
    }

    public void milestoneAdd(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneAddUsage);
            return;
        }

        String areaId = args[0];
        int seconds;

        try {
            seconds = Integer.parseInt(args[1]);
        }catch(NumberFormatException exception) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidSeconds);
            return;
        }

        if(seconds <= 0) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidSeconds);
            return;
        }

        int rolls = 1;
        if(args.length >= 3) {
            try {
                rolls = Integer.parseInt(args[2]);
            }catch(NumberFormatException exception) {
                plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidRolls);
                return;
            }

            if(rolls <= 0) {
                plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidRolls);
                return;
            }
        }

        RewardConfigEditTarget target = editConfigTarget(ctx, areaId);
        if(target == null) return;

        List<RewardMilestoneData> milestones = new ArrayList<>(target.config().getMilestones() == null ? List.of() : target.config().getMilestones());
        boolean exists = milestones.stream().filter(Objects::nonNull).anyMatch(milestone -> milestone.getAfterSeconds() == seconds);

        if(exists) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneExists, Placeholder.unparsed("seconds", Integer.toString(seconds)));
            return;
        }

        milestones.add(new RewardMilestoneData(seconds, rolls)
        );

        milestones.sort(Comparator.comparingInt(RewardMilestoneData::getAfterSeconds)
        );

        target.config().setMilestones(milestones);
        target.config().setScheduleType("milestones");
        int finalRolls = rolls;

        saveSchedule(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminMilestoneAdded,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("seconds", Integer.toString(seconds)),
                        Placeholder.unparsed("rolls", Integer.toString(finalRolls)))
        );
    }

    public void milestoneRemove(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneRemoveUsage);
            return;
        }

        String areaId = args[0];
        int seconds;
        try {
            seconds = Integer.parseInt(args[1]);
        }catch(NumberFormatException exception) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidSeconds);
            return;
        }

        RewardConfigEditTarget target = editConfigTarget(ctx, areaId);
        if(target == null) return;

        List<RewardMilestoneData> milestones = new ArrayList<>(target.config().getMilestones() == null ? List.of() : target.config().getMilestones());

        boolean exists = milestones.stream().filter(Objects::nonNull).anyMatch(milestone -> milestone.getAfterSeconds() == seconds);
        if(!exists) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneNotFound, Placeholder.unparsed("seconds", Integer.toString(seconds)));
            return;
        }

        boolean activeMilestoneSchedule = "milestones".equalsIgnoreCase(target.config().getScheduleType());
        if(activeMilestoneSchedule && milestones.stream().filter(Objects::nonNull).count() == 1L) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneLastActive);
            return;
        }

        milestones.removeIf(milestone -> milestone != null && milestone.getAfterSeconds() == seconds);
        target.config().setMilestones(milestones);
        Runnable success = () -> plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminMilestoneRemoved, Placeholder.unparsed("area", areaId), Placeholder.unparsed("seconds", Integer.toString(seconds)));

        if(activeMilestoneSchedule) {
            saveSchedule(ctx, target.area(), areaId, success);
        }else{
            save(ctx, target.area(), areaId, success);
        }
    }

    public void duplicates(CommandContext ctx) {
        String[] args = ctx.rawArgs();
        if(args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminDuplicatesUsage);
            return;
        }

        String areaId = args[0];
        boolean allowDuplicates;
        if("true".equalsIgnoreCase(args[1])) {
            allowDuplicates = true;
        }else if("false".equalsIgnoreCase(args[1])) {
            allowDuplicates = false;
        }else{
            plugin.messageService().send(ctx.sender(), plugin.messages().rewardAdminInvalidDuplicates);
            return;
        }

        RewardConfigEditTarget target = editConfigTarget(ctx, areaId);
        if(target == null) return;
        target.config().setAllowDuplicates(allowDuplicates);

        save(ctx, target.area(), areaId, () -> plugin.messageService().send(
                        ctx.sender(),
                        plugin.messages().rewardAdminDuplicatesSet,
                        Placeholder.unparsed("area", areaId),
                        Placeholder.unparsed("duplicates", Boolean.toString(allowDuplicates)))
        );
    }

    private RewardConfigEditTarget editConfigTarget(CommandContext ctx, String areaId) {
        AreaData current = plugin.areaManager().getArea(areaId);
        if(current == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().unknownArea, Placeholder.unparsed("area", areaId));
            return null;
        }

        AreaData updated = current.copy();
        RewardConfigData config = updated.getRewards();
        if(config == null) {
            config = new RewardConfigData();
            updated.setRewards(config);
        }

        return new RewardConfigEditTarget(updated, config);
    }

    private void saveSchedule(CommandContext ctx, AreaData area, String areaId, Runnable onSuccess) {
        save(ctx, area, areaId, () -> {plugin.rewardSessionService().resetRewardProgress(areaId);onSuccess.run();});
    }

    private record RewardConfigEditTarget(AreaData area, RewardConfigData config) {}
    private record RewardEditTarget(AreaData area, CommandRewardData reward
    ) {}
}