<p align="center">
  <img src="docs/assets/afkarea-banner.jpg" alt="AFKArea">
</p>

<h1 align="center">AFKArea</h1>

<p align="center">
  Configurable AFK areas, automatic idle handling, visibility control and weighted rewards for Paper servers.
</p>

<p align="center">
  <a href="https://github.com/Tebrox-Development/AFKArea/actions/workflows/ci.yml">
    <img src="https://github.com/Tebrox-Development/AFKArea/actions/workflows/ci.yml/badge.svg?branch=development" alt="Build">
  </a>
  <a href="https://bstats.org/plugin/bukkit/AFKArea/34037">
    <img src="https://img.shields.io/badge/bStats-Statistics-4c9b3f" alt="bStats">
  </a>
  <a href="https://github.com/Tebrox-Development/VertexCore">
    <img src="https://img.shields.io/badge/Requires-VertexCore-blue" alt="VertexCore">
  </a>
  <img src="https://img.shields.io/badge/status-development-yellow" alt="Development status">
</p>

**AFKArea** is a Paper plugin for managing dedicated AFK zones and normal player inactivity in one system. Players can be marked AFK automatically, teleported into a configured AFK area after a longer idle period, hidden from other players while inside an AFK area and rewarded through weighted command-based reward pools.

The plugin is built on **VertexCore** and is currently under active development. The `development` branch represents the current implementation and is not yet a stable public release.

## Current development status

Implemented in the current `development` branch:

- Automatic AFK detection based on player inactivity
- Manual AFK toggle with `/afk`
- Configurable automatic teleport to a default AFK area after a longer idle period
- Native cuboid AFK areas with in-game selection and management commands
- Per-area teleport locations
- Area enter/leave tracking with a dedicated `AFK_AREA` player state
- Configurable hiding of AFK-area players from the world and/or player list
- Staff visibility bypass permission
- Configurable AFK and AFK-area tab-list markers
- Persistent AFK-area data through the VertexCore database layer
- JSON as the default database backend with MySQL connection settings available
- Weighted command rewards with per-reward permissions
- Multiple reward rolls with optional duplicate prevention
- Interval-based reward schedules
- One-time milestone reward schedules based on time spent inside an AFK area
- Internal reward placeholders for player, UUID, area and reward data
- bStats metrics
- Maven CI plus a compatibility compile against the Paper 1.21.4 API

Not yet feature-complete:

- WorldGuard is already declared as an optional dependency, but the current runtime region provider only supports native cuboid areas
- PlaceholderAPI is declared as optional, but AFKArea-specific PlaceholderAPI integration is not implemented yet
- Reward configuration is already part of the persisted area model, but dedicated in-game reward administration commands are not available yet
- The configured per-IP reward limit is present in `config.yml`, but enforcement is not wired into the reward flow yet
- Area priority and enabled state are already supported by the data/runtime model, but do not yet have dedicated administration commands

## Requirements

- **Paper** server
- Current primary target: **Paper 26.2**
- The compatibility workflow additionally verifies compilation against the **Paper 1.21.4 API**
- **VertexCore v1.1.0** is required at runtime
- Plugin bytecode targets **Java 21**
- The Maven build currently requires **JDK 25**

AFKArea is currently Paper-specific. Among other things, activity tracking uses Paper API events, so Spigot is not a supported runtime at this stage.

## Installation

There is no stable release yet. For development testing:

1. Install a compatible build of **VertexCore**.
2. Build AFKArea from the `development` branch with JDK 25:

   ```bash
   mvn -B -ntp verify
   ```

3. Copy the generated `AFKArea-0.1.0-SNAPSHOT.jar` from `target/` into the server's `plugins` directory.
4. Start the Paper server and adjust the generated configuration as needed.

## AFK handling

By default, players are marked AFK after **300 seconds** of inactivity. If automatic teleporting is enabled, an AFK player is teleported to the configured target area after **1800 seconds** of inactivity.

The default target area is `spawn-afk`.

Activity tracking currently reacts to player input such as movement/look input, chat, commands, inventory interaction, entity/block interaction, held-item changes, hand swapping, item drops and related player actions.

Automatic AFK handling is suspended for players with `afkarea.bypass.auto-afk`, spectators, dead players and sleeping players.

## AFK areas

The current implementation supports native **cuboid** regions.

Areas are stored persistently and can contain:

- Unique ID and display name
- Enabled state
- Priority
- Region definition
- Teleport destination
- Reward configuration

If multiple enabled areas overlap, the runtime checks higher-priority areas first and then falls back to the area ID for deterministic ordering.

### Creating a cuboid area

1. Stand at the first corner and run `/afkarea pos1`.
2. Stand at the opposite corner and run `/afkarea pos2`.
3. Create the area with `/afkarea create <id> cuboid`.
4. Optionally set its teleport destination with `/afkarea setteleport <id>`.

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/afk` | Toggle your manual AFK state | `afkarea.command.afk` |
| `/afkarea` | Teleport to the configured default AFK area | `afkarea.command.teleport` |
| `/afkarea reload` | Reload configuration and messages | `afkarea.admin.reload` |
| `/afkarea pos1` | Set the first cuboid selection point | `afkarea.admin.selection` |
| `/afkarea pos2` | Set the second cuboid selection point | `afkarea.admin.selection` |
| `/afkarea create <id> cuboid` | Create a cuboid AFK area from the current selection | `afkarea.admin.create` |
| `/afkarea redefine <id>` | Replace an area's cuboid with the current selection | `afkarea.admin.redefine` |
| `/afkarea rename <id> <name>` | Change an area's display name | `afkarea.admin.rename` |
| `/afkarea delete <id>` | Delete an AFK area | `afkarea.admin.delete` |
| `/afkarea setteleport <id>` | Save your current location as the area's teleport destination | `afkarea.admin.setteleport` |
| `/afkarea tp <id>` | Teleport to a specific AFK area | `afkarea.admin.tp` |
| `/afkarea list` | List all configured AFK areas | `afkarea.admin.list` |
| `/afkarea info <id>` | Show information about an AFK area | `afkarea.admin.info` |

Area IDs support command suggestions for the relevant administration commands.

## Permissions

- `afkarea.command.afk` - use `/afk`; granted by default
- `afkarea.command.teleport` - use `/afkarea`; granted by default
- `afkarea.admin.reload` - reload AFKArea
- `afkarea.admin.selection` - set cuboid selection points
- `afkarea.admin.create` - create AFK areas
- `afkarea.admin.redefine` - redefine native cuboid areas
- `afkarea.admin.rename` - rename AFK areas
- `afkarea.admin.delete` - delete AFK areas
- `afkarea.admin.setteleport` - set area teleport destinations
- `afkarea.admin.tp` - teleport to a specific AFK area
- `afkarea.admin.list` - list configured AFK areas
- `afkarea.admin.info` - inspect AFK area data
- `afkarea.bypass.auto-afk` - bypass automatic AFK detection and teleporting
- `afkarea.bypass.rewards` - prevent the player from receiving AFK-area rewards
- `afkarea.staff.see-hidden` - default permission used to see players hidden by AFK areas

Individual rewards may additionally require their own configurable permission node.

## Visibility

Players inside an AFK area can be hidden from other players in two independent ways:

- Hidden from the game world
- Hidden from the player list

Both options are enabled by default. Viewers with the configured staff permission can still see hidden AFK-area players.

AFK and AFK-area player-list markers use configurable MiniMessage formats and preserve the player's previous list name when the marker is removed.

## Rewards

Each AFK area can store its own reward configuration. Rewards are selected using weighted random selection and executed as console commands.

A reward can define:

- An ID
- Enabled state
- Weight
- Optional permission requirement
- One or more console commands
- An optional message field reserved in the reward data model

The current reward engine supports two schedule types:

- `interval` - repeat reward rolls after a configurable number of seconds while the player remains inside the area
- `milestones` - grant one-time reward rolls after configured stay-time milestones during the current area session

Leaving the AFK area resets the player's reward session and completed milestones.

### Reward command placeholders

The following internal placeholders are currently replaced inside reward commands:

- `<player>` - player name
- `<uuid>` - player UUID
- `<area>` - AFK area ID
- `<area_name>` - AFK area display name
- `<reward>` - selected reward ID

## Configuration

The main configuration is generated as `config.yml` through VertexCore's configuration system.

Current defaults include:

```yaml
afk:
  mark-after-seconds: 300
  teleport-after-seconds: 1800
  auto-teleport:
    enabled: true
    target-area: spawn-afk

visibility:
  hide-from-players: true
  hide-from-tablist: true
  staff-view-permission: afkarea.staff.see-hidden

anti-abuse:
  max-rewarding-players-per-ip: 2

database:
  backend: json
  use-queue: true
  timeout-millis: 5000
  pool-size: 5
  table-prefix: afkarea_
  mysql:
    url: ""
    user: ""
    password: ""
```

The anti-abuse value is already part of the configuration schema, but the per-IP reward limit is not enforced yet in the current development implementation.

## Optional integrations

The plugin descriptor currently declares **WorldGuard** and **PlaceholderAPI** as optional server dependencies so the integrations can be added without making them mandatory.

At the current development head, neither integration should be considered feature-complete. Native cuboid areas are the only active region implementation.

## Metrics

AFKArea uses the official **bStats Bukkit 3.2.1** library for anonymous usage metrics. The library is shaded and relocated into the plugin JAR.

- bStats plugin ID: **34037**
- Statistics: https://bstats.org/plugin/bukkit/AFKArea/34037

## Development

The current Maven project version is `0.1.0-SNAPSHOT`.

CI runs on self-hosted runners and verifies the project with JDK 25 while producing Java 21 bytecode. A separate compatibility workflow recompiles the project against the Paper 1.21.4 API.

The active development branch is:

- `development`

The stable branch is not yet representative of the current feature set.

## Project links

- Source repository: https://github.com/Tebrox-Development/AFKArea
- Development branch: https://github.com/Tebrox-Development/AFKArea/tree/development
- Issue tracker: https://github.com/Tebrox-Development/AFKArea/issues
- VertexCore: https://github.com/Tebrox-Development/VertexCore
- bStats: https://bstats.org/plugin/bukkit/AFKArea/34037
