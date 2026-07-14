# SkyeBlock Changelog

## 3.3.3 (2026-07-13)

### MC 26.2 & ASP 4.0.0 Support
- Updated Paper API to `26.2.build.60-beta`
- Updated `plugin.yml` api-version to `26.2`
- Integrated Advanced Slime World Manager (ASWM) API 4.0.0
  - Two-stage world loading: `readWorld`/`createEmptyWorld` (async) -> `loadWorld` (sync)
  - `FileLoader` shading (package `com.infernalsuite.asp.loaders`)
  - `LegacySWMLoaderWrapper` for backward compatibility
- Java 21 bytecode target (compatible with Java 25 runtime on MC 26.2)

### Schematic Bundling
- Schematics are now bundled inside the plugin JAR under `resources/schematics/`
- `SchematicManager` auto-extracts bundled schematics to the data folder on startup
- 16 bundled schematics: advanced, bare_bones, campsite, cozy_grove, desert, fishermans_paradise, igloo, inverted, mineshaft, nether_generic, nether_jail, olympus, orchid, sandy_isle, vanilla, wilson

### H2 Database Storage (replaces YAML)
- Added embedded H2 `2.3.232` database (`skyeblock.db` in plugin data folder)
- New `DatabaseManager` class handles all H2 connection, schema, and CRUD operations
- **Island data** (`islands`, `island_visitor_permissions`, `island_coop_members`, `island_pending_invites`, `island_votes`, `island_custom_permissions`) — replaces `island-data.yml`
- **Island settings** (`island_settings`) — replaces `island-settings.yml`
- **Player data** (`player_data`) — replaces `player-data.yml`
- Transactional saves with rollback on failure
- `MERGE INTO` for upserts on single-row updates
- Old YAML files are now ignored; data is written to `skyeblock.db`

### Changes
- `/is` now teleports to your island instead of opening settings (use `/is settings` for settings)
- Rewrote README with full feature, command, and permission reference
- Added GitHub Actions workflow for Modrinth alpha publishing
- Version bumped to 3.3.3
