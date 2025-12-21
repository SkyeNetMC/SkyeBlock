# SkyeBlock Plugin Dependencies

## Required Dependencies (Hard)
These plugins **must** be installed for SkyeBlock to load:

1. **WorldEdit** - Required for island schematic management
   - Download: https://dev.bukkit.org/projects/worldedit
   - Version: 7.3.6 or newer
2. **Vault** - Required for economy and permission integration
   - Download: https://www.spigotmc.org/resources/vault.34315/
   - Version: 1.7.3 or newer
## Optional Dependencies (Soft)
These plugins are optional but recommended for full functionality:

1. **WorldGuard** - Used for region protection
   - Download: https://dev.bukkit.org/projects/worldguard
   - Version: 7.0.11 or newer
   - Note: Some protection features may not work without this

2. **ProtocolLib** - Used for visitor packet-level restrictions
   - Download: https://www.spigotmc.org/resources/protocollib.1997/
   - Version: 5.3.0 or newer
   - Note: Advanced visitor protection requires this

3. **LuckPerms** - Used for permission management 
   - Download: https://luckperms.net/download
   - Version: 5.4 or newer

4. **SlimeWorldManager / AdvancedSlimeWorldManager** - Used for world management
   - Download: https://www.spigotmc.org/resources/advanced-slimeworldmanager.87209/
   - Note: Alternative world management system

## Installation Order
1. Install WorldEdit first
2. Install optional dependencies (WorldGuard, ProtocolLib, LuckPerms)
3. Install SkyeBlock
4. Restart server

## Server Requirements
- **Minecraft Version**: 1.21.10
- **Server Type**: Paper, Spigot, or compatible fork
- **Java Version**: 17 or newer

## Troubleshooting
If you see errors about missing dependencies:
- Make sure WorldEdit is installed (required)
- Check that all plugins are compatible with Minecraft 1.21.10
- Ensure plugins are in the `plugins/` folder
- Restart the server after adding dependencies
