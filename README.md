# SkyeBlock

A skyblock island plugin for Minecraft 26.2 (Chaos Cubed) on Paper. Each player gets an isolated island world with persistent storage backed by an embedded H2 database. Supports Advanced Slime World Manager for efficient world management.

## Features

- Isolated island worlds: each island lives in its own world for full separation
- H2 embedded database storage for islands, settings, and player data (no YAML)
- 16 bundled schematic templates extracted from the plugin JAR on first run
- Advanced Slime World Manager (ASWM) 4.0.0 integration for world loading
- GUI-driven settings for gamerules, visiting, and island management
- Per-island gamerule overrides with permission-based access control
- Coop system with role-based access (Visitor, Member, Admin, Co-Owner)
- Voting system with 7-day vote tracking
- Warp system with configurable warp points
- Resource world support (Nether, End)
- Visitor protection with granular permission control
- Nether island auto-creation with biome control
- Island deletion cooldown and try-limit system

## Requirements

- Minecraft 26.2 (Chaos Cubed) server
- Paper or Spigot (api-version 26.2)
- Java 21 or newer
- WorldEdit (required dependency)
- WorldGuard, ProtocolLib (optional, for full functionality)
- Advanced Slime World Manager (optional, for efficient world storage)
- LuckPerms (optional, for permission management)

## Installation

1. Build the plugin:
```bash
cd SkyeBlock
mvn clean package
```
2. Copy `target/SkyeBlock-*.jar` to your server's `plugins/` directory.
3. Start the server once to generate configuration files in `plugins/SkyeBlock/`.
4. Configure `config.yml` to match your server setup.
5. Restart the server.

## Commands

### Player Commands

| Command | Description |
|---------|-------------|
| `/island` or `/is` | Teleport to your island (or open creation GUI if you have none) |
| `/island create [type]` | Create a new island with the given template |
| `/island tp` | Teleport to your island |
| `/island settings` | Open the island settings GUI |
| `/island edit <title\|desc\|icon>` | Customize your island name, description, or icon |
| `/island set <home\|visit>` | Set custom teleport locations |
| `/island coop <add\|remove\|role\|visit\|list>` | Manage coop members |
| `/island visit [player]` | Visit another player's island or open the browser |
| `/island lock` | Lock your island (coop members only) |
| `/island unlock` | Unlock your island |
| `/island vote <player>` | Vote for another player's island |
| `/island types` | List available island templates |
| `/island help` | Show the help message |
| `/visit [player]` | Visit another player's island |
| `/hub` | Teleport to the hub world |
| `/spawn` | Teleport to the spawn world |
| `/warp [name]` | Open the warp GUI or teleport to a specific warp |
| `/mobspawning` | Check mob spawning status |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/sba <subcommand>` | Admin command with full access |
| `/island list` | List all islands on the server |
| `/island status` | Show server status information |
| `/island delete` | Delete your island (with confirmation GUI) |
| `/islandpermissions [player]` | Manage per-player island permissions |
| `/warpadmin <create\|delete\|reset\|reload>` | Manage warps and resource worlds |
| `/convertislands <scan\|convert>` | Convert old island data format |
| `/mobspawning <status\|reload>` | Manage global mob spawning settings |

## Permissions

### Default Player Permissions (`skyeblock.player`)

Grants access to basic island commands, visiting, coop, voting, warps, and hub/spawn. Does not include gamerule access.

### Gamerule Permissions

- `skyeblock.settings.gamerules` - Access to common safe gamerules (daylight cycle, weather cycle, keep inventory, mob griefing, mob spawning, fire tick, fall damage, fire damage, drowning damage, insomnia, immediate respawn, natural regeneration, announce advancements, elytra movement check)
- `skyeblock.gamerule.*` - Access to all gamerules including advanced ones (random tick speed, spawn radius, entity cramming, command chain length, sleeping percentage, etc.)
- `skyeblock.gamerule.<name>` - Individual gamerule access (e.g., `skyeblock.gamerule.randomtickspeed`)
- `skyeblock.gamerules.adminbypass` - Bypass all gamerule permission checks

### Admin Permissions

- `skyeblock.admin` - Full admin access to all commands
- `skyeblock.admin.convert` - Island data conversion
- `skyeblock.admin.warp` - Warp management
- `skyeblock.admin.mobspawning` - Global mob spawning management

### Island-Specific Permissions

- `skyeblock.island` - Basic island commands
- `skyeblock.island.visit` - Visit other islands
- `skyeblock.island.vote` - Vote for islands
- `skyeblock.island.edit` - Edit island title, description, icon
- `skyeblock.island.coop` - Manage coop members
- `skyeblock.island.lock` - Lock/unlock islands
- `skyeblock.island.set` - Set custom home/visit locations
- `skyeblock.island.delete` - Delete your own island
- `skyeblock.island.permissions` - Manage per-player island permissions

### Warp Permissions

- `skyeblock.warp` - Access to warp GUI
- `skyeblock.warp.spawn`, `skyeblock.warp.nether`, `skyeblock.warp.end`, `skyeblock.warp.pvp`, `skyeblock.warp.shop` - Individual warp access

## Configuration

All configuration files are in `plugins/SkyeBlock/`:

### config.yml

- `hub` - Hub world settings (world name, spawn coordinates, teleport-on-join)
- `world` - World settings (nether auto-creation, border size)
- `resource-worlds` - Nether and End resource world configuration
- `island` - Island settings (teleport on join, creation cooldown, template mappings, visiting toggle)
- `messages` - All plugin messages in MiniMessage format
- `schematics` - Schematic template path

### Database

Island data, settings, and player data are stored in `plugins/SkyeBlock/skyeblock.db` (H2 embedded database). The database is created automatically on first startup.

### Warps

Warp definitions are stored in `plugins/SkyeBlock/warps.yml`.

## Island Templates

16 bundled templates are extracted from the plugin JAR on first startup:

**Easy:** vanilla, bare_bones, campsite, cozy_grove, mineshaft
**Medium:** desert, fishermans_paradise, inverted, orchid
**Hard:** advanced, igloo, nether_jail, olympus, sandy_isle, wilson
**Nether:** nether_generic

To add custom templates, place `.schem` files (WorldEdit format) in `plugins/SkyeBlock/schematics/` and add a mapping entry in `config.yml` under `island.templates`.

## Building

```bash
mvn clean package
```

The built JAR includes all dependencies (H2 database, ASWM file loader) shaded in.

## Development

- Language: Java 21 (bytecode target 21)
- Build system: Maven
- Main class: `skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin`
- Database: H2 2.3.232 (embedded, file-based)
- Source structure: `src/main/java/` for code, `src/main/resources/` for configs and schematics

## License

Licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).
