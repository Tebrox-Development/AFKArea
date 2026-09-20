package de.tebrox.afkarea.command.handler;

import de.tebrox.afkarea.area.AreaEntrySource;
import de.tebrox.afkarea.bootstrap.AFKAreaPlugin;
import de.tebrox.afkarea.command.support.KnownPlayerLookup;
import de.tebrox.afkarea.reward.RewardSessionService;
import de.tebrox.afkarea.state.PlayerState;
import de.tebrox.afkarea.stats.AFKPlayerStatsData;
import de.tebrox.vertexCore.command.api.CommandContext;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import static de.tebrox.afkarea.util.DurationFormatter.formatDuration;

public final class DiagnosticsCommandHandler {
    private static final DateTimeFormatter STATS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).withZone(ZoneId.systemDefault());
    private final AFKAreaPlugin plugin;

    public DiagnosticsCommandHandler(AFKAreaPlugin plugin) {
        this.plugin = plugin;
    }

    public void status(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        Player target;

        if (args.length >= 1) {
            target = Bukkit.getPlayerExact(args[0]);

            if (target == null) {
                plugin.messageService().send(ctx.sender(), plugin.messages().statusPlayerNotOnline, Placeholder.unparsed("player", args[0]));
                return;
            }
        } else if (ctx.sender() instanceof Player player) {
            target = player;
        } else {
            plugin.messageService().send(ctx.sender(), plugin.messages().statusUsage);
            return;
        }

        UUID playerId = target.getUniqueId();

        PlayerState state = plugin.playerStateService().getState(playerId);

        String areaId = plugin.areaSessionService().getAreaId(playerId);

        AreaEntrySource entrySource = plugin.areaSessionService().getEntrySource(playerId);

        long idleSeconds = plugin.activityService().getIdleDuration(playerId).toSeconds();

        OptionalLong session = plugin.rewardSessionService().getSessionSeconds(playerId);

        OptionalLong nextReward = plugin.rewardSessionService().getNextRewardSeconds(playerId);

        RewardSessionService.IpRewardStatus ipStatus = plugin.rewardSessionService().getIpRewardStatus(target);

        plugin.messageService().send(
                ctx.sender(),
                plugin.messages().status,
                Placeholder.unparsed("player", target.getName()),
                Placeholder.unparsed("state", state.name()),
                Placeholder.unparsed("area", areaId == null ? "-" : areaId),
                Placeholder.unparsed("entry", entrySource == null ? "-" : entrySource.name()),
                Placeholder.unparsed("idle", formatDuration(idleSeconds)),
                Placeholder.unparsed("session", session.isPresent() ? formatDuration(session.getAsLong()) : "-"),
                Placeholder.unparsed("next_reward", nextReward.isPresent() ? formatDuration(nextReward.getAsLong()) : "-"),
                Placeholder.unparsed("ip_slots", formatIpSlots(ipStatus))
        );
    }

    public void stats(CommandContext ctx) {
        String[] args = ctx.rawArgs();

        if(args.length < 1) {
            plugin.messageService().send(ctx.sender(), plugin.messages().statsUsage);
            return;
        }

        if(!plugin.playerStatsService().isLoaded()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().statsDataLoading);
            return;
        }

        String input = args[0];
        OfflinePlayer target = KnownPlayerLookup.find(input);

        if(target == null) {
            plugin.messageService().send(ctx.sender(), plugin.messages().statsUnknownPlayer, Placeholder.unparsed("player", input));
            return;
        }

        String playerName = target.getName() == null ? input : target.getName();
        Optional<AFKPlayerStatsData> stats = plugin.playerStatsService().getStats(target.getUniqueId());

        if(stats.isEmpty()) {
            plugin.messageService().send(ctx.sender(), plugin.messages().statsNoData, Placeholder.unparsed("player", playerName));
            return;
        }

        AFKPlayerStatsData data = stats.get();

        plugin.messageService().send(
                ctx.sender(),
                plugin.messages().stats,
                Placeholder.unparsed("player", playerName),
                Placeholder.unparsed("sessions", Long.toString(data.getSessionCount())),
                Placeholder.unparsed("total_time", formatDuration(data.getTotalTimeSeconds())),
                Placeholder.unparsed("longest_session", formatDuration(data.getLongestSessionSeconds())),
                Placeholder.unparsed("last_session", formatDuration(data.getLastSessionSeconds())),
                Placeholder.unparsed("last_area", data.getLastAreaId() == null ? "-" : data.getLastAreaId()),
                Placeholder.unparsed("last_ended", formatTimestamp(data.getLastSessionEndedAtEpochMillis()))
        );
    }

    private String formatTimestamp(long epochMillis) {
        if(epochMillis <= 0L) return "-";
        return STATS_DATE_FORMAT.format(Instant.ofEpochMilli(epochMillis));
    }

    private String formatIpSlots(RewardSessionService.IpRewardStatus status) {
        return switch (status.mode()) {
            case BYPASS -> "bypass";
            case UNLIMITED -> "unlimited";
            case ADDRESS_UNAVAILABLE -> "unavailable";
            case LIMITED -> status.activeIdentities() + "/" + status.limit();
        };
    }
}
