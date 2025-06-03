package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages persistent storage of island data
 */
public class IslandDataManager {
    private final SkyeBlockPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public IslandDataManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        loadDataFile();
    }
    
    private void loadDataFile() {
        dataFile = new File(plugin.getDataFolder(), "island-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to create island data file: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }
    
    /**
     * Save all island data to persistent storage
     */
    public void saveAllIslands(Map<UUID, Island> islands) {
        // Clear existing data
        dataConfig.set("islands", null);
        
        for (Map.Entry<UUID, Island> entry : islands.entrySet()) {
            UUID ownerUUID = entry.getKey();
            Island island = entry.getValue();
            
            String basePath = "islands." + ownerUUID.toString();
            
            // Basic island data
            dataConfig.set(basePath + ".island-id", island.getIslandId());
            dataConfig.set(basePath + ".island-type", island.getIslandType());
            dataConfig.set(basePath + ".display-title", island.getDisplayTitle());
            dataConfig.set(basePath + ".display-description", island.getDisplayDescription());
            dataConfig.set(basePath + ".locked", island.isLocked());
            dataConfig.set(basePath + ".adventure-mode-for-visitors", island.isAdventureModeForVisitors());
            dataConfig.set(basePath + ".last-online-time", island.getLastOnlineTime());
            
            // Save location
            Location loc = island.getLocation();
            if (loc != null) {
                dataConfig.set(basePath + ".location.world", loc.getWorld().getName());
                dataConfig.set(basePath + ".location.x", loc.getX());
                dataConfig.set(basePath + ".location.y", loc.getY());
                dataConfig.set(basePath + ".location.z", loc.getZ());
                dataConfig.set(basePath + ".location.yaw", loc.getYaw());
                dataConfig.set(basePath + ".location.pitch", loc.getPitch());
            }
            
            // Save home location
            Location homeLoc = island.getHomeLocation();
            if (homeLoc != null) {
                dataConfig.set(basePath + ".home-location.world", homeLoc.getWorld().getName());
                dataConfig.set(basePath + ".home-location.x", homeLoc.getX());
                dataConfig.set(basePath + ".home-location.y", homeLoc.getY());
                dataConfig.set(basePath + ".home-location.z", homeLoc.getZ());
                dataConfig.set(basePath + ".home-location.yaw", homeLoc.getYaw());
                dataConfig.set(basePath + ".home-location.pitch", homeLoc.getPitch());
            }
            
            // Save visit location
            Location visitLoc = island.getVisitLocation();
            if (visitLoc != null) {
                dataConfig.set(basePath + ".visit-location.world", visitLoc.getWorld().getName());
                dataConfig.set(basePath + ".visit-location.x", visitLoc.getX());
                dataConfig.set(basePath + ".visit-location.y", visitLoc.getY());
                dataConfig.set(basePath + ".visit-location.z", visitLoc.getZ());
                dataConfig.set(basePath + ".visit-location.yaw", visitLoc.getYaw());
                dataConfig.set(basePath + ".visit-location.pitch", visitLoc.getPitch());
            }
            
            // Save display icon
            ItemStack icon = island.getDisplayIcon();
            if (icon != null) {
                dataConfig.set(basePath + ".display-icon", icon);
            }
            
            // Save coop members
            Map<UUID, Island.CoopRole> coopMembers = island.getCoopMembers();
            if (!coopMembers.isEmpty()) {
                for (Map.Entry<UUID, Island.CoopRole> coopEntry : coopMembers.entrySet()) {
                    String coopPath = basePath + ".coop-members." + coopEntry.getKey().toString();
                    dataConfig.set(coopPath, coopEntry.getValue().name());
                }
            }
            
            // Save pending invites
            Set<UUID> pendingInvites = island.getPendingInvites();
            if (!pendingInvites.isEmpty()) {
                List<String> inviteList = new ArrayList<>();
                for (UUID invite : pendingInvites) {
                    inviteList.add(invite.toString());
                }
                dataConfig.set(basePath + ".pending-invites", inviteList);
            }
            
            // Save votes (only recent ones)
            for (Map.Entry<UUID, Long> voteEntry : island.getVotes().entrySet()) {
                // Only save votes from the last 7 days
                long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
                if (voteEntry.getValue() > sevenDaysAgo) {
                    String votePath = basePath + ".votes." + voteEntry.getKey().toString();
                    dataConfig.set(votePath, voteEntry.getValue());
                }
            }
        }
        
        try {
            dataConfig.save(dataFile);
            plugin.getLogger().info("Saved " + islands.size() + " islands to persistent storage");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save island data: " + e.getMessage());
        }
    }
    
    /**
     * Load all island data from persistent storage
     */
    public Map<UUID, Island> loadAllIslands() {
        Map<UUID, Island> islands = new HashMap<>();
        
        if (!dataConfig.contains("islands")) {
            return islands;
        }
        
        for (String uuidString : dataConfig.getConfigurationSection("islands").getKeys(false)) {
            try {
                UUID ownerUUID = UUID.fromString(uuidString);
                String basePath = "islands." + uuidString;
                
                // Load basic data
                String islandId = dataConfig.getString(basePath + ".island-id");
                String islandType = dataConfig.getString(basePath + ".island-type");
                
                // Load location
                String worldName = dataConfig.getString(basePath + ".location.world");
                if (worldName == null) {
                    plugin.getLogger().warning("Skipping island " + islandId + " - no world specified");
                    continue;
                }
                
                // Check if world exists or can be loaded
                org.bukkit.World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    // Try to load the world through WorldManager
                    world = plugin.getWorldManager().getIslandWorld(islandId);
                    if (world == null) {
                        plugin.getLogger().warning("Skipping island " + islandId + " - world " + worldName + " not found");
                        continue;
                    }
                }
                
                double x = dataConfig.getDouble(basePath + ".location.x");
                double y = dataConfig.getDouble(basePath + ".location.y");
                double z = dataConfig.getDouble(basePath + ".location.z");
                float yaw = (float) dataConfig.getDouble(basePath + ".location.yaw");
                float pitch = (float) dataConfig.getDouble(basePath + ".location.pitch");
                
                Location location = new Location(world, x, y, z, yaw, pitch);
                
                // Create island object
                Island island = new Island(ownerUUID, islandType, location);
                
                // Load optional data
                if (dataConfig.contains(basePath + ".display-title")) {
                    island.setDisplayTitle(dataConfig.getString(basePath + ".display-title"));
                }
                if (dataConfig.contains(basePath + ".display-description")) {
                    island.setDisplayDescription(dataConfig.getString(basePath + ".display-description"));
                }
                if (dataConfig.contains(basePath + ".locked")) {
                    island.setLocked(dataConfig.getBoolean(basePath + ".locked"));
                }
                if (dataConfig.contains(basePath + ".adventure-mode-for-visitors")) {
                    island.setAdventureModeForVisitors(dataConfig.getBoolean(basePath + ".adventure-mode-for-visitors"));
                }
                if (dataConfig.contains(basePath + ".last-online-time")) {
                    // Use reflection to set the last online time (as there's no setter)
                    try {
                        java.lang.reflect.Field field = Island.class.getDeclaredField("lastOnlineTime");
                        field.setAccessible(true);
                        field.set(island, dataConfig.getLong(basePath + ".last-online-time"));
                    } catch (Exception e) {
                        // Fallback - update to current time
                        island.updateLastOnlineTime();
                    }
                }
                
                // Load home location
                if (dataConfig.contains(basePath + ".home-location.world")) {
                    String homeWorldName = dataConfig.getString(basePath + ".home-location.world");
                    org.bukkit.World homeWorld = plugin.getServer().getWorld(homeWorldName);
                    if (homeWorld != null) {
                        double hx = dataConfig.getDouble(basePath + ".home-location.x");
                        double hy = dataConfig.getDouble(basePath + ".home-location.y");
                        double hz = dataConfig.getDouble(basePath + ".home-location.z");
                        float hyaw = (float) dataConfig.getDouble(basePath + ".home-location.yaw");
                        float hpitch = (float) dataConfig.getDouble(basePath + ".home-location.pitch");
                        
                        island.setHomeLocation(new Location(homeWorld, hx, hy, hz, hyaw, hpitch));
                    }
                }
                
                // Load visit location
                if (dataConfig.contains(basePath + ".visit-location.world")) {
                    String visitWorldName = dataConfig.getString(basePath + ".visit-location.world");
                    org.bukkit.World visitWorld = plugin.getServer().getWorld(visitWorldName);
                    if (visitWorld != null) {
                        double vx = dataConfig.getDouble(basePath + ".visit-location.x");
                        double vy = dataConfig.getDouble(basePath + ".visit-location.y");
                        double vz = dataConfig.getDouble(basePath + ".visit-location.z");
                        float vyaw = (float) dataConfig.getDouble(basePath + ".visit-location.yaw");
                        float vpitch = (float) dataConfig.getDouble(basePath + ".visit-location.pitch");
                        
                        island.setVisitLocation(new Location(visitWorld, vx, vy, vz, vyaw, vpitch));
                    }
                }
                
                // Load display icon
                if (dataConfig.contains(basePath + ".display-icon")) {
                    ItemStack icon = dataConfig.getItemStack(basePath + ".display-icon");
                    island.setDisplayIcon(icon);
                }
                
                // Load coop members
                if (dataConfig.contains(basePath + ".coop-members")) {
                    for (String coopUuidString : dataConfig.getConfigurationSection(basePath + ".coop-members").getKeys(false)) {
                        try {
                            UUID coopUuid = UUID.fromString(coopUuidString);
                            String roleName = dataConfig.getString(basePath + ".coop-members." + coopUuidString);
                            Island.CoopRole role = Island.CoopRole.valueOf(roleName);
                            island.addCoopMember(coopUuid, role);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load coop member " + coopUuidString + " for island " + islandId);
                        }
                    }
                }
                
                // Load pending invites
                if (dataConfig.contains(basePath + ".pending-invites")) {
                    List<String> inviteStrings = dataConfig.getStringList(basePath + ".pending-invites");
                    for (String inviteString : inviteStrings) {
                        try {
                            UUID inviteUuid = UUID.fromString(inviteString);
                            island.addPendingInvite(inviteUuid);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load pending invite " + inviteString + " for island " + islandId);
                        }
                    }
                }
                
                // Load votes
                if (dataConfig.contains(basePath + ".votes")) {
                    for (String voteUuidString : dataConfig.getConfigurationSection(basePath + ".votes").getKeys(false)) {
                        try {
                            UUID voteUuid = UUID.fromString(voteUuidString);
                            long voteTime = dataConfig.getLong(basePath + ".votes." + voteUuidString);
                            // Directly add to votes map to preserve timestamp
                            island.getVotes().put(voteUuid, voteTime);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load vote " + voteUuidString + " for island " + islandId);
                        }
                    }
                }
                
                islands.put(ownerUUID, island);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load island for UUID " + uuidString + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + islands.size() + " islands from persistent storage");
        return islands;
    }
    
    /**
     * Save a single island to persistent storage
     */
    public void saveIsland(Island island) {
        Map<UUID, Island> singleIslandMap = new HashMap<>();
        singleIslandMap.put(island.getOwnerUUID(), island);
        
        // Load existing data first
        Map<UUID, Island> allIslands = loadAllIslands();
        allIslands.put(island.getOwnerUUID(), island);
        
        // Save all data
        saveAllIslands(allIslands);
    }
    
    /**
     * Delete an island from persistent storage
     */
    public void deleteIsland(UUID ownerUUID) {
        if (dataConfig.contains("islands." + ownerUUID.toString())) {
            dataConfig.set("islands." + ownerUUID.toString(), null);
            try {
                dataConfig.save(dataFile);
                plugin.getLogger().info("Deleted island data for player " + ownerUUID);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to delete island data: " + e.getMessage());
            }
        }
    }
}
