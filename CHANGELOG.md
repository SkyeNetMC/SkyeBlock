# Changelog

All notable changes to this project will be documented in this file.

## [3.2.0-1.21.10] - 2025-12-26
- Build now targets Java 21 for 1.21.x dependency compatibility.
- CI/CD: GitHub Actions builds on pushes/PRs, publishes releases on `v*` tags, and maintains a rolling `main-latest` prerelease with auto-generated release notes.
- Packaging: resource filtering adjusted so `.schem` files are included correctly.
- Visitor protection: fixed ProtocolLib packet handling for block/container interaction restrictions.
- Docs/config: added/expanded permission documentation and configuration migration support.

## [1.21.10 up2]
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
