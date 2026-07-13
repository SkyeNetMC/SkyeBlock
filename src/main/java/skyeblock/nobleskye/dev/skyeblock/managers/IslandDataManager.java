package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;

import java.util.*;

/**
 * Manages persistent storage of island data via H2 database
 */
public class IslandDataManager {
    private final SkyeBlockPlugin plugin;
    private final DatabaseManager db;

    public IslandDataManager(SkyeBlockPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
    }

    /**
     * Save all island data to persistent storage
     */
    public void saveAllIslands(Map<UUID, Island> islands) {
        db.saveAllIslands(islands);
    }

    /**
     * Load all island data from persistent storage
     */
    public Map<UUID, Island> loadAllIslands() {
        return db.loadAllIslands();
    }

    /**
     * Save a single island to persistent storage
     */
    public void saveIsland(Island island) {
        db.saveIsland(island);
    }

    /**
     * Delete an island from persistent storage
     */
    public void deleteIsland(UUID ownerUUID) {
        // Get island ID before deleting so we can clean up settings too
        db.deleteIsland(ownerUUID);
    }

    /**
     * Load a specific island for a player
     */
    public Island loadIsland(UUID playerUUID) {
        Map<UUID, Island> singleMap = db.loadAllIslands();
        return singleMap.get(playerUUID);
    }
}
