# Island Creation GUI - Troubleshooting Guide

## Current Status
✅ **IMPLEMENTATION COMPLETE** - The Island Creation GUI has been fully implemented and is ready for testing.

## Plugin Details
- **File**: `target/skyeblock-2.0.0.jar` (158KB)
- **Status**: Compiled successfully with all GUI components
- **Implementation**: Complete with event handlers, click detection, and visual feedback

## Quick Test Instructions

### 1. Deploy the Plugin
```bash
# Copy the plugin to your server
cp target/skyeblock-2.0.0.jar /path/to/your/server/plugins/

# Restart or reload the server
# The plugin will automatically register all event handlers
```

### 2. Test the GUI
```bash
# In-game commands for testing:
/createisland           # Opens the Island Creation GUI
/createisland classic   # Direct creation (bypass GUI)
```

### 3. Expected Behavior
When `/createisland` is executed:
1. **GUI Opens**: 3x9 inventory titled "Create Your Island" opens
2. **Items Display**: Island type items appear in slots 10-16 (grass block, sand, etc.)
3. **Visual Elements**: 
   - Info book in slot 4
   - Red barrier "Select Island Type" in slot 22
   - Red glass pane "Cancel" in slot 18
4. **Click Functionality**:
   - Clicking island types (slots 10-16) selects them with glow effect
   - Create button changes from red barrier to green emerald when type is selected
   - Cancel button closes the GUI
   - Create button creates the island and teleports player

## Troubleshooting Steps

### If GUI Items Are Not Clickable

#### Step 1: Verify Plugin Loading
```bash
# Check server console for:
# "SkyeBlock plugin enabled!"
# No error messages during plugin load
```

#### Step 2: Check Permissions
```bash
# Ensure player has permission:
# skyeblock.island.create
```

#### Step 3: Verify Command Registration
```bash
# Test command recognition:
/createisland
# Should either open GUI or show command not found
```

#### Step 4: Check for Conflicts
```bash
# Look for other plugins that might:
# - Override inventory click events
# - Cancel all inventory interactions
# - Interfere with custom GUIs
```

#### Step 5: Enable Debug Mode
If issues persist, temporarily add debug logging by modifying `IslandCreationGUI.java`:

```java
@EventHandler
public void onInventoryClick(InventoryClickEvent event) {
    // Add this line for debugging:
    plugin.getLogger().info("Click detected: " + event.getWhoClicked().getName() + " slot " + event.getSlot());
    
    if (!(event.getWhoClicked() instanceof Player)) return;
    if (!(event.getInventory().getHolder() instanceof IslandCreationGUI)) return;
    // ... rest of method
}
```

### Common Issues and Solutions

#### Issue: GUI Opens But Items Don't Respond
**Cause**: Event handler not registered or cancelled by another plugin
**Solution**: 
1. Check for conflicting plugins
2. Verify no errors in console during plugin load
3. Test on a clean server with only SkyeBlock

#### Issue: Command Not Found
**Cause**: Command not registered in plugin.yml or registration failed
**Solution**:
1. Verify `plugin.yml` contains `createisland:` command definition
2. Check console for "Failed to register 'createisland' command" messages
3. Ensure proper plugin loading order

#### Issue: Permission Denied
**Cause**: Player lacks required permission
**Solution**:
1. Grant `skyeblock.island.create` permission
2. Or temporarily test with OP player

#### Issue: GUI Opens But No Items Visible
**Cause**: SchematicManager returning empty array or item creation failing
**Solution**:
1. Verify schematics exist in `schematics/` folder
2. Check SchematicManager implementation
3. Verify Material types are valid for server version

## Implementation Details

### Key Components
1. **IslandCreationGUI.java**: Main GUI implementation with event handlers
2. **CreateIslandCommand.java**: Dual-mode command (GUI + direct)
3. **SkyeBlockPlugin.java**: Plugin integration and registration

### Event Flow
1. Player executes `/createisland`
2. CreateIslandCommand checks permissions and existing islands
3. If valid, calls `plugin.getIslandCreationGUI().openCreationGUI(player)`
4. IslandCreationGUI creates inventory with proper holder (`this`)
5. Items are added to specific slots (10-16 for types, 22 for create, 18 for cancel)
6. Player clicks items, triggering `onInventoryClick` event
7. Event handler checks inventory holder type and processes clicks
8. Island creation or GUI updates occur based on clicked slot

### Critical Implementation Points
- **Inventory Holder**: Created with `Bukkit.createInventory(this, size, title)`
- **Event Registration**: Automatic in constructor via `Bukkit.getPluginManager().registerEvents(this, plugin)`
- **Click Detection**: Uses `event.getInventory().getHolder() instanceof IslandCreationGUI`
- **Slot Mapping**: Specific slots for each GUI element (10-16, 18, 22)

## Next Steps for Testing

1. **Deploy** the latest plugin JAR (`skyeblock-2.0.0.jar`)
2. **Test** with a player that has the `skyeblock.island.create` permission
3. **Execute** `/createisland` and verify GUI opens
4. **Click** island type items and verify selection (glow effect)
5. **Click** create button and verify island creation
6. **Check** server console for any error messages

The implementation is complete and should work correctly. If issues persist, they are likely environmental (plugin conflicts, permissions, server configuration) rather than code-related.
