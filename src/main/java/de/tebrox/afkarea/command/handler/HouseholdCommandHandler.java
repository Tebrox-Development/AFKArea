package de.tebrox.afkarea.command.handler;

import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.command.support.KnownPlayerLookup;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class HouseholdCommandHandler {
    private final AFKAreaPlugin plugin;

    public HouseholdCommandHandler(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void link(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 2) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdLinkUsage);
            return;
        }

        String firstInput = args[0];
        String secondInput = args[1];

        OfflinePlayer first = KnownPlayerLookup.find(firstInput);

        if (first == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", firstInput));
            return;
        }

        OfflinePlayer second = KnownPlayerLookup.find(secondInput);

        if (second == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", secondInput));
            return;
        }

        String firstName = first.getName() == null ? firstInput : first.getName();
        String secondName = second.getName() == null ? secondInput : second.getName();

        plugin.householdManager().link(
                first.getUniqueId(),
                second.getUniqueId(),
                result -> {
                    switch (result) {
                        case LINKED -> plugin.messageService().send(ctx.sender(), plugin.messages().householdLinked, Placeholder.unparsed("player1", firstName), Placeholder.unparsed("player2", secondName));

                        case SAME_PLAYER -> plugin.messageService().send(ctx.sender(), plugin.messages().householdSamePlayer);
                        case ALREADY_SAME_HOUSEHOLD -> plugin.messageService().send(ctx.sender(), plugin.messages().householdAlreadySame, Placeholder.unparsed("player1", firstName), Placeholder.unparsed("player2", secondName));
                        case DIFFERENT_HOUSEHOLDS -> plugin.messageService().send(ctx.sender(), plugin.messages().householdDifferentHouseholds, Placeholder.unparsed("player1", firstName), Placeholder.unparsed("player2", secondName));
                        case DATA_LOADING -> plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
                        case BUSY -> plugin.messageService().send(ctx.sender(), plugin.messages().householdBusy);
                    }
                },
                error -> {
                    plugin.getLogger().severe("Failed to link household members: " + error.getMessage());
                    error.printStackTrace();

                    plugin.messageService().send(ctx.sender(), plugin.messages().householdSaveFailed);
                }
        );
    }

    public void unlink(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnlinkUsage);
            return;
        }

        String input = args[0];

        OfflinePlayer player = KnownPlayerLookup.find(input);

        if (player == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", input));
            return;
        }

        String name = player.getName() == null ? input : player.getName();

        plugin.householdManager().unlink(
                player.getUniqueId(),
                result -> {
                    switch (result) {
                        case UNLINKED -> plugin.messageService().send(ctx.sender(), plugin.messages().householdUnlinked, Placeholder.unparsed("player", name));
                        case NOT_LINKED -> plugin.messageService().send(ctx.sender(), plugin.messages().householdNotLinked, Placeholder.unparsed("player", name));
                        case DATA_LOADING -> plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
                        case BUSY -> plugin.messageService().send(ctx.sender(), plugin.messages().householdBusy);
                    }
                },
                error -> {
                    plugin.getLogger().severe("Failed to unlink household member: " + error.getMessage());

                    error.printStackTrace();
                    plugin.messageService().send(ctx.sender(), plugin.messages().householdSaveFailed);
                }
        );
    }

    public void info(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if (args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdInfoUsage);
            return;
        }

        if (!plugin.householdManager().isLoaded()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
            return;
        }

        String input = args[0];
        OfflinePlayer player = KnownPlayerLookup.find(input);

        if (player == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdUnknownPlayer, Placeholder.unparsed("player", input));
            return;
        }

        Set<UUID> members = plugin.householdManager().membersOf(player.getUniqueId());
        String name = player.getName() == null ? input : player.getName();

        if (members.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdNotLinked, Placeholder.unparsed("player", name)
            );

            return;
        }

        List<String> names = members.stream().map(KnownPlayerLookup::displayPlayerName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
        plugin.messageService().send(ctx.sender(), plugin.messages().householdInfo, Placeholder.unparsed("player", name), Placeholder.unparsed("members", String.join(", ", names))
        );
    }

    public void list(CommandContext ctx) {
        if (!plugin.householdManager().isLoaded()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdDataLoading);
            return;
        }

        List<Set<UUID>> groups = plugin.householdManager().getMemberGroups();
        if (groups.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().householdListEmpty);
            return;
        }

        List<String> entries = new ArrayList<>();
        for (Set<UUID> group : groups) {
            List<String> names = group.stream().map(KnownPlayerLookup::displayPlayerName).sorted(String.CASE_INSENSITIVE_ORDER).toList();
            entries.add("- " + String.join(", ", names));
        }

        entries.sort(String.CASE_INSENSITIVE_ORDER);
        plugin.messageService().send(ctx.sender(), plugin.messages().householdList, Placeholder.unparsed("count", Integer.toString(groups.size())), Placeholder.unparsed("households", String.join("\n", entries))
        );
    }
}
