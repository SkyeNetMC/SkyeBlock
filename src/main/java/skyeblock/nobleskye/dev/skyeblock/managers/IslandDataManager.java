package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import skyeblock.nobleskye.dev.skyeblock.permissions.IslandPermission;
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
            
            // Save granular visitor permissions
            dataConfig.set(basePath + ".visitor-permissions.can-break-blocks", island.canVisitorBreakBlocks());
            dataConfig.set(basePath + ".visitor-permissions.can-place-blocks", island.canVisitorPlaceBlocks());
            dataConfig.set(basePath + ".visitor-permissions.can-open-containers", island.canVisitorOpenContainers());
            dataConfig.set(basePath + ".visitor-permissions.can-pickup-items", island.canVisitorPickupItems());
            dataConfig.set(basePath + ".visitor-permissions.can-drop-items", island.canVisitorDropItems());
            dataConfig.set(basePath + ".visitor-permissions.can-interact-entities", island.canVisitorInteractWithEntities());
            dataConfig.set(basePath + ".visitor-permissions.can-use-pvp", island.canVisitorUsePvp());
            dataConfig.set(basePath + ".visitor-permissions.can-use-redstone", island.canVisitorUseRedstone());
            
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
            
            // Save custom permissions
            Map<UUID, Set<IslandPermission>> customPermissions = island.getCustomPlayerPermissions();
            if (!customPermissions.isEmpty()) {
                for (Map.Entry<UUID, Set<IslandPermission>> permEntry : customPermissions.entrySet()) {
                    String permPath = basePath + ".custom-permissions." + permEntry.getKey().toString();
                    List<String> permissionNodes = new ArrayList<>();
                    for (IslandPermission perm : permEntry.getValue()) {
                        permissionNodes.add(perm.getNode());
                    }
                    dataConfig.set(permPath, permissionNodes);
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
                    world = plugin.getWorldManager().getOrLoadIslandWorld(islandId);
                    if (world == null || world.getName().equals(plugin.getWorldManager().getSkyBlockWorld().getName())) {
                        plugin.getLogger().warning("Skipping island " + islandId + " - world " + worldName + " could not be loaded");
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
                
                // Load granular visitor permissions
                if (dataConfig.contains(basePath + ".visitor-permissions.can-break-blocks")) {
                    island.setVisitorCanBreakBlocks(dataConfig.getBoolean(basePath + ".visitor-permissions.can-break-blocks"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-place-blocks")) {
                    island.setVisitorCanPlaceBlocks(dataConfig.getBoolean(basePath + ".visitor-permissions.can-place-blocks"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-open-containers")) {
                    island.setVisitorCanOpenContainers(dataConfig.getBoolean(basePath + ".visitor-permissions.can-open-containers"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-pickup-items")) {
                    island.setVisitorCanPickupItems(dataConfig.getBoolean(basePath + ".visitor-permissions.can-pickup-items"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-drop-items")) {
                    island.setVisitorCanDropItems(dataConfig.getBoolean(basePath + ".visitor-permissions.can-drop-items"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-interact-entities")) {
                    island.setVisitorCanInteractWithEntities(dataConfig.getBoolean(basePath + ".visitor-permissions.can-interact-entities"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-use-pvp")) {
                    island.setVisitorCanUsePvp(dataConfig.getBoolean(basePath + ".visitor-permissions.can-use-pvp"));
                }
                if (dataConfig.contains(basePath + ".visitor-permissions.can-use-redstone")) {
                    island.setVisitorCanUseRedstone(dataConfig.getBoolean(basePath + ".visitor-permissions.can-use-redstone"));
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
                    
                    // If world not found, try to load it through WorldManager
                    if (homeWorld == null) {
                        homeWorld = plugin.getWorldManager().getIslandWorld(islandId);
                    }
                    
                    if (homeWorld != null) {
                        double hx = dataConfig.getDouble(basePath + ".home-location.x");
                        double hy = dataConfig.getDouble(basePath + ".home-location.y");
                        double hz = dataConfig.getDouble(basePath + ".home-location.z");
                        float hyaw = (float) dataConfig.getDouble(basePath + ".home-location.yaw");
                        float hpitch = (float) dataConfig.getDouble(basePath + ".home-location.pitch");
                        
                        island.setHomeLocation(new Location(homeWorld, hx, hy, hz, hyaw, hpitch));
                    } else {
                        plugin.getLogger().warning("Could not load home location for island " + islandId + " - world " + homeWorldName + " not found");
                    }
                }
                
                // Load visit location
                if (dataConfig.contains(basePath + ".visit-location.world")) {
                    String visitWorldName = dataConfig.getString(basePath + ".visit-location.world");
                    org.bukkit.World visitWorld = plugin.getServer().getWorld(visitWorldName);
                    
                    // If world not found, try to load it through WorldManager
                    if (visitWorld == null) {
                        visitWorld = plugin.getWorldManager().getIslandWorld(islandId);
                    }
                    
                    if (visitWorld != null) {
                        double vx = dataConfig.getDouble(basePath + ".visit-location.x");
                        double vy = dataConfig.getDouble(basePath + ".visit-location.y");
                        double vz = dataConfig.getDouble(basePath + ".visit-location.z");
                        float vyaw = (float) dataConfig.getDouble(basePath + ".visit-location.yaw");
                        float vpitch = (float) dataConfig.getDouble(basePath + ".visit-location.pitch");
                        
                        island.setVisitLocation(new Location(visitWorld, vx, vy, vz, vyaw, vpitch));
                    } else {
                        plugin.getLogger().warning("Could not load visit location for island " + islandId + " - world " + visitWorldName + " not found");
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
                
                // Load custom permissions
                if (dataConfig.contains(basePath + ".custom-permissions")) {
                    for (String permUuidString : dataConfig.getConfigurationSection(basePath + ".custom-permissions").getKeys(false)) {
                        try {
                            UUID playerUuid = UUID.fromString(permUuidString);
                            List<String> permissionNodes = dataConfig.getStringList(basePath + ".custom-permissions." + permUuidString);
                            
                            for (String node : permissionNodes) {
                                for (IslandPermission perm : IslandPermission.values()) {
                                    if (perm.getNode().equals(node)) {
                                        island.addCustomPermission(playerUuid, perm);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to load custom permissions for player " + permUuidString + " on island " + islandId);
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
    
    /**
     * Load a specific island for a player
     */
    public Island loadIsland(UUID playerUUID) {
        if (!dataConfig.contains("islands." + playerUUID.toString())) {
            return null;
        }
        
        try {
            String basePath = "islands." + playerUUID.toString();
            
            // Load basic data
            String islandId = dataConfig.getString(basePath + ".island-id");
            String islandType = dataConfig.getString(basePath + ".island-type");
            
            if (islandId == null) {
                plugin.getLogger().warning("Island data for " + playerUUID + " has no island-id");
                return null;
            }
            
            // Load location
            String worldName = dataConfig.getString(basePath + ".location.world");
            if (worldName == null) {
                plugin.getLogger().warning("Island " + islandId + " has no world specified");
                return null;
            }
            
            // Try to get or load the world
            org.bukkit.World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                // Try to load the world through WorldManager
                world = plugin.getWorldManager().getIslandWorld(islandId);
                if (world == null) {
                    plugin.getLogger().warning("Could not load world " + worldName + " for island " + islandId);
                    // Continue anyway - the world might be loaded later
                    world = plugin.getWorldManager().getSkyBlockWorld(); // Use main world as fallback
                }
            }
            
            double x = dataConfig.getDouble(basePath + ".location.x");
            double y = dataConfig.getDouble(basePath + ".location.y");
            double z = dataConfig.getDouble(basePath + ".location.z");
            float yaw = (float) dataConfig.getDouble(basePath + ".location.yaw");
            float pitch = (float) dataConfig.getDouble(basePath + ".location.pitch");
            
            Location location = new Location(world, x, y, z, yaw, pitch);
            
            // Create island object
            Island island = new Island(playerUUID, islandType != null ? islandType : "vanilla", location);
            
            // Load additional properties
            if (dataConfig.contains(basePath + ".locked")) {
                island.setLocked(dataConfig.getBoolean(basePath + ".locked"));
            }
            
            // Load home location if it exists
            if (dataConfig.contains(basePath + ".home-location")) {
                String homeWorldName = dataConfig.getString(basePath + ".home-location.world");
                org.bukkit.World homeWorld = plugin.getServer().getWorld(homeWorldName != null ? homeWorldName : worldName);
                if (homeWorld != null) {
                    double homeX = dataConfig.getDouble(basePath + ".home-location.x");
                    double homeY = dataConfig.getDouble(basePath + ".home-location.y");
                    double homeZ = dataConfig.getDouble(basePath + ".home-location.z");
                    float homeYaw = (float) dataConfig.getDouble(basePath + ".home-location.yaw");
                    float homePitch = (float) dataConfig.getDouble(basePath + ".home-location.pitch");
                    
                    island.setHomeLocation(new Location(homeWorld, homeX, homeY, homeZ, homeYaw, homePitch));
                }
            }
            
            // Load visit location if it exists
            if (dataConfig.contains(basePath + ".visit-location")) {
                String visitWorldName = dataConfig.getString(basePath + ".visit-location.world");
                org.bukkit.World visitWorld = plugin.getServer().getWorld(visitWorldName != null ? visitWorldName : worldName);
                if (visitWorld != null) {
                    double visitX = dataConfig.getDouble(basePath + ".visit-location.x");
                    double visitY = dataConfig.getDouble(basePath + ".visit-location.y");
                    double visitZ = dataConfig.getDouble(basePath + ".visit-location.z");
                    float visitYaw = (float) dataConfig.getDouble(basePath + ".visit-location.yaw");
                    float visitPitch = (float) dataConfig.getDouble(basePath + ".visit-location.pitch");
                    
                    island.setVisitLocation(new Location(visitWorld, visitX, visitY, visitZ, visitYaw, visitPitch));
                }
            }
            
            // Load coop players (using the correct method name)
            if (dataConfig.contains(basePath + ".coop-players")) {
                List<String> coopPlayerStrings = dataConfig.getStringList(basePath + ".coop-players");
                for (String coopPlayerString : coopPlayerStrings) {
                    try {
                        UUID coopUUID = UUID.fromString(coopPlayerString);
                        // Add as coop member with default role
                        island.addCoopMember(coopUUID, Island.CoopRole.MEMBER);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid coop player UUID: " + coopPlayerString);
                    }
                }
            }
            
            plugin.getLogger().info("Successfully loaded island " + islandId + " for player " + playerUUID);
            return island;
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading island for player " + playerUUID + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
