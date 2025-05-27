# SkyeBlock Command Testing Guide

This document provides testing instructions for verifying that the multi-word island type functionality works correctly after the Maven group ID change.

## Project Changes Summary

### 1. Maven Group ID Change
- **Old Group ID:** `com.skyenetwork`
- **New Group ID:** `skyeblock.nobleskye.dev`
- **Status:** ✅ COMPLETED

### 2. Package Structure Migration
- **Old Package:** `com.skyenetwork.skyeblock.*`
- **New Package:** `skyeblock.nobleskye.dev.skyeblock.*`
- **Status:** ✅ COMPLETED

### 3. Multi-Word Island Type Fix
- **Issue:** Commands like `/island create Desert Island` were not working
- **Fix:** Modified argument parsing to join all arguments after "create"
- **Status:** ✅ COMPLETED

## Testing Multi-Word Island Types

### Available Island Types
Based on the schematic files, the following island types should be available:
1. `classic` - Default island type
2. `Desert Island` - Multi-word type (from desert.yml)
3. `nether` - Single word type (from nether.yml)

### Test Commands

#### 1. List Available Types
```
/island types
```
**Expected Result:** Should display all available island types including "Desert Island"

#### 2. Create Classic Island (Single Word)
```
/island create classic
```
**Expected Result:** Should create a classic island successfully

#### 3. Create Desert Island (Multi-Word)
```
/island create Desert Island
```
**Expected Result:** Should create a desert-themed island successfully

#### 4. Create Nether Island (Single Word)
```
/island create nether
```
**Expected Result:** Should create a nether-themed island successfully

#### 5. Test Case Insensitivity
```
/island create desert island
/island create DESERT ISLAND
/island create Desert island
```
**Expected Result:** All variations should work due to case-insensitive matching

#### 6. Test Invalid Island Type
```
/island create invalid type
```
**Expected Result:** Should show error message with list of available types

### Tab Completion Testing

#### 1. Basic Tab Completion
Type `/island ` and press TAB
**Expected Result:** Should show subcommands: create, tp, teleport, home, types, help

#### 2. Create Command Tab Completion
Type `/island create ` and press TAB
**Expected Result:** Should show available island types including "Desert Island"

#### 3. Partial Multi-Word Completion
Type `/island create Des` and press TAB
**Expected Result:** Should complete to "Desert Island"

Type `/island create Desert ` and press TAB
**Expected Result:** Should complete to "Desert Island"

### Error Handling Tests

#### 1. Player Already Has Island
Create an island, then try to create another:
```
/island create classic
/island create Desert Island
```
**Expected Result:** Second command should show "island already exists" message

#### 2. No Permission Test
Test with a player without `skyeblock.island` permission:
```
/island create classic
```
**Expected Result:** Should show "no permission" message

#### 3. Admin Commands
Test with admin permissions:
```
/island list
/island delete
```
**Expected Result:** Should work for players with `skyeblock.admin` permission

## Technical Implementation Details

### Command Parsing Fix
The `handleCreateCommand` method now properly joins all arguments after "create":

```java
// Determine island type
String islandType;
if (args.length > 1) {
    // Join all arguments after "create" to handle island types with spaces
    StringBuilder typeBuilder = new StringBuilder();
    for (int i = 1; i < args.length; i++) {
        if (i > 1) typeBuilder.append(" ");
        typeBuilder.append(args[i]);
    }
    islandType = typeBuilder.toString();
} else {
    islandType = "classic"; // Default to classic type
}
```

### Tab Completion Fix
The tab completion now handles multi-word types:

```java
// Join current arguments to build partial type name
StringBuilder currentType = new StringBuilder();
for (int i = 1; i < args.length; i++) {
    if (i > 1) currentType.append(" ");
    currentType.append(args[i]);
}
String partialType = currentType.toString();

for (String type : availableTypes) {
    // If the type starts with what the user has typed (case-insensitive)
    if (type.toLowerCase().startsWith(partialType.toLowerCase())) {
        // Add the full type name for completion
        completions.add(type);
    }
}
```

## Build Verification

### Maven Build
```bash
cd /mnt/sda4/SkyeNetwork/SkyeBlock
mvn clean package
```
**Expected Result:** BUILD SUCCESS with correct groupId in output

### JAR Contents
```bash
jar -tf target/skyeblock-1.0.0.jar | grep skyeblock/nobleskye
```
**Expected Result:** Should show files in the new package structure

### Maven Metadata
```bash
jar -xf target/skyeblock-1.0.0.jar META-INF/maven/skyeblock.nobleskye.dev/skyeblock/pom.properties
cat META-INF/maven/skyeblock.nobleskye.dev/skyeblock/pom.properties
```
**Expected Result:** Should show `groupId=skyeblock.nobleskye.dev`

## Server Installation Testing

1. Copy `target/skyeblock-1.0.0.jar` to your Minecraft server's `plugins` folder
2. Start/restart the server
3. Check server logs for successful plugin loading
4. Test commands in-game with the scenarios above

## Status Summary

✅ **COMPLETED TASKS:**
- Maven group ID changed from `com.skyenetwork` to `skyeblock.nobleskye.dev`
- Package structure migrated to `skyeblock.nobleskye.dev.skyeblock.*`
- All import statements updated
- Plugin.yml main class updated
- Command parsing fixed for multi-word island types
- Tab completion fixed for multi-word island types
- Project builds successfully
- JAR contains correct package structure and metadata

🧪 **READY FOR TESTING:**
- Multi-word island type commands (`/island create Desert Island`)
- Tab completion for multi-word types
- Case-insensitive island type matching
- All existing functionality should continue to work

The SkyeBlock plugin is now ready for deployment and testing with the new group ID and improved command handling!
