# SkyeBlock Plugin - External Server Deployment Guide

## Current Status
✅ **Plugin Built Successfully**: `skyeblock-1.1.0.jar` (147KB)  
✅ **All Features Implemented**: Island persistence, conversion command, automatic nether islands  
✅ **Local Build Successful**: No compilation errors  

## Transfer to External Server

### Method 1: SCP/SFTP Transfer
```bash
# Replace with your server details
scp /mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-1.1.0.jar username@your-server-ip:/path/to/minecraft/server/plugins/

# Example:
scp /mnt/sda4/SkyeNetwork/SkyeBlock/target/skyeblock-1.1.0.jar user@example.com:/home/minecraft/server/plugins/
```

### Method 2: Manual Upload
1. Download `skyeblock-1.1.0.jar` from this local machine
2. Upload to your external server's `plugins/` folder
3. Remove any old SkyeBlock plugin versions

## Server Requirements Check

### Required Dependencies
- ✅ **WorldEdit Plugin**: Required for schematic pasting
- ✅ **WorldGuard Plugin**: Required for region protection
- ❓ **SlimeWorldManager (ASWM)**: Optional but recommended for individual island worlds

### Optional Dependencies
- **LuckPerms**: For granular permission management
- **Paper/Spigot**: 1.20+ recommended

## Installation Steps

### 1. Stop the Server
```bash
# Stop your Minecraft server completely
systemctl stop minecraft
# OR
screen -r minecraft  # then type 'stop'
```

### 2. Install Plugin
```bash
# Navigate to plugins directory
cd /path/to/your/minecraft/server/plugins/

# Remove old SkyeBlock plugin (if exists)
rm -f skyeblock-*.jar

# Copy new plugin
cp skyeblock-1.1.0.jar ./

# Verify permissions
chmod 644 skyeblock-1.1.0.jar
```

### 3. Verify Dependencies
```bash
# Check for required plugins
ls -la plugins/ | grep -E "(WorldEdit|WorldGuard)"

# If missing, download from:
# WorldEdit: https://dev.bukkit.org/projects/worldedit
# WorldGuard: https://dev.bukkit.org/projects/worldguard
```

### 4. Start Server and Monitor
```bash
# Start server
systemctl start minecraft
# OR
screen -S minecraft java -jar server.jar

# Monitor logs for plugin loading
tail -f logs/latest.log | grep -i skyeblock
```

## Expected Server Console Output

### ✅ Successful Loading
```
[INFO] [SkyeBlock] Enabling SkyeBlock v1.1.0
[INFO] [SkyeBlock] Island data manager initialized
[INFO] [SkyeBlock] Loading existing islands...
[INFO] [SkyeBlock] Loaded X islands from storage
[INFO] [SkyeBlock] Created skyblock world: skyblock_world
[INFO] [SkyeBlock] SkyeBlock plugin enabled successfully!
```

### ❌ Common Issues

#### Missing Dependencies
```
[ERROR] Could not load 'plugins/skyeblock-1.1.0.jar' in folder 'plugins'
org.bukkit.plugin.UnknownDependencyException: WorldEdit
```
**Fix**: Install WorldEdit and WorldGuard plugins

#### Version Mismatch
```
[ERROR] Could not load 'plugins/skyeblock-1.1.0.jar'
UnsupportedClassVersionError
```
**Fix**: Server needs Java 17+ and Minecraft 1.20+

#### File Permissions
```
[ERROR] Could not load plugin 'skyeblock-1.1.0.jar'
```
**Fix**: Check file permissions: `chmod 644 skyeblock-1.1.0.jar`

## Testing After Installation

### 1. Basic Plugin Check
```
/plugins
# Should show "SkyeBlock v1.1.0" in green
```

### 2. Test Island Creation
```
/island create classic
# Should create an island and teleport player
```

### 3. Test Persistence Features
```
# Create an island, then restart server
# Island should still exist after restart
```

### 4. Test Conversion Command (if you have old islands)
```
/convertislands scan
# Should show any old format islands found

/convertislands convert all
# Should convert old islands to new format
```

## Debugging Commands

### Check Plugin Status
```bash
# View server logs
tail -f logs/latest.log

# Search for SkyeBlock messages
grep -i skyeblock logs/latest.log

# Check for errors
grep -i error logs/latest.log | grep -i skyeblock
```

### Verify Island Files
```bash
# Check if island data is being saved
ls -la plugins/SkyeBlock/islands/
cat plugins/SkyeBlock/islands/example-island.yml
```

### Check Worlds
```bash
# Verify world directories exist
ls -la skyeblock/
ls -la skyeblock/islands/
ls -la skyeblock/nether/
```

## Troubleshooting Specific Issues

### Issue: Plugin Not Loading
1. Check server console for specific error messages
2. Verify Java version: `java -version` (needs 17+)
3. Check Minecraft server version (needs 1.20+)
4. Verify all dependencies are installed

### Issue: Islands Not Persisting
1. Check file permissions on `plugins/SkyeBlock/` directory
2. Verify no errors in console during island creation
3. Check if `plugins/SkyeBlock/islands/` folder exists

### Issue: Conversion Command Not Working
1. Verify old island directories exist
2. Check console for specific conversion errors
3. Ensure proper permissions for file operations

## Support Information

If you encounter issues:

1. **Share Server Logs**: Copy relevant console output
2. **List Installed Plugins**: `/plugins` command output
3. **Server Version**: `/version` command output
4. **Java Version**: `java -version` on server
5. **File Structure**: `ls -la plugins/SkyeBlock/`

## Key Files to Monitor

- `logs/latest.log` - Server console output
- `plugins/SkyeBlock/config.yml` - Plugin configuration
- `plugins/SkyeBlock/islands/` - Island data storage
- `skyeblock/islands/` - Island world folders
- `skyeblock/nether/` - Nether island world folders

---

**Plugin Version**: 1.1.0  
**Built**: $(date)  
**Features**: Island persistence, automatic nether islands, conversion command  
**Ready for Production**: ✅ Yes
