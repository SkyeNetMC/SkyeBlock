# Changelog

All notable changes to this project will be documented in this file.

## [1.21.10]
- Enforce container/workstation access via per-island permissions and visitor settings (blocks external plugins like sell wands on others' chests).
- Permission-aware inventory open/interaction handling integrated with IslandPermissionManager.

## [1.21.10-dev] - 2025-12-20
- Updated for Minecraft 1.21.10 API compatibility and opened the 1.21.10-dev line.
- Lowered Java target to 17 for wider host compatibility.
- Added gamerule access checks and new permission bundles (`skyeblock.player`, `skyeblock.settings.gamerules`), with GUI filtering.
- Added `/mobspawning` command registration to plugin.yml.
- Marked WorldGuard and ProtocolLib as soft dependencies; added DEPENDENCIES.md for setup guidance.
- Added new schematic files for multiple environments.
- Allowed admins to bypass visiting-disabled checks when appropriate.
- Build/Maven updates to align with 1.21.10 APIs.

## [1.21.4]
- Earlier 1.21.4 work predates this changelog; see git history for details.
