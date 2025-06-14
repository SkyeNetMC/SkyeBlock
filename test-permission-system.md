# Permission System Test Guide

## System Components Implemented ✅

### 1. Permission Enum (`IslandPermission.java`)
- ✅ 30+ permissions organized into categories (CONTAINER, BLOCK, REDSTONE, ENTITY, ITEM, ADMIN)
- ✅ Display names for GUI representation
- ✅ Permission nodes for role-based defaults

### 2. Permission Manager (`IslandPermissionManager.java`)
- ✅ Role-based default permissions (VISITOR < MEMBER < ADMIN < CO_OWNER < OWNER)
- ✅ Custom player permission overrides
- ✅ Integration with Island model for data persistence

### 3. Enhanced Visitor Protection (`VisitorProtectionListener.java`)
- ✅ Granular permission checking instead of simple role checks
- ✅ Container-specific permission mapping
- ✅ Block interaction permission verification
- ✅ Redstone device access control

### 4. Data Persistence (`IslandDataManager.java`)
- ✅ Custom permission saving/loading to YAML
- ✅ Player UUID to permission set mapping
- ✅ Integration with existing island data structure

### 5. GUI System (`PermissionManagementGUI.java`)
- ✅ Visual permission management interface
- ✅ Color-coded permission states (green=custom, yellow=from role, red=denied)
- ✅ Permission category organization
- ✅ Click-to-toggle functionality

### 6. Command Interface (`PermissionCommand.java`)
- ✅ `/islandpermissions [player]` command
- ✅ Player targeting and permission validation
- ✅ Integration with GUI system

## Testing Scenarios

### Basic Functionality Tests

#### Test 1: Permission Defaults
1. Create an island as Player A
2. Have Player B (VISITOR) try to:
   - ❌ Open chests → Should be blocked
   - ❌ Break blocks → Should be blocked  
   - ❌ Use redstone → Should be blocked
   - ✅ Walk around → Should work

#### Test 2: Coop Member Permissions
1. Add Player B as MEMBER to Player A's island
2. Have Player B try to:
   - ✅ Open chests → Should work (MEMBER+ default)
   - ✅ Use crafting tables → Should work
   - ❌ Manage other players → Should be blocked

#### Test 3: Custom Permission Override
1. Open `/islandpermissions Player_B`
2. Grant Player B BLOCK_BREAK permission (custom)
3. Player B should now be able to break blocks despite being VISITOR

#### Test 4: Permission Persistence
1. Set custom permissions for Player B
2. Restart server/reload plugin
3. Verify permissions are maintained

### GUI Testing

#### Test 5: Permission Management Interface
1. Use `/islandpermissions Player_B` as island owner
2. Verify GUI shows:
   - Current role (yellow indicators)
   - Custom permissions (green indicators)
   - Denied permissions (red indicators)
3. Test permission toggle functionality

### Integration Testing

#### Test 6: Listener Integration
1. Set custom CONTAINER_ACCESS permission for visitor
2. Verify visitor can open containers
3. Remove permission
4. Verify visitor is blocked again

## Expected Behavior

### Container Protection (Flan-like)
- ✅ Prevents visitors from accessing chests, barrels, shulker boxes
- ✅ Blocks furnace, brewing stand, and crafting station use
- ✅ Protects ender chests and item frames
- ✅ Allows appropriate access for island owners/coop members

### Granular Control
- ✅ Separate permissions for different container types
- ✅ Individual block interaction permissions  
- ✅ Redstone device access control
- ✅ Entity interaction management

### Role-Based Defaults
- VISITOR: Minimal access (walk, look)
- MEMBER: Container access, basic interactions
- ADMIN: Most permissions except management
- CO_OWNER: Nearly all permissions
- OWNER: All permissions

## Performance Considerations
- ✅ Permission checking optimized with cached lookups
- ✅ Role-based defaults minimize storage requirements
- ✅ Only custom overrides stored in island data
- ✅ GUI uses async operations where possible

## Compatibility
- ✅ Fully backward compatible with existing islands
- ✅ Integrates with existing coop role system
- ✅ Maintains existing visitor protection behavior as fallback
- ✅ Works with all existing commands and features
