package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Custom schematic parser for YAML-based island templates
 */
public class CustomSchematicManager {
    
    private final SkyeBlockPlugin plugin;
    private final Map<String, IslandSchematic> schematics;
    
    public CustomSchematicManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.schematics = new HashMap<>();
        loadSchematics();
    }
    
    /**
     * Load all schematic files from resources
     */
    private void loadSchematics() {
        String[] schematicFiles = {"classic.yml", "desert.yml", "nether.yml"};
        
        for (String fileName : schematicFiles) {
            try {
                InputStream stream = plugin.getResource("schematics/" + fileName);
                if (stream != null) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                    IslandSchematic schematic = parseSchematic(config);
                    if (schematic != null) {
                        // Use filename without extension as key (e.g., "classic.yml" -> "classic")
                        String key = fileName.substring(0, fileName.lastIndexOf('.'));
                        schematics.put(key, schematic);
                        plugin.getLogger().info("Loaded schematic: " + schematic.getName() + " (key: " + key + ")");
                    }
                    stream.close();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load schematic: " + fileName, e);
            }
        }
        
        plugin.getLogger().info("Loaded " + schematics.size() + " island schematics");
    }
    
    /**
     * Parse a YAML configuration into an IslandSchematic
     */
    private IslandSchematic parseSchematic(YamlConfiguration config) {
        try {
            String name = config.getString("name");
            String description = config.getString("description");
            
            ConfigurationSection sizeSection = config.getConfigurationSection("size");
            int width = sizeSection.getInt("width");
            int height = sizeSection.getInt("height");
            int length = sizeSection.getInt("length");
            
            ConfigurationSection spawnSection = config.getConfigurationSection("spawn_offset");
            int spawnX = spawnSection.getInt("x");
            int spawnY = spawnSection.getInt("y");
            int spawnZ = spawnSection.getInt("z");
            
            List<Map<?, ?>> structure = config.getMapList("structure");
            List<Map<?, ?>> chestContents = config.getMapList("chest_contents");
            
            return new IslandSchematic(name, description, width, height, length, 
                                     spawnX, spawnY, spawnZ, structure, chestContents);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to parse schematic", e);
            return null;
        }
    }
    
    /**
     * Paste a schematic at the specified location
     */
    public boolean pasteSchematic(String schematicName, Location location) {
        IslandSchematic schematic = schematics.get(schematicName.toLowerCase());
        if (schematic == null) {
            return false;
        }
        
        try {
            World world = location.getWorld();
            int baseX = location.getBlockX();
            int baseY = location.getBlockY();
            int baseZ = location.getBlockZ();
            
            // Paste blocks level by level
            for (Map<?, ?> levelData : schematic.getStructure()) {
                int level = (Integer) levelData.get("level");
                @SuppressWarnings("unchecked")
                List<String> blocks = (List<String>) levelData.get("blocks");
                
                for (int z = 0; z < blocks.size(); z++) {
                    String[] row = blocks.get(z).split(" ");
                    for (int x = 0; x < row.length; x++) {
                        String blockType = row[x];
                        if (!"air".equals(blockType)) {
                            Location blockLoc = new Location(world, 
                                baseX + x - schematic.getWidth() / 2,
                                baseY + level,
                                baseZ + z - schematic.getLength() / 2);
                            
                            setBlock(blockLoc, blockType);
                        }
                    }
                }
            }
            
            // Add chest contents
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                fillChests(schematic, location);
            }, 5L); // Wait 5 ticks for blocks to be placed
            
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to paste schematic: " + schematicName, e);
            return false;
        }
    }
    
    /**
     * Set a block with support for block data
     */
    private void setBlock(Location location, String blockString) {
        try {
            String[] parts = blockString.split("\\[");
            String materialName = parts[0].toUpperCase();
            Material material = Material.valueOf(materialName);
            
            Block block = location.getBlock();
            block.setType(material);
            
            // Handle block data (like chest facing direction)
            if (parts.length > 1) {
                String dataString = parts[1].replace("]", "");
                BlockData blockData = block.getBlockData();
                
                if (dataString.contains("facing=") && blockData instanceof Directional) {
                    String facing = dataString.split("facing=")[1];
                    BlockFace face = BlockFace.valueOf(facing.toUpperCase());
                    ((Directional) blockData).setFacing(face);
                    block.setBlockData(blockData);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set block: " + blockString, e);
        }
    }
    
    /**
     * Fill chests with specified items
     */
    private void fillChests(IslandSchematic schematic, Location baseLocation) {
        for (Map<?, ?> chestData : schematic.getChestContents()) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> loc = (List<Integer>) chestData.get("location");
                @SuppressWarnings("unchecked")
                List<String> items = (List<String>) chestData.get("items");
                
                Location chestLoc = new Location(baseLocation.getWorld(),
                    baseLocation.getBlockX() + loc.get(0) - schematic.getWidth() / 2,
                    baseLocation.getBlockY() + loc.get(1),
                    baseLocation.getBlockZ() + loc.get(2) - schematic.getLength() / 2);
                
                Block block = chestLoc.getBlock();
                if (block.getState() instanceof Chest) {
                    Chest chest = (Chest) block.getState();
                    Inventory inventory = chest.getInventory();
                    
                    for (String itemString : items) {
                        String[] itemParts = itemString.split(":");
                        Material material = Material.valueOf(itemParts[0].toUpperCase());
                        int amount = Integer.parseInt(itemParts[1]);
                        inventory.addItem(new ItemStack(material, amount));
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to fill chest", e);
            }
        }
    }
    
    /**
     * Get spawn location for a schematic
     */
    public Location getSpawnLocation(String schematicName, Location pasteLocation) {
        IslandSchematic schematic = schematics.get(schematicName.toLowerCase());
        if (schematic == null) {
            return pasteLocation;
        }
        
        return new Location(pasteLocation.getWorld(),
            pasteLocation.getBlockX() + schematic.getSpawnX() - schematic.getWidth() / 2,
            pasteLocation.getBlockY() + schematic.getSpawnY(),
            pasteLocation.getBlockZ() + schematic.getSpawnZ() - schematic.getLength() / 2);
    }
    
    /**
     * Get available schematic names
     */
    public String[] getAvailableSchematics() {
        return schematics.keySet().toArray(new String[0]);
    }
    
    /**
     * Get schematic by name
     */
    public IslandSchematic getSchematic(String name) {
        return schematics.get(name.toLowerCase());
    }
    
    /**
     * Inner class to represent a parsed schematic
     */
    public static class IslandSchematic {
        private final String name;
        private final String description;
        private final int width, height, length;
        private final int spawnX, spawnY, spawnZ;
        private final List<Map<?, ?>> structure;
        private final List<Map<?, ?>> chestContents;
        
        public IslandSchematic(String name, String description, int width, int height, int length,
                             int spawnX, int spawnY, int spawnZ, List<Map<?, ?>> structure,
                             List<Map<?, ?>> chestContents) {
            this.name = name;
            this.description = description;
            this.width = width;
            this.height = height;
            this.length = length;
            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.spawnZ = spawnZ;
            this.structure = structure;
            this.chestContents = chestContents;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getLength() { return length; }
        public int getSpawnX() { return spawnX; }
        public int getSpawnY() { return spawnY; }
        public int getSpawnZ() { return spawnZ; }
        public List<Map<?, ?>> getStructure() { return structure; }
        public List<Map<?, ?>> getChestContents() { return chestContents; }
    }
}
