<p align="center">
  <img src="docs/assets/afkarea-banner.jpg" alt="AFKArea">
</p>

<h1 align="center">AFK Area</h1>

<p align="center">
  Automatic AFK handling, configurable AFK areas, visibility control and rewards for Paper servers.
</p>

<p align="center">
  <a href="https://github.com/Tebrox-Development/AFKArea/actions/workflows/ci.yml">
    <img src="https://github.com/Tebrox-Development/AFKArea/actions/workflows/ci.yml/badge.svg?branch=development" alt="Build">
  </a>
  <a href="https://github.com/Tebrox-Development/VertexCore">
    <img src="https://img.shields.io/badge/Requires-VertexCore-blue" alt="VertexCore">
  </a>
</p>

**AFKArea** combines automatic player inactivity handling with dedicated AFK areas.

Players can be marked AFK automatically or manually, moved into configured AFK areas after longer periods of inactivity and rewarded while staying inside them.

AFK areas can use native cuboid regions or optional WorldGuard regions.

## Features

- Automatic and manual AFK handling
- Native cuboid AFK areas
- Optional WorldGuard region support
- Automatic AFK area teleporting
- Cross-world teleport destinations
- Configurable area priorities
- Player visibility control with staff bypass
- Weighted command rewards
- Interval and milestone reward schedules
- Per-reward permissions and messages
- IP-based reward limiting
- Persistent household grouping
- Persistent AFK session statistics
- Bossbar and actionbar displays
- PlaceholderAPI integration
- TAB integration
- JSON, H2 and MySQL/MariaDB persistence
- MiniMessage and legacy `&` color support
- bStats metrics

## Requirements

- **Paper 1.21.4 or newer**
- **Java 21 or newer**
- **VertexCore v1.1.0**

Optional integrations:

- **WorldGuard + WorldEdit** — WorldGuard-backed AFK areas
- **PlaceholderAPI** — AFKArea placeholders
- **TAB** — external player-list formatting

AFKArea is designed for Paper servers and does not currently support Spigot.

## Installation

1. Install **VertexCore v1.1.0**.
2. Place the AFKArea JAR in the server's `plugins` directory.
3. Install any optional integrations you want to use.
4. Start the server.
5. Configure AFKArea and create your AFK areas.

For complete setup instructions, see the [AFKArea Wiki](https://github.com/Tebrox-Development/AFKArea/wiki).

## Documentation

The [AFKArea Wiki](https://github.com/Tebrox-Development/AFKArea/wiki) contains the complete documentation for:

- Installation and first setup
- Configuration
- AFK handling
- AFK areas
- WorldGuard integration
- Commands and permissions
- Rewards
- Anti-abuse and households
- PlaceholderAPI and TAB
- Bossbar and actionbar displays
- Player statistics
- Database and storage configuration
- Troubleshooting

## Integrations

### WorldGuard

WorldGuard can be used instead of native cuboid regions when defining AFK areas.

### PlaceholderAPI

AFKArea provides placeholders for player state, AFK areas, session information and persistent statistics.

### TAB

When TAB is installed, AFKArea can provide its AFK player-list suffix through PlaceholderAPI instead of modifying the player-list name directly.

## Metrics

AFKArea uses bStats for anonymous usage statistics.

## Project links

- [Documentation](https://github.com/Tebrox-Development/AFKArea/wiki)
- [Issue tracker](https://github.com/Tebrox-Development/AFKArea/issues)
- [VertexCore](https://github.com/Tebrox-Development/VertexCore)
