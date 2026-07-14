package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data such as last locations and preferences via H2 database
 */
public class PlayerDataManager {
    private final SkyeBlockPlugin plugin;
    private final DatabaseManager db;
    private final Map<UUID, Location> lastLocations;

    public PlayerDataManager(SkyeBlockPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        this.lastLocations = new HashMap<>();
        loadPlayerData();
    }

    /**
     * Load all player data from persistent storage
     */
    private void loadPlayerData() {
        lastLocations.putAll(db.loadAllPlayerData());
        plugin.getLogger().info("Loaded player data for " + lastLocations.size() + " players from H2");
    }

    /**
     * Save all player data to persistent storage
     */
    public void savePlayerData() {
        db.saveAllPlayerData(lastLocations);
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
        db.deletePlayerData(playerUUID);
    }

    /**
     * Remove a player's last location data
     */
    public void removePlayerData(Player player) {
        removePlayerData(player.getUniqueId());
    }
}
