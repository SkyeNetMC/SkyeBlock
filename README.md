# SkyeBlock

Skyblock plugin for Paper/Spigot (1.21.10 tested) with isolated island worlds, GUI-driven settings, and granular permissions. Dual command style (`/island` and `/sb …`) keeps legacy players happy while giving admins structured subcommands.

## Highlights
- Island per player with optional SlimeWorldManager/ASWM storage; hub + spawn helpers.
- GUI settings for islands (gamerules, visiting, deletion guard) with live apply.
- Multiple starter templates (classic, desert, nether) plus drop-in schematics.
- LuckPerms-friendly permissions, split into player base, safe gamerules, and full admin bypass.
- Nether island support with biome control and safe teleports.

## Requirements
- Paper/Spigot 1.21.x
- Java 17+
- Optional: SlimeWorldManager/AdvancedSlimeWorldManager, WorldEdit (dep), LuckPerms.

## Install / Build
```bash
mvn clean package
cp target/skyeblock-*.jar /path/to/server/plugins/
```
Start the server once to generate configs in `plugins/SkyeBlock/`.

## Core Commands
- `/island create <type>` – create an island (classic/desert/nether)
- `/island home` – go to your island
- `/island settings` – open settings GUI (gamerules/visiting/delete)
- `/visit <player>` – visit another island
- `/hub` and `/spawn` – quick travel
- `/sb island …` – subcommand equivalents; `/sb` shows help
- Admin: `/sba …`, `/delete [player]`, `/island list`, `/island status`

## Permissions (key groups)
- `skyeblock.player` – default player bundle (island basics, hub/spawn, warp) **no gamerules**.
- `skyeblock.settings.gamerules` – safe/common gamerules (daylight/weather cycle, keepinventory, mobgriefing/spawn, fire/fall/fire/drown damage, insomnia, immediate respawn, natural regen, announce advancements, elytra check).
- `skyeblock.gamerule.*` – all gamerules; `skyeblock.gamerules.adminbypass` overrides checks.
- `skyeblock.*` – everything (op).

Individual gamerule nodes mirror Bukkit names (e.g., `skyeblock.gamerule.dodaylightcycle`, `skyeblock.gamerule.doweathercycle`, `skyeblock.gamerule.randomtickspeed`).

## Gamerule & Settings GUI
- Access via `/island settings` → main menu → Gamerule Settings.
- Only gamerules you have permission for are shown.
- Boolean rules toggle; numeric rules adjust via clicks/input; applied instantly to the island world.

## Configuration
Generated files live in `plugins/SkyeBlock/`:
- `config.yml` – world names, hub, nether toggle/biome, distances.
- `island-settings.yml` – per-island gamerule values.
- `warps.yml` – warp definitions.
- `schematics/` – bundled island templates; add your own `.schem` files here.

## Island Templates
- Built-in: classic, desert, nether (see `src/main/resources/schematics/`).
- To add: drop a `.schem` into `plugins/SkyeBlock/schematics/`, then reference it via your island type config.

## Development Notes
- Language: Java 17; build: Maven.
- Main entry: `skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin`.
- Primary configs/resourceds under `src/main/resources/`.

## Contributing / Support
PRs and issues welcome. For perms: start players with `skyeblock.player`, grant `skyeblock.settings.gamerules` for safe tweaks, reserve `skyeblock.gamerule.*`/`adminbypass` for staff.


## Server Owners
Ensure dependencies (WorldGuard, ProtocolLib) are installed for full functionality. Adjust permissions via your chosen manager (e.g., LuckPerms) to suit your server's needs.
For autoupdating you can use a plugin like
[PluginUpdater](https://modrinth.com/plugin/plugin-updater) which you can find more info [here](https://github.com/OakLoaf/PluginUpdater/wiki/PluginUpdater-Plugin#configuring-pluginupdater)

## License
Licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0). See LICENSE file for details.
