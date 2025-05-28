# SkyeBlock - LuckPerms Gamerule Permissions Guide

## Overview

The SkyeBlock plugin now uses a **LuckPerms-style permission system** for controlling which gamerules players can see and modify in the `/island settings` GUI. This system follows the principle of **"allow by default, deny explicitly"**.

## How It Works

### Default Behavior
- **All gamerules are visible by default** to players with the `skyeblock.island` permission
- Players can modify any gamerule unless explicitly denied access
- This provides maximum flexibility while allowing precise control

### Permission Structure

#### Admin Bypass
```
skyeblock.gamerules.adminbypass
```
- **Default:** `op` only
- **Effect:** Players with this permission see ALL gamerules regardless of individual permissions
- **Use Case:** Server administrators who need full access

#### Individual Gamerule Permissions
```
skyeblock.gamerule.<gamerule_name>
```
- **Default:** `true` (everyone can access)
- **Format:** Uses lowercase gamerule names
- **Examples:**
  - `skyeblock.gamerule.keepinventory`
  - `skyeblock.gamerule.mobgriefing`
  - `skyeblock.gamerule.dodaylightcycle`

## Supported Gamerules

The following gamerules are available with individual permissions:

### Boolean Gamerules
- `skyeblock.gamerule.dodaylightcycle` - Daylight cycle control
- `skyeblock.gamerule.doweathercycle` - Weather changes
- `skyeblock.gamerule.keepinventory` - Keep items on death
- `skyeblock.gamerule.mobgriefing` - Mob block changes
- `skyeblock.gamerule.domobspawning` - Natural mob spawning
- `skyeblock.gamerule.dofiretick` - Fire spread
- `skyeblock.gamerule.falldamage` - Fall damage
- `skyeblock.gamerule.firedamage` - Fire damage
- `skyeblock.gamerule.drowningdamage` - Drowning damage
- `skyeblock.gamerule.doinsomnia` - Phantom spawning
- `skyeblock.gamerule.doimmediaterespawn` - Skip death screen
- `skyeblock.gamerule.announceadvancements` - Advancement messages
- `skyeblock.gamerule.disableelytraMovementcheck` - Elytra movement validation
- `skyeblock.gamerule.dolimitedcrafting` - Limited crafting
- `skyeblock.gamerule.naturalregeneration` - Health regeneration
- `skyeblock.gamerule.reduceddebuginfo` - Reduced F3 debug
- `skyeblock.gamerule.sendcommandfeedback` - Command feedback
- `skyeblock.gamerule.showdeathmessages` - Death messages
- `skyeblock.gamerule.doentitydrops` - Entity item drops
- `skyeblock.gamerule.dotiledrops` - Block item drops
- `skyeblock.gamerule.domobloot` - Mob loot drops
- `skyeblock.gamerule.dopatrolspawning` - Patrol spawning
- `skyeblock.gamerule.dotraderSpawning` - Wandering trader spawning
- `skyeblock.gamerule.forgivedeadplayers` - Forgive dead players
- `skyeblock.gamerule.universalanger` - Universal anger

### Integer Gamerules
- `skyeblock.gamerule.randomtickspeed` - Random tick speed
- `skyeblock.gamerule.spawnradius` - Spawn radius
- `skyeblock.gamerule.maxentitycramming` - Entity cramming limit
- `skyeblock.gamerule.maxcommandchainlength` - Command chain limit
- `skyeblock.gamerule.playerssleepingpercentage` - Sleep percentage

## LuckPerms Configuration Examples

### Example 1: Default Setup (All players can access all gamerules)
```bash
# No additional configuration needed - default behavior
# All players with skyeblock.island can access all gamerules
```

### Example 2: Restrict Dangerous Gamerules
```bash
# Deny access to potentially disruptive gamerules
lp group default permission set skyeblock.gamerule.mobgriefing false
lp group default permission set skyeblock.gamerule.dofiretick false
lp group default permission set skyeblock.gamerule.keepinventory false

# VIP players can still access these
lp group vip permission set skyeblock.gamerule.mobgriefing true
lp group vip permission set skyeblock.gamerule.dofiretick true
```

### Example 3: Staff-Only Advanced Settings
```bash
# Restrict advanced/technical gamerules to staff
lp group default permission set skyeblock.gamerule.randomtickspeed false
lp group default permission set skyeblock.gamerule.maxentitycramming false
lp group default permission set skyeblock.gamerule.maxcommandchainlength false

# Allow staff access
lp group moderator permission set skyeblock.gamerule.randomtickspeed true
lp group admin inherit moderator
```

### Example 4: Progressive Permission System
```bash
# New players - basic gamerules only
lp group newbie permission set skyeblock.gamerule.keepinventory true
lp group newbie permission set skyeblock.gamerule.dodaylightcycle true
lp group newbie permission set skyeblock.gamerule.doweathercycle true
# Deny everything else
lp group newbie permission set skyeblock.gamerule.* false

# Regular players - more access
lp group player inherit newbie
lp group player permission set skyeblock.gamerule.mobgriefing true
lp group player permission set skyeblock.gamerule.domobspawning true

# VIP players - almost everything
lp group vip permission set skyeblock.gamerule.* true
lp group vip permission set skyeblock.gamerule.randomtickspeed false  # Still restrict this
```

### Example 5: Admin Bypass
```bash
# Give admins full access regardless of individual permissions
lp group admin permission set skyeblock.gamerules.adminbypass true
```

## GUI Behavior

### What Players See
- **Permitted Gamerules:** Show in the GUI with current values and modification controls
- **Denied Gamerules:** Hidden from the GUI completely
- **Admin Bypass:** All gamerules visible regardless of individual permissions

### GUI Controls
- **Boolean Gamerules:** Left-click to toggle true/false
- **Integer Gamerules:** 
  - Left-click: +1
  - Right-click: -1
  - Shift+Left-click: +10
  - Shift+Right-click: -10

### Visual Feedback
- **Boolean True:** Lime dye icon
- **Boolean False:** Red dye icon
- **Integer Values:** Comparator icon with current value in lore
- **Permission Denied:** Gamerule not visible in GUI

## Permission Testing

### Check Player Permissions
```bash
# Check if a player can see a specific gamerule
lp user <player> permission check skyeblock.gamerule.keepinventory

# Check admin bypass
lp user <player> permission check skyeblock.gamerules.adminbypass

# View all skyeblock permissions for a user
lp user <player> permission info | grep skyeblock
```

### Test Cases
1. **Default User:** Should see all gamerules
2. **Restricted User:** Should only see permitted gamerules
3. **Admin User:** Should see all gamerules regardless of restrictions
4. **Mixed Permissions:** Test combinations of allowed/denied gamerules

## Migration from Old System

### Old Permission Format (Deprecated)
```
skyeblock.gamerules.<gamerule>  # Required explicit true
```

### New Permission Format (Current)
```
skyeblock.gamerule.<gamerule>   # Default true, set false to deny
```

### Migration Steps
1. **Remove old explicit grants** (they're now default)
2. **Add explicit denies** where you want to restrict access
3. **Test the new behavior** with your permission groups
4. **Update documentation** for your players

## Benefits of LuckPerms Style

### ✅ Advantages
- **Simpler Setup:** Most servers need no configuration changes
- **Flexible Control:** Easy to restrict specific gamerules as needed
- **Intuitive Behavior:** Follows standard LuckPerms patterns
- **Future Proof:** Easy to add new gamerules with default access
- **Admin Friendly:** Clear bypass mechanism for administrators

### 🔧 Configuration Tips
- **Start permissive:** Use default behavior initially
- **Restrict gradually:** Add specific denies based on needs
- **Use inheritance:** Leverage LuckPerms group inheritance
- **Test thoroughly:** Verify behavior with different permission levels
- **Document changes:** Keep track of customizations for your server

## Troubleshooting

### Player Can't See Any Gamerules
1. Check they have `skyeblock.island` permission
2. Verify they have an island (`/island create`)
3. Check for blanket `skyeblock.gamerule.*` false permission

### Player Sees Wrong Gamerules
1. Check specific gamerule permissions
2. Verify inheritance chain in LuckPerms
3. Check for admin bypass permission

### GUI Not Updating
1. Restart the GUI (`/island settings`)
2. Check server logs for permission errors
3. Verify LuckPerms is loaded and functioning

---

**Note:** This permission system requires LuckPerms to be installed and properly configured on your server. The plugin will work with other permission systems but may not behave exactly as described above.
