# SkyeBlock Plugin Changelog

## Version 1.1.0 (May 30, 2025)

### 🗂️ Directory Structure Reorganization
- **BREAKING**: Changed normal islands directory from `skyeblock/overworld/` to `skyeblock/islands/`
- **Maintained**: Nether islands continue using `skyeblock/nether/` for proper environment separation
- **Updated**: SlimeWorld naming conventions updated to `skyeblock_islands_` for normal islands
- **Enhanced**: Improved world lookup logic to handle both old and new directory structures
- **Migration**: Seamless backward compatibility with existing installations

### 🔧 Technical Improvements
- Updated `WorldManager.java` for new directory structure
- Enhanced `createStandardWorld()` method to use `skyeblock/islands/` for normal islands
- Updated `createASWMWorld()` and `createSWMWorld()` methods with new SlimeWorld naming
- Improved `getIslandWorld()` method to search multiple directory structures
- Enhanced world deletion logic to handle both old and new structures

### 🐛 Bug Fixes
- Fixed world lookup issues when transitioning between directory structures
- Resolved SlimeWorld naming conflicts between overworld and normal islands
- Improved error handling for world creation and deletion

### 📚 Documentation Updates
- Updated README.md with version 1.1.0 information
- Added directory structure reorganization details
- Enhanced technical implementation documentation

---

## Version 1.0.0 (May 2025)

### 🎯 Initial Release Features
- **Dual Command System**: Support for both direct commands (`/island`) and sub-commands (`/sb island`)
- **🌋 Nether Island System**: Complete nether island support with dedicated void worlds
- **🔧 Advanced World Management**: SlimeWorldManager integration with fallback support
- **⚙️ Island Settings GUI**: Interactive gamerule management with 31 different settings
- **🔑 Granular Permissions**: LuckPerms integration for individual gamerule control
- **🏝️ Multiple Island Types**: Classic, Desert, and Nether island templates
- **🎮 Modern Features**: MiniMessage support, Adventure API integration
- **🛠️ Admin Tools**: Comprehensive island management and monitoring tools

### 🏗️ Core Systems
- Island creation and management system
- Custom YAML-based schematic system
- Hub integration with configurable teleportation
- Two-step deletion confirmation system
- Individual island worlds with proper environment settings
- Biome management for nether islands (NETHER_WASTES)

### 🔧 Technical Foundation
- Paper/Spigot compatibility (1.20+)
- SlimeWorldManager integration (ASWM/SWM)
- LuckPerms integration for advanced permissions
- Adventure API for modern text formatting
- Comprehensive error handling and fallback systems
