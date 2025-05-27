# SkyeBlock Plugin

A comprehensive SkyeBlock plugin for Paper/Spigot servers that allows players to create and manage their own skyblock islands.

## Features

### Core Functionality
- **Multiple Island Types**: Choose from Classic, Desert, or Nether-themed islands
- **Custom Schematic System**: YAML-based island templates with detailed block placement
- **Void World Generation**: Dedicated void world for skyblock gameplay
- **Island Management**: Create, delete, and teleport to islands
- **Player Permissions**: Role-based access control for commands

### Island Types Available
1. **Classic SkyBlock** - Traditional dirt platform with tree, chest, and basic supplies
2. **Desert Island** - Sand-based island with cactus and desert survival items  
3. **Nether Island** - Challenging netherrack island with nether-themed resources

### Advanced Features
- **Smart Island Spacing**: Islands automatically placed 1000+ blocks apart
- **Chest Auto-Population**: Islands come pre-loaded with type-specific items
- **Configurable Messages**: Fully customizable player messages
- **Tab Completion**: Smart command completion for better UX
- **Admin Tools**: Island listing and management for server operators

## Commands

- `/island create [type]` - Create a new island (types: classic, desert, nether)
- `/island types` - Show available island types with descriptions
- `/island tp` - Teleport to your island
- `/island help` - Show help message

### Admin Commands
- `/island delete` - Delete your island (admin only)
- `/island list` - List all islands (admin only)

## Installation

1. **Prerequisites**:
   - Paper/Spigot server (1.20+)
   - No additional plugins required (self-contained schematic system)

2. **Building**:
   ```bash
   ./build.sh
   ```

3. **Installation**:
   - Copy `target/skyeblock-1.0.0.jar` to your server's `plugins/` folder
   - Restart your server
   - Plugin includes built-in island templates (Classic, Desert, Nether)

4. **Quick Test**:
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

# Island settings  
island:
  distance: 1000             # Distance between islands (increased for larger templates)
  start-x: 0                # Starting X coordinate
  start-z: 0                # Starting Z coordinate

# Messages (customizable)
messages:
  prefix: "&8[&6SkyeBlock&8] &r"
  island-created: "&aIsland created successfully! Teleporting you there..."
  island-already-exists: "&cYou already have an island!"
  island-not-found: "&cYou don't have an island yet!"
  no-permission: "&cYou don't have permission to use this command!"
  invalid-command: "&cInvalid command! Use /island help for available commands."
```

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

- `skyeblock.*` - All permissions (default: op)
- `skyeblock.island` - Basic island commands (default: true)
- `skyeblock.admin` - Admin commands (default: op)

## Technical Details

- **Island Spacing**: Islands are placed in a grid pattern with configurable distance
- **World Generation**: Uses a custom void world generator
- **Island Format**: `island-<type>-<player-uuid>`
- **Schematic Support**: Supports both `.schem` and `.schematic` formats
- **Safe Teleportation**: Players are teleported to a safe location above the island

## Support

For issues, questions, or contributions, please contact the SkyeNetwork development team.

## License

This plugin is developed for SkyeNetwork. All rights reserved.
