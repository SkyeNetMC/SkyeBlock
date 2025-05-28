# SkyeBlock Plugin

A comprehensive SkyeBlock plugin for Paper/Spigot servers that allows players to create and manage their own skyblock islands.

## Features

### Core Functionality
- **Multiple Island Types**: Choose from Classic, Desert, or Nether-themed islands with multi-word support
- **Custom Schematic System**: YAML-based island templates with detailed block placement
- **Individual Island Worlds**: Each island gets its own dedicated world (with SlimeWorldManager support)
- **Island Settings Management**: Full gamerule control system with GUI interface
- **Advanced Permissions**: LuckPerms-style permission system for granular gamerule control
- **Hub Integration**: Seamless teleportation between hub and islands

### Island Types Available
1. **Classic SkyBlock** - Traditional dirt platform with tree, chest, and basic supplies
2. **Desert Island** - Sand-based island with cactus and desert survival items  
3. **Nether Island** - Challenging netherrack island with nether-themed resources

### Advanced Features
- **Smart World Management**: SlimeWorldManager integration with fallback to standard Bukkit worlds
- **Island Settings GUI**: Interactive interface for managing 31 different gamerules
- **Granular Permissions**: Individual permissions for each gamerule with admin bypass system
- **Multi-Word Island Types**: Support for island types with spaces (e.g., "Desert Island")
- **Smart Tab Completion**: Intelligent command completion for all subcommands and island types
- **Comprehensive Admin Tools**: Island listing, deletion, status monitoring, and management
- **Safe Teleportation**: Proper spawn point calculation and world-specific teleportation

## Commands

### Player Commands
- `/island create [type]` - Create a new island (supports multi-word types: "Classic SkyBlock", "Desert Island", "Nether Island")
- `/island types` - Show available island types with descriptions
- `/island tp` / `/island teleport` / `/island home` - Teleport to your island
- `/island settings` - Open island gamerule settings GUI
- `/island help` - Show help message

### Admin Commands (Requires `skyeblock.admin`)
- `/island delete` - Delete your island with proper cleanup
- `/island list` - List all created islands with details
- `/island status` - Show server status and world manager information

### Hub Commands
- `/hub` - Teleport to the hub world (if hub is enabled)

## Installation

1. **Prerequisites**:
   - Paper/Spigot server (1.20+)
   - (Optional) SlimeWorldManager for optimized world management
   - (Optional) LuckPerms for advanced gamerule permissions

2. **Building**:
   ```bash
   ./build.sh
   ```

3. **Installation**:
   - Copy `target/skyeblock-1.0.0.jar` to your server's `plugins/` folder
   - Restart your server
   - Plugin includes built-in island templates and void world generator

4. **Optional Enhancements**:
   - Install **SlimeWorldManager** (SWM/ASWM) for individual island worlds
   - Install **LuckPerms** for granular gamerule permission control
   - Both plugins provide automatic fallback if not available

5. **Quick Test**:
   ```bash
   ./test-setup.sh
   ```

## Configuration

The plugin creates a `config.yml` file with the following settings:

```yaml
# World settings
world:
  name: "skyblock_world"
  environment: "NORMAL"

# Hub settings (optional)
hub:
  enabled: true
  world: "world"
  spawn:
    x: 0
    y: 100
    z: 0

# Island settings  
island:
  distance: 1000             # Distance between islands (for fallback shared world)
  start-x: 0                # Starting X coordinate
  start-z: 0                # Starting Z coordinate

# Messages (fully customizable)
messages:
  prefix: "&8[&6SkyeBlock&8] &r"
  island-created: "&aIsland created successfully! Teleporting you there..."
  island-already-exists: "&cYou already have an island!"
  island-not-found: "&cYou don't have an island yet!"
  island-deleted: "&aYour island has been deleted!"
  teleported: "&aTeleported to your island!"
  no-permission: "&cYou don't have permission to use this command!"
  invalid-command: "&cInvalid command! Use /island help for available commands."
  # ... additional customizable messages
```

## Island Settings System

### Gamerule Management
The plugin features a comprehensive gamerule management system accessible via `/island settings`:

### Available Gamerules
**Survival & Damage Control:**
- Keep Inventory, Fall Damage, Fire Damage, Drowning Damage
- Natural Regeneration, Immediate Respawn

**World Mechanics:**
- Day/Light Cycle, Weather Cycle, Fire Spread
- Mob Spawning, Mob Griefing, Insomnia (Phantoms)

**Game Features:**
- Announce Advancements, Show Death Messages
- Entity/Tile Drops, Mob Loot, Limited Crafting

**Technical Settings:**
- Random Tick Speed, Spawn Radius
- Max Entity Cramming, Command Chain Length
- Players Sleeping Percentage

### GUI Interface
- **Interactive Menus**: Click-based interface for easy gamerule management
- **Visual Indicators**: Clear on/off states for boolean gamerules
- **Value Input**: Chat-based input for integer gamerules
- **Permission Awareness**: Only shows gamerules the player has permission to modify
- **Real-time Application**: Changes apply immediately to the island world

### Permission System
- **Default Access**: All gamerules visible to island owners by default
- **LuckPerms Integration**: Use LuckPerms to hide specific gamerules
- **Admin Bypass**: Staff can access all gamerules regardless of restrictions
- **Granular Control**: Individual permissions for each of the 31 supported gamerules

## Custom Island Templates

The plugin includes a sophisticated YAML-based schematic system. Island templates are stored in `src/main/resources/schematics/` and include:

### Template Structure
```yaml
name: "Island Name"
description: "Island description"
size:
  width: 9
  height: 5  
  length: 9
spawn_offset:
  x: 4
  y: 1
  z: 4
structure:
  - level: 0
    blocks:
      - "air air dirt dirt dirt air air air"
      # ... more block rows
chest_contents:
  - location: [4, 2, 3]
    items:
      - "ice:2"
      - "lava_bucket:1"
```

### Creating Custom Templates
1. Create a new YAML file in the schematics format
2. Add it to `src/main/resources/schematics/`
3. Update `CustomSchematicManager.java` to load your new template
4. Rebuild the plugin

# Messages (customizable)
messages:
  prefix: "&8[&6SkyeBlock&8] &r"
  island-created: "&aIsland created successfully! Teleporting you there..."
  # ... more messages
```

## Creating Island Templates

1. Build your island template in a world
2. Select the area with WorldEdit wand (`//wand`)
3. Copy the selection (`//copy`)
4. Save as schematic (`//schem save island-normal`)
5. Move the schematic file to `plugins/SkyeBlock/schematics/`

## Permissions

### Basic Permissions
- `skyeblock.*` - All permissions (default: op)
- `skyeblock.island` - Basic island commands (default: true)
- `skyeblock.hub` - Hub teleport command (default: true)
- `skyeblock.admin` - Admin commands (default: op)

### Gamerule Permissions (LuckPerms Integration)
- `skyeblock.gamerules.adminbypass` - Bypass all gamerule restrictions (default: op)
- `skyeblock.gamerule.*` - All individual gamerule permissions (default: true)

### Individual Gamerule Controls
The plugin provides granular control over 31 different gamerules:

**Boolean Gamerules:**
- `skyeblock.gamerule.keepinventory` - Keep inventory on death
- `skyeblock.gamerule.mobgriefing` - Mob block destruction
- `skyeblock.gamerule.dodaylightcycle` - Day/night cycle
- `skyeblock.gamerule.doweathercycle` - Weather changes
- `skyeblock.gamerule.domobspawning` - Natural mob spawning
- `skyeblock.gamerule.dofiretick` - Fire spread
- `skyeblock.gamerule.falldamage` - Fall damage
- `skyeblock.gamerule.firedamage` - Fire damage
- `skyeblock.gamerule.drowningdamage` - Drowning damage
- And 16 more boolean gamerules...

**Integer Gamerules:**
- `skyeblock.gamerule.randomtickspeed` - Random tick speed
- `skyeblock.gamerule.spawnradius` - Spawn radius
- `skyeblock.gamerule.maxentitycramming` - Entity cramming limit
- `skyeblock.gamerule.maxcommandchainlength` - Command chain limit
- `skyeblock.gamerule.playerssleepingpercentage` - Sleep percentage

### LuckPerms Usage Examples
```bash
# Hide dangerous gamerules from default players
lp group default permission set skyeblock.gamerule.mobgriefing false
lp group default permission set skyeblock.gamerule.dofiretick false

# Allow VIP players access to restricted gamerules
lp group vip permission set skyeblock.gamerule.mobgriefing true

# Restrict technical gamerules to staff only
lp group default permission set skyeblock.gamerule.randomtickspeed false
lp group moderator permission set skyeblock.gamerule.randomtickspeed true
```

> **Note**: All gamerules are visible by default. Set permissions to `false` to hide specific gamerules from players.

## Technical Details

### World Management
- **SlimeWorldManager Integration**: Automatic detection and use of SWM/ASWM for optimized world handling
- **Individual Island Worlds**: Each island gets its own world when SlimeWorldManager is available
- **Fallback Support**: Graceful fallback to standard Bukkit worlds if SlimeWorldManager is not installed
- **World Cleanup**: Proper world deletion and resource cleanup on island deletion

### Island Management
- **Island Format**: `island-<type>-<player-uuid>` for unique identification
- **Safe Teleportation**: Smart spawn point calculation with template-based positioning
- **Schematic Support**: Built-in YAML-based schematic system with chest population
- **Settings Persistence**: Individual gamerule settings saved per island

### GUI System
- **Interactive Settings**: Full GUI interface for gamerule management
- **Permission Integration**: Dynamic gamerule visibility based on LuckPerms permissions
- **Real-time Updates**: Live updates when gamerule values are changed
- **Admin Bypass**: Staff can see all gamerules regardless of individual restrictions

### Performance Features
- **Optimized World Loading**: Efficient world creation and management
- **Memory Management**: Proper cleanup and resource management
- **Compressed Storage**: SlimeWorld integration for reduced disk usage
- **Async Operations**: Non-blocking world operations where possible

## Usage Examples

### Creating Islands
```bash
# Create a classic island
/island create classic

# Create a desert island (multi-word support)
/island create Desert Island

# Create with mixed case (case-insensitive)
/island create NETHER ISLAND
```

### Managing Island Settings
```bash
# Open the settings GUI
/island settings

# Players can modify gamerules they have permission for
# Staff with adminbypass can see all gamerules
```

### Admin Management
```bash
# List all islands
/island list

# Check server status
/island status

# Delete an island (as owner or admin)
/island delete
```

## Documentation

- **`LUCKPERMS_GAMERULE_PERMISSIONS.md`** - Complete guide for LuckPerms gamerule permission setup
- **`GAMERULE_PERMISSIONS_UPDATE.md`** - Technical implementation details
- **`DEPLOYMENT_GUIDE.md`** - Server deployment and debugging information
- **`TESTING.md`** - Testing procedures and expected results

## Recent Updates

### Version 1.0.0 - Latest Features
- ✅ **LuckPerms Gamerule Permissions**: Complete permission system for 31 gamerules with admin bypass
- ✅ **Individual Island Worlds**: Each island gets its own world with SlimeWorldManager integration
- ✅ **Interactive Settings GUI**: Full interface for managing island gamerules with real-time updates
- ✅ **Multi-Word Island Types**: Support for "Desert Island", "Nether Island" with smart tab completion
- ✅ **Advanced World Management**: Automatic SlimeWorldManager detection with Bukkit fallback
- ✅ **Comprehensive Admin Tools**: Status monitoring, island listing, and enhanced management commands
- ✅ **MiniMessage Integration**: Modern text formatting with Adventure API support
- ✅ **Hub System**: Configurable hub world with seamless teleportation

### Core Features
- **31 Configurable Gamerules**: Full control over island world mechanics
- **3 Island Templates**: Classic, Desert, and Nether islands with custom schematics
- **Permission-Based Access**: Granular control using LuckPerms-style permissions
- **Safe World Management**: Proper cleanup and resource management
- **Intelligent Fallbacks**: Works with or without optional dependencies

## Support

For issues, questions, or contributions, please contact the SkyeNetwork development team.

## License

This plugin is developed for SkyeNetwork. All rights reserved.
