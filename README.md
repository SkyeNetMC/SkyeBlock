# SkyeBlock Plugin

A comprehensive SkyeBlock plugin for Paper/Spigot servers that allows players to create and manage their own skyblock islands with advanced features and a flexible dual command system.

## Features

### Core Functionality
- **🎯 Dual Command System**: Use both direct commands (`/island`, `/visit`) and unified sub-commands (`/sb island`, `/sb visit`) - **NEW!**
- **🏝️ Multiple Island Types**: Choose from Classic, Desert, or Nether-themed islands with clean single-word identifiers
- **📋 Custom Schematic System**: YAML-based island templates with detailed block placement and chest contents
- **🌍 Individual Island Worlds**: Each island gets its own dedicated world (with SlimeWorldManager support)
- **⚙️ Island Settings Management**: Full gamerule control system with interactive GUI interface
- **🔐 Advanced Permissions**: LuckPerms-style permission system for granular gamerule control
- **🏠 Hub Integration**: Seamless teleportation between hub and islands
- **👥 Visitor System**: Visit other players' islands with protection and settings
- **🤝 Cooperative Play**: Island cooperation system with member management

### Island Types Available
1. **🌿 Classic SkyBlock** (`classic`) - Traditional dirt platform with tree, chest, and basic supplies
2. **🏜️ Desert Island** (`desert`) - Sand-based island with cactus and desert survival items  
3. **🔥 Nether Island** (`nether`) - Challenging netherrack island with nether-themed resources

### Advanced Features
- **⚡ Dual Command Interface**: Choose between direct commands (`/island`) or unified sub-commands (`/sb island`) with full backward compatibility
- **🚀 Smart World Management**: SlimeWorldManager integration with fallback to standard Bukkit worlds
- **🎮 Island Settings GUI**: Interactive interface for managing 31 different gamerules
- **🔑 Granular Permissions**: Individual permissions for each gamerule with admin bypass system
- **🎯 Single-Word Island Types**: Clean, simple island type identifiers (`classic`, `desert`, `nether`)
- **📝 Smart Tab Completion**: Intelligent command completion for all commands and island types
- **🛠️ Comprehensive Admin Tools**: Island listing, deletion, status monitoring, and management
- **🌐 Safe Teleportation**: Proper spawn point calculation and world-specific teleportation
- **🎨 MiniMessage Support**: Modern text formatting with Adventure API integration
- **💾 Optimized Storage**: SlimeWorld integration for reduced disk usage and better performance

## Commands

### 🎯 Dual Command System (NEW!)
The plugin supports both direct commands and unified sub-commands for maximum flexibility:

#### Direct Commands (Original)
```bash
/island create <type>        # Create a new island (classic, desert, nether)
/island home                 # Teleport to your island
/island settings             # Open island settings GUI
/island types                # Show available island types
/island delete               # Delete your island with confirmation
/island list                 # List all islands (admin only)
/island status               # Show server status (admin only)

/visit <player>              # Visit another player's island
/delete [player]             # Delete island with two-step confirmation
/hub                         # Return to hub world
/is                          # Alias for /island
```

#### Sub-Commands (NEW!)
```bash
/sb island create <type>     # Create a new island (classic, desert, nether)
/sb island home              # Teleport to your island
/sb island settings          # Open island settings GUI
/sb island types             # Show available island types
/sb island delete            # Delete your island with confirmation
/sb island list              # List all islands (admin only)
/sb island status            # Show server status (admin only)

/sb visit <player>           # Visit another player's island
/sb delete [player]          # Delete island with two-step confirmation
/sb hub                      # Return to hub world
/skyblock                    # Alias for /sb
```

#### Help & Information
```bash
/sb                          # Show all available sub-commands with descriptions
/island help                 # Show island command help
```

### 🔑 Permission-Based Commands
Commands are filtered based on permissions:
- **Basic Users**: `create`, `home`, `settings`, `types`, `delete` (own island)
- **Admins**: All commands including `list`, `status`, and delete others' islands

## Installation & Setup

### 1. Prerequisites
- **Paper/Spigot server** (1.20+)
- **Optional**: SlimeWorldManager (ASWM/SWM) for optimized world management
- **Optional**: LuckPerms for advanced gamerule permissions

### 2. Quick Installation
```bash
# Clone or download the plugin
git clone <repository-url>
cd SkyeBlock

# Build the plugin
./build.sh

# Copy to your server
cp target/skyeblock-1.0.0.jar /path/to/your/server/plugins/

# Restart your server
```

### 3. Optional Enhancements
- **Install SlimeWorldManager** for individual island worlds (highly recommended)
- **Install LuckPerms** for granular gamerule permission control
- Both plugins provide automatic fallback if not available

### 4. Quick Test & Validation
```bash
# Test the installation
./test-setup.sh

# Validate dual command system
./validate-dual-commands.sh

# Test all functionality
./test-functionality.sh
```

### 5. Ready to Use!
- Plugin auto-creates necessary configurations
- Includes built-in island templates
- Works out-of-the-box with smart defaults

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

### 🎯 Dual Command System Usage
Both command styles work identically - choose what feels natural!

```bash
# Creating Islands - Both methods work:
/island create classic       ↔️    /sb island create classic
/island create desert        ↔️    /sb island create desert  
/island create nether        ↔️    /sb island create nether

# Navigation - Both methods work:
/island home                 ↔️    /sb island home
/visit PlayerName            ↔️    /sb visit PlayerName
/hub                         ↔️    /sb hub

# Management - Both methods work:
/island settings             ↔️    /sb island settings
/island types                ↔️    /sb island types
/delete                      ↔️    /sb delete

# Using Aliases:
/is home                     ↔️    /skyblock island home
```

### 🏝️ Creating Islands
```bash
# Simple island creation
/island create classic
/island create desert
/island create nether

# Check available types first
/island types
```

### ⚙️ Managing Island Settings
```bash
# Open the interactive GUI
/island settings

# Players see gamerules based on their permissions
# Staff with adminbypass see all 31 gamerules
```

### 🛠️ Admin Management
```bash
# List all islands with details
/island list

# Check server and world manager status
/island status

# Delete any player's island (admin only)
/island delete PlayerName
```

### 🎮 Getting Help
```bash
# Show sub-command help
/sb

# Show island command help  
/island help
```

## 📚 Documentation

### Core Guides
- **`DUAL_COMMAND_SYSTEM.md`** - Complete guide to the new dual command system
- **`LUCKPERMS_GAMERULE_PERMISSIONS.md`** - LuckPerms gamerule permission setup
- **`ASWM_INTEGRATION_GUIDE.md`** - SlimeWorldManager integration details
- **`ISLAND_SETTINGS_IMPLEMENTATION.md`** - Island settings and GUI system

### Technical Documentation
- **`GAMERULE_PERMISSIONS_UPDATE.md`** - Technical implementation details
- **`DEPLOYMENT_GUIDE.md`** - Server deployment and debugging information
- **`TESTING.md`** - Testing procedures and expected results
- **`PROJECT_COMPLETE.md`** - Complete feature overview and status

### Quick References
- **`DEPLOYMENT_CHECKLIST.md`** - Pre-deployment validation checklist
- **`COMMAND_TESTING.md`** - Command testing procedures
- **`MINIMESSAGE_TESTING.md`** - Text formatting validation

## 🚀 Recent Updates

### ✨ Version 1.0.0 - Latest Release (May 2025)

#### 🎯 NEW: Dual Command System
- **Direct Commands**: `/island`, `/visit`, `/delete`, `/hub` (preserves existing usage)
- **Sub-Commands**: `/sb island`, `/sb visit`, `/sb delete`, `/sb hub` (new unified interface)
- **Full Backward Compatibility**: Existing users can continue using familiar commands
- **Smart Help System**: `/sb` shows all available sub-commands with descriptions
- **Complete Tab Completion**: Both command styles support intelligent tab completion

#### 🏝️ Enhanced Island Management
- **Single-Word Island Types**: Clean identifiers (`classic`, `desert`, `nether`)
- **Fixed Island Type Storage**: Islands now store single-word types instead of display names
- **Improved Tab Completion**: Smart completion for island types and commands
- **Two-Step Deletion**: Enhanced confirmation system for island deletion

#### 🔧 Core System Improvements
- **SlimeWorldManager Integration**: Individual island worlds with automatic fallback
- **LuckPerms Permission System**: Granular control over 31 island gamerules
- **Interactive Settings GUI**: Real-time gamerule management with visual indicators
- **MiniMessage Support**: Modern text formatting with Adventure API
- **Optimized Performance**: Better world management and resource cleanup

#### 🎮 Quality of Life Features
- **Admin Bypass System**: Staff can access all gamerules regardless of restrictions
- **Dynamic Permission Filtering**: GUI shows only accessible gamerules per player
- **Comprehensive Admin Tools**: Enhanced monitoring and management capabilities
- **Safe World Operations**: Proper cleanup and resource management
- **Hub System Integration**: Configurable hub world with seamless teleportation

## 💡 Support & Contributing

### Getting Help
- Check the comprehensive documentation in the repository
- Use the testing scripts to validate your setup
- Review the configuration examples for common setups

### Reporting Issues
- Include server version and plugin configuration
- Provide relevant console logs and error messages
- Test with the included validation scripts

### Development
- Plugin follows modern Bukkit/Paper development practices
- Uses Adventure API for text components and MiniMessage formatting
- Includes comprehensive testing suite and validation tools
- Supports both SlimeWorldManager and standard Bukkit worlds

### Quality Assurance
- ✅ **Full Test Coverage**: Comprehensive testing scripts included
- ✅ **Validation Tools**: Multiple validation scripts for different components
- ✅ **Backward Compatibility**: Maintains compatibility with existing setups
- ✅ **Performance Optimized**: Efficient world management and resource usage

---

## 📄 License

This plugin is developed for SkyeNetwork. All rights reserved.

**SkyeBlock Plugin v1.0.0** - A comprehensive island management solution with dual command system, advanced permissions, and modern Minecraft server integration.
