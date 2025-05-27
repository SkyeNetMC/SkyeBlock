# SkyeBlock Plugin - Debugging & Deployment Guide

## Issues Identified and Fixed

### 1. ✅ **SlimeWorldManager Hard Dependency Issue**
**Problem:** The plugin.yml had SlimeWorldManager as a hard dependency (`depend`), which prevented the plugin from loading when SlimeWorldManager wasn't installed.

**Solution:** Changed SlimeWorldManager from `depend` to `softdepend` in plugin.yml:
```yaml
# Before (causing load failure):
depend: [WorldEdit, WorldGuard, SlimeWorldManager]

# After (allows optional integration):
depend: [WorldEdit, WorldGuard]
softdepend: [SlimeWorldManager]
```

### 2. ✅ **Plugin Build System**
**Problem:** Plugin needed to be rebuilt after dependency changes.

**Solution:** Successfully rebuilt the plugin with Maven:
```bash
mvn clean package
```

**Result:** Created `skyeblock-1.0.0.jar` (36K) with all features integrated.

## Plugin Validation Results

✅ **JAR File:** Built successfully (36K)  
✅ **Main Class:** `skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin`  
✅ **Dependencies:** WorldEdit, WorldGuard (required), SlimeWorldManager (optional)  
✅ **Commands:** `/island`, `/hub`  
✅ **Resources:** config.yml, plugin.yml, 3 schematic files  
✅ **Classes:** All command and manager classes present  

## Deployment Instructions

### Step 1: Server Requirements
Your Minecraft server needs:
- **Paper/Spigot 1.21+** 
- **WorldEdit plugin** (required)
- **WorldGuard plugin** (required)
- **SlimeWorldManager plugin** (optional, for individual island worlds)

### Step 2: Install the Plugin
1. **Copy the JAR file:**
   ```bash
   cp /mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-1.0.0.jar /path/to/your/server/plugins/
   ```

2. **Remove any old SkyeBlock JAR files** from the plugins folder

3. **Start/restart your server**

### Step 3: Verify Installation
Check your server console for these messages:

✅ **Successful Load:**
```
[INFO] [SkyeBlock] Enabling SkyeBlock v1.0.0
[INFO] [SkyeBlock] SlimeWorldManager integration initialized successfully!
# OR (if SlimeWorldManager not installed):
[INFO] [SkyeBlock] SlimeWorldManager not found. Using standard world creation.
[INFO] [SkyeBlock] Created skyblock world: skyblock_world
[INFO] [SkyeBlock] SkyeBlock plugin enabled successfully!
```

❌ **Failed Load (check for these):**
```
[ERROR] Error occurred while enabling SkyeBlock
[ERROR] Could not load 'plugins/skyeblock-1.0.0.jar'
```

## Troubleshooting Common Issues

### Issue: Plugin Won't Load
**Symptoms:** Plugin not showing in `/plugins` list

**Possible Causes & Solutions:**

1. **Missing Dependencies:**
   ```
   [ERROR] Could not load 'plugins/skyeblock-1.0.0.jar' in folder 'plugins'
   org.bukkit.plugin.UnknownDependencyException: WorldEdit
   ```
   **Fix:** Install WorldEdit and WorldGuard plugins

2. **Wrong Server Version:**
   ```
   [ERROR] Could not load 'plugins/skyeblock-1.0.0.jar'
   UnsupportedClassVersionError
   ```
   **Fix:** Server needs Java 17+ and Minecraft 1.21+

3. **File Permissions:**
   ```bash
   # Make sure the JAR is readable
   chmod 644 /path/to/plugins/skyeblock-1.0.0.jar
   ```

### Issue: Commands Not Working
**Symptoms:** `/island` or `/hub` commands not recognized

**Diagnosis:**
```
# In server console, check if plugin loaded:
/plugins

# Check if commands are registered:
/help island
/help hub
```

**Solutions:**
1. Restart server completely (not just reload)
2. Check for command conflicts with other plugins
3. Verify player has permissions: `skyeblock.island`, `skyeblock.hub`

### Issue: World Creation Problems
**Symptoms:** Islands not generating, world errors

**Check Configuration:**
1. Verify `config.yml` was created in `plugins/SkyeBlock/`
2. Check world permissions on server
3. Look for errors like:
   ```
   [ERROR] Failed to create skyblock world!
   [ERROR] Failed to create SlimeWorld: <error details>
   ```

**Solutions:**
1. Ensure server has write permissions to worlds folder
2. If using SlimeWorldManager, check its configuration
3. For standard worlds, ensure enough disk space

## Features Overview

### Core Features (Always Available)
- **Island Creation:** `/island create [type]` - Creates islands with templates
- **Island Teleportation:** `/island tp` - Teleport to your island
- **Island Deletion:** `/island delete` - Properly deletes islands with cleanup
- **Hub System:** `/hub` - Teleport to hub world
- **Multiple Templates:** Classic, Desert, Nether island types

### Advanced Features (With SlimeWorldManager)
- **Individual Island Worlds:** Each island gets its own world
- **Better Performance:** Optimized world management
- **Efficient Storage:** Compressed world storage

### Fallback Behavior (Without SlimeWorldManager)
- **Shared World:** All islands in one skyblock world
- **Standard Bukkit Worlds:** Uses default Bukkit world system
- **Full Compatibility:** All features work, just different world structure

## Configuration

The plugin creates `plugins/SkyeBlock/config.yml` with:

```yaml
# Hub world settings
hub:
  world: "world"           # Hub world name
  spawn:
    x: 0                   # Hub spawn coordinates
    y: 100
    z: 0
  enabled: true            # Enable hub teleportation

# Island settings
island:
  distance: 200            # Distance between islands
  templates:
    normal: "island-normal"
    spruce: "island-spruce" 
    cherry: "island-cherry"
  default-template: "normal"
```

## Next Steps

1. **Deploy the plugin** using the instructions above
2. **Test basic functionality:**
   - `/island create` - Create an island
   - `/island tp` - Teleport to island
   - `/hub` - Teleport to hub
   - `/island delete` - Delete island (will teleport to hub)

3. **Monitor server logs** for any warnings or errors
4. **Configure hub world** in config.yml if needed
5. **Test with SlimeWorldManager** if you plan to use individual island worlds

## Support

If you encounter issues:
1. Check server logs for specific error messages
2. Verify all dependencies are installed and compatible
3. Test commands step by step
4. Check file permissions and disk space
5. Consider testing without SlimeWorldManager first for basic functionality

The plugin is now properly configured for deployment and should work on any compatible Minecraft server with the required dependencies.
