# SkyeBlock Dual Command System

## Overview
The SkyeBlock plugin now supports both direct commands and sub-commands, providing maximum flexibility for users and server administrators.

## Implementation Details

### Command Registration
- **Main Command**: `/sb` (aliases: `/skyblock`)
- **Individual Commands**: `/island`, `/visit`, `/delete`, `/hub`
- **Aliases**: `/is` (for `/island`)

### Architecture
The dual command system is implemented using:

1. **SkyeBlockCommand**: Main command handler that routes sub-commands
2. **Individual Command Classes**: Existing command handlers (IslandCommand, VisitCommand, etc.)
3. **Plugin Registration**: Both command types registered in plugin.yml and SkyeBlockPlugin.java

## Command Usage

### Direct Commands (Original)
```
/island create <type>        - Create a new island
/island home                 - Teleport to your island
/island settings             - Open island settings GUI
/island lock                 - Lock your island
/island unlock               - Unlock your island
/island coop <player>        - Add a coop member
/island vote                 - Vote for your island
/island set spawn            - Set island spawn point
/island types                - List available island types

/visit <player>              - Visit another player's island
/delete [player]             - Delete your island (or admin delete)
/hub                         - Return to hub world
/is                          - Alias for /island
```

### Sub-Commands (New)
```
/sb island create <type>     - Create a new island
/sb island home              - Teleport to your island
/sb island settings          - Open island settings GUI
/sb island lock              - Lock your island
/sb island unlock            - Unlock your island
/sb island coop <player>     - Add a coop member
/sb island vote              - Vote for your island
/sb island set spawn         - Set island spawn point
/sb island types             - List available island types

/sb visit <player>           - Visit another player's island
/sb delete [player]          - Delete your island (or admin delete)
/sb hub                      - Return to hub world
/skyblock                    - Alias for /sb
```

### Help Command
```
/sb                          - Shows help with all available sub-commands
```

## Benefits

### For Users
- **Familiar**: Can continue using existing commands (`/island`, `/visit`, etc.)
- **Consistent**: Can use unified `/sb` prefix for all SkyeBlock commands
- **Flexible**: Choose the style that feels most natural

### For Servers
- **Backward Compatibility**: Existing users don't need to relearn commands
- **Organization**: All SkyeBlock commands can be grouped under `/sb`
- **Permission Control**: Can grant access to individual commands or the main `/sb` command

## Technical Implementation

### Files Modified
1. **Created**: `SkyeBlockCommand.java` - Main command router
2. **Updated**: `plugin.yml` - Added `/sb` command registration
3. **Updated**: `SkyeBlockPlugin.java` - Register new command handler

### Key Features
- **Command Routing**: SkyeBlockCommand routes sub-commands to appropriate handlers
- **Tab Completion**: Full tab completion support for both command styles
- **Error Handling**: Graceful handling of unknown sub-commands
- **Help System**: Built-in help when using `/sb` without arguments

### Code Structure
```java
public class SkyeBlockCommand implements CommandExecutor, TabCompleter {
    // Routes commands to individual handlers
    // Provides unified help system
    // Handles tab completion
}
```

## Testing
The implementation has been tested with:
- Command registration validation
- Plugin compilation verification
- JAR build confirmation
- Alias functionality testing

## Migration Guide
No migration required - this is a purely additive feature that maintains full backward compatibility.

## Future Enhancements
- Additional aliases can be easily added to plugin.yml
- New sub-commands can be added by updating the routing in SkyeBlockCommand
- Permission-based command filtering can be implemented in the router
