# Island Deletion Cooldown System Update

## Overview
Updated the island deletion cooldown system with the following changes:

### 1. Admin Bypass for Deletion Cooldown ✅
- **Change**: Admins with `skyeblock.admin` permission can now bypass deletion cooldown restrictions
- **Location**: `IslandManager.canDeleteIsland()` method
- **Behavior**: Admins can delete any island at any time, regardless of cooldown status

### 2. Cooldown Logic Reversal ✅
- **Previous Behavior**: Cooldown prevented island **creation** after deletion
- **New Behavior**: Cooldown prevents island **deletion** during cooldown period
- **Effect**: Players can now create islands immediately after deletion, but must wait during cooldown to delete again

### 3. Location Requirement During Cooldown ✅
- **New Rule**: Players must be on their island to delete it during cooldown period
- **Check**: Compares player's current world name with island world name
- **Purpose**: Ensures players are intentionally on their island before deletion

### 4. Updated Flow ✅
The new island lifecycle flow:
```
Create Island → Delete Island → [Cooldown Period - Can Create New Island] → [Must be on island to delete during cooldown] → Cooldown Ends
```

## Technical Changes

### Modified Methods

#### `IslandManager.canDeleteIsland(Player player)`
- Added admin bypass check (`skyeblock.admin` permission)
- Added location requirement during cooldown
- Updated cooldown logic to block deletion instead of creation

#### `IslandManager.canCreateIsland(Player player)`
- Removed cooldown checks
- Now only checks maximum deletion tries limit
- Players can create islands immediately after deletion

#### `IslandManager.deleteIsland(Player player)`
- Updated deletion warning messages
- Changed cooldown messaging to reflect new system (deletion restrictions vs creation restrictions)

### New Configuration Messages

Added to `config.yml`:
```yaml
deletion-blocked-cooldown: "<red>You cannot delete your island while on cooldown. Time remaining: {time}</red>"
deletion-requires-island-location: "<red>You must be on your island to delete it during cooldown period!</red>"
deletion-cooldown-info: "<yellow>You cannot delete another island for {time}. You must be on your island to delete it during this cooldown.</yellow>"
```

## Behavior Examples

### Example 1: Regular Player Flow
1. **Create Island** → ✅ Allowed
2. **Delete Island** → ✅ Allowed, cooldown starts
3. **Create New Island** → ✅ Allowed immediately
4. **Try to Delete (not on island)** → ❌ Must be on island during cooldown
5. **Go to Island and Try to Delete** → ❌ Still on cooldown
6. **Wait for Cooldown to End** → ✅ Can delete again

### Example 2: Admin Flow
1. **Create Island** → ✅ Allowed
2. **Delete Island** → ✅ Allowed
3. **Try to Delete Another Island** → ✅ Admin bypass - always allowed

### Example 3: Max Tries Reached
1. **Delete Island 3rd Time** → ✅ Allowed
2. **Try to Create New Island** → ❌ Max tries reached
3. **Try to Delete Current Island** → ✅ Still allowed if admin, or if on island during cooldown

## Configuration
- Cooldown duration: `island.create-island.delay` (default: 300 seconds)
- Maximum deletion tries: `island.create-island.tries` (default: 3)
- Admin permission: `skyeblock.admin`

## Backward Compatibility
- ✅ All existing configuration options preserved
- ✅ No database schema changes required
- ✅ Existing cooldown tracking data remains valid
- ✅ No breaking changes to existing commands

## Testing Recommendations

### Test Admin Bypass
```bash
# As admin
/island delete  # Should work regardless of cooldown status
```

### Test Location Requirement
```bash
# As regular player during cooldown
/hub              # Go away from island
/island delete    # Should fail with location requirement message
/island tp        # Go back to island
/island delete    # Should fail with cooldown message (but different from location message)
```

### Test New Flow
```bash
# Complete flow test
/island create vanilla    # Create island
/island delete           # Delete island (cooldown starts)
/island create beginner  # Should work immediately
/island delete           # Should require being on island + respect cooldown
```

## Files Modified
- `src/main/java/skyeblock/nobleskye/dev/skyeblock/managers/IslandManager.java`
- `config.yml`
- `src/main/resources/config.yml`
- `target/classes/config.yml`

## Implementation Complete ✅
All requested changes have been implemented:
- ✅ Admin bypass for deletion cooldown
- ✅ Cooldown blocks deletion instead of creation
- ✅ Location requirement during cooldown
- ✅ Updated messaging and documentation
