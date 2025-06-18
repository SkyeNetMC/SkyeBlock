package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data such as last locations and preferences
 */
public class PlayerDataManager {
    private final SkyeBlockPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    private final Map<UUID, Location> lastLocations;
    
    public PlayerDataManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.lastLocations = new HashMap<>();
        loadDataFile();
        loadPlayerData();
    }
    
    private void loadDataFile() {
        dataFile = new File(plugin.getDataFolder(), "player-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create player data file: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Load all player data from persistent storage
     */
    private void loadPlayerData() {
        if (!dataConfig.contains("players")) {
            return;
        }
        
        for (String uuidString : dataConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(uuidString);
                String basePath = "players." + uuidString;
                
                // Load last location
                if (dataConfig.contains(basePath + ".last-location.world")) {
                    String worldName = dataConfig.getString(basePath + ".last-location.world");
                    org.bukkit.World world = plugin.getServer().getWorld(worldName);
                    
                    if (world != null) {
                        double x = dataConfig.getDouble(basePath + ".last-location.x");
                        double y = dataConfig.getDouble(basePath + ".last-location.y");
                        double z = dataConfig.getDouble(basePath + ".last-location.z");
                        float yaw = (float) dataConfig.getDouble(basePath + ".last-location.yaw");
                        float pitch = (float) dataConfig.getDouble(basePath + ".last-location.pitch");
                        
                        Location lastLocation = new Location(world, x, y, z, yaw, pitch);
                        lastLocations.put(playerUUID, lastLocation);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load player data for " + uuidString + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded player data for " + lastLocations.size() + " players");
    }
    
    /**
     * Save all player data to persistent storage
     */
    public void savePlayerData() {
        // Clear existing player data
        dataConfig.set("players", null);
        
        for (Map.Entry<UUID, Location> entry : lastLocations.entrySet()) {
            UUID playerUUID = entry.getKey();
            Location location = entry.getValue();
            
            String basePath = "players." + playerUUID.toString();
            
            // Save last location
            if (location != null && location.getWorld() != null) {
                dataConfig.set(basePath + ".last-location.world", location.getWorld().getName());
                dataConfig.set(basePath + ".last-location.x", location.getX());
                dataConfig.set(basePath + ".last-location.y", location.getY());
                dataConfig.set(basePath + ".last-location.z", location.getZ());
                dataConfig.set(basePath + ".last-location.yaw", location.getYaw());
                dataConfig.set(basePath + ".last-location.pitch", location.getPitch());
            }
        }
        
        try {
            dataConfig.save(dataFile);
            plugin.getLogger().info("Saved player data for " + lastLocations.size() + " players");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }
    
    /**
     * Update a player's last location
     */
    public void updateLastLocation(Player player) {
        updateLastLocation(player.getUniqueId(), player.getLocation());
    }
    
    /**
     * Update a player's last location
     */
    public void updateLastLocation(UUID playerUUID, Location location) {
        if (location != null && location.getWorld() != null) {
            lastLocations.put(playerUUID, location.clone());
        }
    }
    
    /**
     * Get a player's last location
     */
    public Location getLastLocation(UUID playerUUID) {
        return lastLocations.get(playerUUID);
    }
    
    /**
     * Get a player's last location
     */
    public Location getLastLocation(Player player) {
        return getLastLocation(player.getUniqueId());
    }
    
    /**
     * Check if a player has a stored last location
     */
    public boolean hasLastLocation(UUID playerUUID) {
        return lastLocations.containsKey(playerUUID);
    }
    
    /**
     * Check if a player has a stored last location
     */
    public boolean hasLastLocation(Player player) {
        return hasLastLocation(player.getUniqueId());
    }
    
    /**
     * Remove a player's last location data
     */
    public void removePlayerData(UUID playerUUID) {
        lastLocations.remove(playerUUID);
    }
    
    /**
     * Remove a player's last location data
     */
    public void removePlayerData(Player player) {
        removePlayerData(player.getUniqueId());
    }
}
