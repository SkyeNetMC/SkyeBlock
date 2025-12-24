# SkyeBlock Permissions

This document lists all permissions defined in the plugin and the defaults shipped in `plugin.yml`.

## Roles / Bundles

| Role / Node | Includes | Default |
| --- | --- | --- |
| `skyeblock.*` | Everything in this file | `op` |
| `skyeblock.player` | All player commands and warps (no gamerule admin bypass) | `true` |
| `skyeblock.admin` | Admin commands (/sba), high-level management | `op` |

## Core Commands

| Command | Permission | Default |
| --- | --- | --- |
| `/sb`, `/island`, `/is` | `skyeblock.island` | `true` |
| `/visit` | `skyeblock.island.visit` | `true` |
| `/island vote` | `skyeblock.island.vote` | `true` |
| `/island edit` (title/desc/icon) | `skyeblock.island.edit` | `true` |
| `/island coop` | `skyeblock.island.coop` | `true` |
| `/island lock` / `unlock` | `skyeblock.island.lock` | `true` |
| `/island set home|visit` | `skyeblock.island.set` | `true` |
| `/delete` (self delete) | `skyeblock.island.delete` | `true` |
| `/islandpermissions` | `skyeblock.island.permissions` | `true` |
| `/hub` | `skyeblock.hub` | `true` |
| `/spawn` | `skyeblock.spawn` | `true` |
| `/warp` | `skyeblock.warp` | `true` |
| `/createisland` | `skyeblock.island.create` | `true` |

## Warp Permissions

| Permission | Use | Default |
| --- | --- | --- |
| `skyeblock.warp.spawn` | Spawn warp | `true` |
| `skyeblock.warp.nether` | Nether resource world warp | `true` |
| `skyeblock.warp.end` | End resource world warp | `true` |
| `skyeblock.warp.pvp` | PvP arena warp | `true` |
| `skyeblock.warp.shop` | Shop/market warp | `true` |
| `skyeblock.admin.warp` | Admin warp management (/warpadmin) | `op` |

## Admin / Staff

| Command | Permission | Default |
| --- | --- | --- |
| `/sba ...` (admin suite) | `skyeblock.admin` | `op` |
| `/convertislands` | `skyeblock.admin.convert` | `op` |
| `/warpadmin ...` | `skyeblock.admin.warp` | `op` |
| `/mobspawning ...` | `skyeblock.admin.mobspawning` | `op` |

## Gamerule Access

- Global bypass: `skyeblock.gamerules.adminbypass` (see all gamerules) — default `op`.
- Common bundle: `skyeblock.settings.gamerules` — default `false`, grants the listed gamerules below.
- Full bundle: `skyeblock.gamerule.*` — default `true` (inherits all individual gamerule nodes).

### Individual Gamerule Nodes (all default `true` unless overridden)
`skyeblock.gamerule.dodaylightcycle`, `skyeblock.gamerule.doweathercycle`, `skyeblock.gamerule.keepinventory`, `skyeblock.gamerule.mobgriefing`, `skyeblock.gamerule.domobspawning`, `skyeblock.gamerule.dofiretick`, `skyeblock.gamerule.falldamage`, `skyeblock.gamerule.firedamage`, `skyeblock.gamerule.drowningdamage`, `skyeblock.gamerule.doinsomnia`, `skyeblock.gamerule.doimmediaterespawn`, `skyeblock.gamerule.announceadvancements`, `skyeblock.gamerule.disableelytraMovementcheck`, `skyeblock.gamerule.dolimitedcrafting`, `skyeblock.gamerule.naturalregeneration`, `skyeblock.gamerule.reduceddebuginfo`, `skyeblock.gamerule.sendcommandfeedback`, `skyeblock.gamerule.showdeathmessages`, `skyeblock.gamerule.doentitydrops`, `skyeblock.gamerule.dotiledrops`, `skyeblock.gamerule.domobloot`, `skyeblock.gamerule.dopatrolspawning`, `skyeblock.gamerule.dotraderSpawning`, `skyeblock.gamerule.forgivedeadplayers`, `skyeblock.gamerule.universalanger`, `skyeblock.gamerule.randomtickspeed`, `skyeblock.gamerule.spawnradius`, `skyeblock.gamerule.maxentitycramming`, `skyeblock.gamerule.maxcommandchainlength`, `skyeblock.gamerule.playerssleepingpercentage`.

## Quick Assignment Examples

- **Default players**: grant `skyeblock.player` (already default `true`).
- **Trusted players with gamerules**: grant `skyeblock.settings.gamerules` or specific `skyeblock.gamerule.*` entries.
- **Admins**: grant `skyeblock.admin` (includes /sba) plus `skyeblock.admin.warp` and `skyeblock.admin.mobspawning` as needed.
- **Everything**: grant `skyeblock.*` (OPs have this by default).

## Source of Truth

All permission definitions are declared in [src/main/resources/plugin.yml](src/main/resources/plugin.yml).