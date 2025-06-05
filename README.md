# SkyeBlock Plugin

A comprehensive SkyeBlock plugin for Paper/Spigot servers that allows players to create and manage their own skyblock islands with advanced features and a flexible dual command system.

## 📋 Table of Contents

- [Features](#features)
  - [Core Functionality](#core-functionality)
  - [Island Types Available](#island-types-available)
  - [Advanced Features](#advanced-features)
- [Commands](#commands)
  - [🎯 Dual Command System](#-dual-command-system-new)
  - [Direct Commands (Original)](#direct-commands-original)
  - [Sub-Commands (NEW!)](#sub-commands-new)
  - [Help & Information](#help--information)
  - [🔑 Permission-Based Commands](#-permission-based-commands)
- [Installation & Setup](#installation--setup)
- [Configuration](#configuration)
- [Island Settings System](#island-settings-system)
  - [Gamerule Management](#gamerule-management)
  - [Available Gamerules](#available-gamerules)
  - [GUI Interface](#gui-interface)
  - [Permission System](#permission-system)
- [Custom Island Templates](#custom-island-templates)
- [Permissions](#permissions)
  - [Basic Permissions](#basic-permissions)
  - [Gamerule Permissions (LuckPerms Integration)](#gamerule-permissions-luckperms-integration)
  - [Individual Gamerule Controls](#individual-gamerule-controls)
  - [LuckPerms Usage Examples](#luckperms-usage-examples)
- [Technical Details](#technical-details)
- [Usage Examples](#usage-examples)
- [📚 Documentation](#-documentation)
- [🚀 Recent Updates](#-recent-updates)
- [💡 Support & Contributing](#-support--contributing)
- [📄 License](#-license)

## Features

### Core Functionality
- **🎯 Dual Command System**: Use both direct commands (`/island`, `/visit`) and unified sub-commands (`/sb island`, `/sb visit`) - **NEW!**
- **🏝️ Multiple Island Types**: Choose from Classic, Desert, or Nether-themed islands with clean single-word identifiers
- **🌋 Nether Island System**: Dedicated nether void worlds with proper biome management and nether-specific templates - **NEW!**
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
3. **🔥 Nether Island** (`nether`) - Challenging nether-themed island with netherrack, soul sand, and blackstone in dedicated nether void worlds

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
- **🌋 Nether Island Support**: Dedicated nether void worlds with biome setting and nether-specific templates
- **🔥 Multi-Environment Islands**: Support for both overworld and nether island environments

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

# Nether world settings (NEW!)
nether:
  name: "skyblock_nether"
  environment: "NETHER"
  biome: "NETHER_WASTES"
  enabled: true

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

# Optional: Specify world type and biome (for nether islands)
world: "skyblock_nether"
biome: "NETHER_WASTES"

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

## 🌋 Nether Island System

SkyeBlock now features comprehensive Nether Island support with dedicated nether void worlds and biome management.

### 🔥 Nether Island Features
- **Dedicated Nether Worlds**: Each nether island gets its own void nether world environment
- **Automatic Biome Setting**: Islands are automatically set to `NETHER_WASTES` biome
- **Nether-Specific Template**: Custom schematic with blackstone, netherrack, and soul sand
- **Starter Kit Integration**: Chest filled with nether survival essentials
- **World Environment**: Proper NETHER environment with nether mechanics

### 🏗️ Nether Island Structure
The nether island template includes:
- **Blackstone Platform Base**: Sturdy foundation for your nether island
- **Netherrack Middle Layer**: Classic nether building material
- **Soul Sand Patches**: Pre-planted areas for nether wart farming
- **Starter Chest Contents**:
  - Water buckets (2) - Essential for nether survival
  - Obsidian (4) - For portal construction
  - Flint and Steel (1) - Fire making tool
  - Nether Wart (8) - For brewing and farming
  - Blaze Rod (2) - Brewing stand fuel
  - Ghast Tear (1) - Advanced brewing ingredient
  - Cooked Porkchop (10) - Food supply
  - Magma Block (4) - Light and building material

### 🌍 Technical Implementation
- **World Structure**: Nether islands are organized in `skyeblock/nether/` directories
- **SlimeWorld Integration**: Uses structured naming `skyeblock_nether_<islandId>` 
- **Biome Management**: Automatic 32x32 biome setting around the island
- **Environment Settings**: Proper nether environment with mob spawning disabled by default
- **Fallback Support**: Falls back to main nether world if individual worlds aren't available

### 🎮 Creating Nether Islands
```bash
# Create a nether island using either command style
/island create nether
/sb island create nether

# View available island types (includes nether)
/island types
/sb island types
```

### ⚙️ Configuration
Nether worlds are configured in the main config:
```yaml
worlds:
  nether:
    enabled: true
    environment: "NETHER"
    biome: "NETHER_WASTES"
```

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
- **Nether World Support**: Dedicated nether void worlds with proper environment and biome settings
- **Structured World Organization**: Islands organized in `skyeblock/overworld/` and `skyeblock/nether/` directories
- **Fallback Support**: Graceful fallback to standard Bukkit worlds if SlimeWorldManager is not installed
- **World Cleanup**: Proper world deletion and resource cleanup on island deletion

### Island Management
- **Island Format**: `island-<type>-<player-uuid>` for unique identification
- **Multi-Environment Support**: Supports both overworld and nether environments
- **Biome Management**: Automatic biome setting for nether islands (NETHER_WASTES)
- **Safe Teleportation**: Smart spawn point calculation with template-based positioning
- **Schematic Support**: Built-in YAML-based schematic system with chest population and nether-specific features
- **Settings Persistence**: Individual gamerule settings saved per island

### GUI System
- **Interactive Settings**: Full GUI interface for gamerule management
- **Permission Integration**: Dynamic gamerule visibility based on LuckPerms permissions
- **Real-time Updates**: Live updates when gamerule values are changed
- **Admin Bypass**: Staff can see all gamerules regardless of individual restrictions

### Performance Features
- **Optimized World Loading**: Efficient world creation and management for both overworld and nether environments
- **Memory Management**: Proper cleanup and resource management
- **Compressed Storage**: SlimeWorld integration for reduced disk usage
- **Structured World Organization**: Organized folder structure for overworld and nether islands
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
/island create nether     # NEW! Creates nether island in dedicated nether void world

# Check available types first
/island types
# Output: Available island types: classic, desert, nether
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
- **`NETHER_ISLAND_GUIDE.md`** - Comprehensive nether island features and setup guide
- **`LUCKPERMS_GAMERULE_PERMISSIONS.md`** - LuckPerms gamerule permission setup
- **`ASWM_INTEGRATION_GUIDE.md`** - SlimeWorldManager integration details
- **`ISLAND_SETTINGS_IMPLEMENTATION.md`** - Island settings and GUI system

### Technical Documentation
- **`GAMERULE_PERMISSIONS_UPDATE.md`** - Technical implementation details
- **`NETHER_FOLDER_STRUCTURE_COMPLETE.md`** - Nether world organization and structure
- **`DEPLOYMENT_GUIDE.md`** - Server deployment and debugging information
- **`TESTING.md`** - Testing procedures and expected results
- **`PROJECT_COMPLETE.md`** - Complete feature overview and status

### Quick References
- **`DEPLOYMENT_CHECKLIST.md`** - Pre-deployment validation checklist
- **`COMMAND_TESTING.md`** - Command testing procedures
- **`test-nether-folder-structure.sh`** - Nether island testing script
- **`MINIMESSAGE_TESTING.md`** - Text formatting validation

## 🚀 Recent Updates

### ✨ Version 2.0.0 - Major Release (January 2025)

#### 🎮 Island Creation GUI System
- **NEW: Interactive Island Creation**: Beautiful GUI interface for creating new islands
- **Visual Island Type Selection**: Choose from Classic, Desert, or Nether islands with visual previews
- **Template Preview System**: See island layouts before creation with detailed descriptions
- **Enhanced User Experience**: Intuitive click-to-create interface with confirmation dialogs
- **Smart Template Loading**: Automatic template validation and loading system

#### 🏝️ Advanced Island Management
- **Comprehensive Island Control**: Full lifecycle management from creation to deletion
- **Multi-Environment Support**: Seamless handling of overworld and nether islands
- **Island Type Validation**: Robust validation system for all island operations
- **Enhanced Safety Features**: Multiple confirmation layers for destructive operations
- **Improved World Handling**: Better world creation, loading, and cleanup processes

#### 🌋 Nether Integration & Biome Management
- **Complete Nether System**: Full support for nether islands with proper environment handling
- **Advanced Biome Control**: Automatic biome setting and management for all island types
- **Nether Void Worlds**: Dedicated nether void environments with proper spawning
- **Multi-Biome Support**: Support for various biomes including desert, plains, and nether wastes
- **Environment-Specific Templates**: Tailored templates for different biome types

#### 💬 MiniMessage Support & Modern UI
- **Adventure API Integration**: Full MiniMessage support for modern text formatting
- **Rich Text Messaging**: Colored text, hover effects, and interactive elements
- **Enhanced GUI Elements**: Beautiful, modern interface elements with proper styling
- **Improved User Feedback**: Clear, formatted messages for all user interactions
- **Consistent Design Language**: Unified visual design across all plugin interfaces

#### 🔒 Enhanced Security & Stability
- **Robust Error Handling**: Comprehensive error catching and graceful failure recovery
- **Input Validation**: Strong validation for all user inputs and commands
- **Resource Management**: Improved memory and resource cleanup
- **Thread Safety**: Better concurrency handling for multi-player environments
- **Stability Improvements**: Reduced crashes and improved overall plugin stability

#### 🎯 Dual Command System (Continued)
- **Direct Commands**: `/island`, `/visit`, `/delete`, `/hub` (preserves existing usage)
- **Sub-Commands**: `/sb island`, `/sb visit`, `/sb delete`, `/sb hub` (unified interface)
- **Full Backward Compatibility**: All existing commands continue to work seamlessly
- **Smart Help System**: `/sb` shows all available sub-commands with descriptions
- **Complete Tab Completion**: Both command styles support intelligent tab completion

#### 🔧 Core System Improvements
- **SlimeWorldManager Integration**: Individual island worlds with automatic fallback
- **🌍 Advanced World Management**: Structured organization with overworld/nether directories
- **LuckPerms Permission System**: Granular control over 31 island gamerules
- **Interactive Settings GUI**: Real-time gamerule management with visual indicators
- **Optimized Performance**: Better world management and resource cleanup
- **Directory Structure**: Organized world storage with `skyeblock/islands/` and `skyeblock/nether/`

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
- ✅ **Multi-Environment Testing**: Validation for both overworld and nether islands
- ✅ **Validation Tools**: Multiple validation scripts for different components
- ✅ **Backward Compatibility**: Maintains compatibility with existing setups
- ✅ **Performance Optimized**: Efficient world management and resource usage

---

## 📄 License

This plugin is developed for SkyeNetwork. All rights reserved.

**SkyeBlock Plugin v2.0.0** - A comprehensive island management solution with interactive GUI system, advanced island management, nether integration, modern MiniMessage support, and enhanced security features for modern Minecraft servers.
