# SkyeBlock Plugin Testing Guide

## Quick Test Setup

### 1. Automatic Setup
Run the provided setup script:
```bash
./test-setup.sh
```

### 2. Manual Testing Steps

1. **Start a Paper/Spigot 1.20+ server** with the plugin installed
2. **Join the server** and test these commands:

```
/island types
# Should show: classic, desert, nether with descriptions

/island create classic
# Should create a classic skyblock island and teleport you there

/island tp
# Should teleport you back to your island

/island help
# Should show all available commands
```

### 3. Expected Results

#### Classic Island:
- Small dirt platform with grass
- Oak sapling and chest
- Chest contains: ice, lava bucket, seeds, food, bone meal

#### Desert Island:
- Sand platform with sandstone base
- Cactus plants and dead bushes  
- Chest contains: water bucket, cactus, sand, seeds, food

#### Nether Island:
- Netherrack platform with blackstone base
- Soul sand with nether wart
- Chest contains: water buckets, obsidian, nether items

### 4. Admin Testing

If you have admin permissions, test:
```
/island list
# Shows all created islands

/island delete
# Deletes your current island
```

### 5. Troubleshooting

**Plugin not loading?**
- Check server logs for errors
- Ensure Java 17+ and Paper/Spigot 1.20+
- Verify plugin JAR is in plugins/ folder

**Islands not generating?**
- Check if skyblock world was created
- Look for "SkyeBlock" messages in server console
- Try `/island create classic` (make sure you don't already have an island)

**Custom schematics not working?**
- Check YAML syntax in schematic files
- Verify block names are valid Minecraft materials
- Look for parsing errors in server logs

### 6. Performance Notes

- Islands are spaced 1000 blocks apart by default
- Each island type has different size requirements
- Plugin uses minimal resources with custom schematic system
- No WorldEdit dependency required
