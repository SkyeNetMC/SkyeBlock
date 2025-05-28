# Advanced Slime World Manager (ASWM) Integration Guide

## Overview

SkyeBlock now supports **Advanced Slime World Manager (ASWM)** integration for better performance and memory efficiency when managing island worlds. The integration uses reflection to avoid compile-time dependencies, making it work with or without ASWM installed.

## How It Works

### Automatic Detection
- SkyeBlock automatically detects if ASWM or the old SlimeWorldManager is installed
- Falls back to standard Bukkit world creation if neither is available
- No configuration required - just install ASWM and restart your server

### Supported Versions
- **Advanced Slime World Manager (ASWM)** - Recommended
- **SlimeWorldManager (old)** - Legacy support
- **Standard Bukkit Worlds** - Fallback when no slime manager is available

## Installation

### Option 1: ASWM Plugin (Recommended)
1. Download ASWM from: https://infernalsuite.com/docs/asp/
2. Place `AdvancedSlimeWorldManager.jar` in your `plugins/` folder
3. Restart your server
4. SkyeBlock will automatically detect and use ASWM

### Option 2: ASWM Server
1. Download the ASWM server jar (modified Paper with ASWM built-in)
2. Use it as your server jar instead of Paper
3. SkyeBlock will automatically detect and use ASWM

## Benefits of Using ASWM

### Performance
- **Faster world loading**: Slime worlds load instantly
- **Memory efficiency**: Worlds are stored in compressed format
- **Better I/O**: Reduced disk operations for world management

### Island Management
- **Quick creation**: Islands are created instantly using templates
- **Easy deletion**: No need to delete large world folders
- **World templates**: Efficient storage of island schematics

### Server Resources
- **Lower RAM usage**: Slime worlds use less memory
- **Reduced disk space**: Compressed world storage
- **Better scalability**: Handle more islands efficiently

## Commands

### Check ASWM Status
```
/island status
```
Shows current world manager status and ASWM integration info (Admin only).

## Configuration

### World Settings
SkyeBlock automatically configures slime worlds with optimal settings for skyblock:

```yaml
# Automatically applied slime world properties:
spawn:
  x: 0
  y: 100
  z: 0
difficulty: peaceful
allowMonsters: false
allowAnimals: false
pvp: false
```

### File Storage
ASWM stores worlds in the `slime_worlds/` directory instead of individual world folders, significantly reducing disk usage.

## Troubleshooting

### Check Integration Status
1. Use `/island status` command to verify ASWM is detected
2. Check server logs for ASWM initialization messages:
   - ✅ `"Advanced Slime World Manager integration initialized successfully!"`
   - ✅ `"SlimeWorldManager integration initialized successfully!"`
   - ⚠️ `"No SlimeWorldManager found. Using standard world creation."`

### Common Issues

#### ASWM Not Detected
- Ensure ASWM plugin is in the `plugins/` folder
- Check that ASWM loads before SkyeBlock
- Verify ASWM version compatibility

#### Memory Issues
- If using ASWM, ensure adequate RAM allocation
- Check ASWM configuration for memory settings
- Monitor server performance with `/island status`

#### World Loading Problems
- Check ASWM logs for errors
- Verify file permissions in `slime_worlds/` directory
- Restart server if needed

### Fallback Behavior
If ASWM fails or becomes unavailable:
1. SkyeBlock automatically falls back to standard Bukkit worlds
2. Existing islands remain accessible
3. New islands are created as standard worlds
4. No data loss occurs

## Migration

### From Standard Worlds to ASWM
1. Install ASWM
2. Restart server
3. New islands will use ASWM automatically
4. Existing standard world islands continue to work

### From ASWM to Standard Worlds
1. Remove ASWM plugin
2. Restart server
3. SkyeBlock falls back to standard world creation
4. Existing slime world islands may need manual conversion

## Performance Comparison

| Feature | Standard Worlds | ASWM |
|---------|----------------|------|
| World Creation | ~5-10 seconds | Instant |
| Memory Usage | High | Low |
| Disk Space | High | Low |
| I/O Operations | Many | Few |
| Scalability | Limited | High |

## Advanced Configuration

### Custom ASWM Settings
You can modify ASWM's global configuration in `plugins/AdvancedSlimeWorldManager/config.yml` to customize:
- World storage backend
- Compression settings
- Memory limits
- Performance options

### Integration with Other Plugins
SkyeBlock's ASWM integration is compatible with:
- Multiverse-Core
- World management plugins
- Backup plugins that support slime worlds

## Support

If you encounter issues with ASWM integration:
1. Check `/island status` for current status
2. Review server logs for error messages
3. Ensure ASWM and SkyeBlock are up to date
4. Test without ASWM to isolate issues

For ASWM-specific issues, refer to the ASWM documentation: https://infernalsuite.com/docs/asp/
