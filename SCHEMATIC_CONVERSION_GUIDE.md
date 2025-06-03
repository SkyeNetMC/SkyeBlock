# SkyeBlock Schematic System Conversion Guide

## Overview
The SkyeBlock plugin has been successfully converted from using YAML-based island schematics to WorldEdit .schem files. This guide documents the changes made and the steps needed to complete the conversion.

## Changes Made

### Code Changes
1. **IslandManager.java**
   - Modified `createIsland()` method to use `SchematicManager.pasteSchematic()` instead of `CustomSchematicManager.pasteSchematic()`
   - Modified `createNetherIsland()` method to use `SchematicManager.pasteSchematic()`
   - Updated `isValidIslandType()` to use `SchematicManager.getAvailableSchematics()`
   - Updated spawn location fallback logic to use island center instead of YAML-defined spawn points

2. **IslandCommand.java**
   - Updated all references from `getCustomSchematicManager()` to `getSchematicManager()`
   - Modified `handleTypesCommand()` to show generic descriptions since .schem files don't have built-in metadata
   - Updated tab completion to use `SchematicManager.getAvailableSchematics()`

3. **SchematicManager.java**
   - Fixed compilation issues with WorldEdit API calls
   - Added `getAvailableSchematics()` method to list available .schem/.schematic files
   - Enhanced `pasteSchematic()` method to handle both .schem and .schematic file extensions
   - Improved error handling and logging

### File Structure Changes
- Created `/schematics/` folder in plugin directory for .schem files
- Added placeholder .schem files: `classic.schem`, `desert.schem`, `nether.schem`

## What's Still Needed

### 1. Create Actual .schem Files
The current .schem files are placeholders. You need to:

1. **Start a Minecraft server** with WorldEdit and SkyeBlock plugins
2. **Build island templates** in-game based on the original YAML specifications:
   
   **Classic Island:**
   - 9x9 dirt base, 7x7 grass top
   - Oak sapling in center
   - Chest with: ice(2), lava_bucket(1), melon_seeds(1), pumpkin_seeds(1), sugar_cane(2), bread(5), bone_meal(3)
   
   **Desert Island:**
   - Sand platform with sandstone base
   - Cactus and dead bush
   - Chest with desert-appropriate starting items
   
   **Nether Island:**
   - Netherrack platform
   - Nether fortress-style structures
   - Chest with nether-appropriate items
   - Lava source and nether plants

3. **Create schematics** using WorldEdit:
   ```
   //wand
   //pos1 (select first corner)
   //pos2 (select second corner)
   //copy
   //schem save <name>
   ```

4. **Copy .schem files** from WorldEdit's schematics folder to SkyeBlock's `/schematics/` folder

### 2. Configuration Updates
Consider adding configuration options for:
- Default spawn offset for .schem files (since they don't have embedded spawn data)
- Template mappings in config.yml if you want friendly names

### 3. Migration for Existing Servers
If upgrading an existing server:
- The old CustomSchematicManager is still present but unused
- Consider adding a migration notice in console logs
- Test island creation with new system before going live

## Testing
1. Place actual .schem files in the `/schematics/` folder
2. Start the server and test:
   - `/island create classic`
   - `/island create desert` 
   - `/island create nether`
   - `/island types` (should show available schematics)
3. Verify islands paste correctly and spawn locations work

## Benefits of .schem Format
- **Better Performance**: WorldEdit handles pasting more efficiently than block-by-block placement
- **Full Block Support**: Supports all Minecraft blocks and block states without manual mapping
- **Visual Creation**: Build templates in-game instead of writing YAML
- **Industry Standard**: Compatible with all WorldEdit-based tools
- **Smaller Files**: Binary format is more compact than YAML
- **Future-Proof**: Automatically supports new blocks as Minecraft updates

## Rollback Plan
If issues arise:
1. The CustomSchematicManager code is still present
2. Revert the changes in IslandManager and IslandCommand to use CustomSchematicManager
3. The original YAML files are still in `src/main/resources/schematics/`

## Build Status
✅ Plugin compiles successfully
✅ JAR built: `target/skyeblock-1.1.0.jar`
✅ All references to CustomSchematicManager updated
⚠️ Placeholder .schem files need replacement with actual schematics
