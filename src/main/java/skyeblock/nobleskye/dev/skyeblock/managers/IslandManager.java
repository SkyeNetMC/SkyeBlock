package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandManager {
    private final SkyeBlockPlugin plugin;
    private final Map<UUID, Island> playerIslands;
    private final MiniMessage miniMessage;
    private IslandDataManager dataManager;

    public IslandManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.playerIslands = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    /**
     * Initialize the data manager and load existing islands
     */
    public void initialize() {
        this.dataManager = new IslandDataManager(plugin);
        loadAllIslands();
    }
    
    /**
     * Load all islands from persistent storage
     */
    private void loadAllIslands() {
        Map<UUID, Island> loadedIslands = dataManager.loadAllIslands();
        playerIslands.putAll(loadedIslands);
        plugin.getLogger().info("Loaded " + loadedIslands.size() + " islands");
    }
    
    /**
     * Save all islands to persistent storage
     */
    public void saveAllIslands() {
        if (dataManager != null) {
            dataManager.saveAllIslands(playerIslands);
        }
    }

    public boolean hasIsland(UUID playerUUID) {
        return playerIslands.containsKey(playerUUID);
    }

    public Island getIsland(UUID playerUUID) {
        return playerIslands.get(playerUUID);
    }
    
    public List<Island> getAllIslands() {
        return new ArrayList<>(playerIslands.values());
    }

    public boolean createIsland(Player player, String islandType) {
        UUID playerUUID = player.getUniqueId();
        
        // Check if player already has an island
        if (hasIsland(playerUUID)) {
            return false;
        }

        // Validate island type
        if (!isValidIslandType(islandType)) {
            return false;
        }

        // Create island ID
        String islandId = "island-" + islandType + "-" + playerUUID.toString();
        
        // Create individual world for this island
        World islandWorld = plugin.getWorldManager().createIslandWorld(islandId);
        if (islandWorld == null) {
            return false;
        }

        // Calculate location for new island (center of the world)
        Location islandLocation = new Location(islandWorld, 0, 100, 0);
        
        // Create the island object
        Island island = new Island(playerUUID, islandType, islandLocation);
        
        // Paste the schematic using WorldEdit schematic manager
        boolean success = plugin.getSchematicManager().pasteSchematic(islandType, islandLocation);
        if (!success) {
            // Clean up the world if schematic failed
            plugin.getWorldManager().deleteIslandWorld(islandId);
            return false;
        }

        // Store the island
        playerIslands.put(playerUUID, island);
        
        // Save island to persistent storage
        if (dataManager != null) {
            dataManager.saveIsland(island);
        }
        
        // Create default settings for the island and apply them to the world
        plugin.getIslandSettingsManager().createDefaultSettings(islandId);
        plugin.getIslandSettingsManager().applySettingsToWorld(islandId, islandWorld);
        
        // Create a corresponding nether island if this isn't already a nether island
        if (!islandType.equals("nether")) {
            createNetherIsland(playerUUID, island);
        }
        
        plugin.getLogger().info("Created island " + islandId + " for player " + player.getName());
        return true;
    }

    /**
     * Create a nether island for a player who already has a main island
     */
    private void createNetherIsland(UUID playerUUID, Island mainIsland) {
        try {
            // Only create nether island if nether worlds are enabled
            if (!plugin.getWorldManager().hasNetherWorld()) {
                plugin.getLogger().info("Nether worlds not enabled, skipping nether island creation for " + playerUUID);
                return;
            }
            
            // Use the same world as the main island to allow portal sync
            World islandWorld = mainIsland.getLocation().getWorld();
            if (islandWorld == null) {
                plugin.getLogger().warning("Main island world is null, cannot create nether island for " + playerUUID);
                return;
            }

            // Calculate nether location in the same world (offset to nether coordinates)
            // Standard nether coordinate conversion: divide by 8 and offset to avoid overlap
            Location mainLocation = mainIsland.getLocation();
            Location netherIslandLocation = new Location(islandWorld, 
                mainLocation.getX() / 8, // Standard nether coordinate scaling
                64, // Lower Y level for nether feel
                mainLocation.getZ() / 8);
            
            // Create nether island ID for settings and data management
            String netherIslandId = "island-nether-" + playerUUID.toString();
            
            // Create the nether island object
            Island netherIsland = new Island(playerUUID, "nether", netherIslandLocation);
            
            // Paste the nether schematic
            boolean success = plugin.getSchematicManager().pasteSchematic("nether", netherIslandLocation);
            if (!success) {
                plugin.getLogger().warning("Failed to paste nether schematic for island: " + netherIslandId);
                return;
            }

            // Save nether island to persistent storage
            if (dataManager != null) {
                dataManager.saveIsland(netherIsland);
            }
            
            // Create default settings for the nether island and apply them
            plugin.getIslandSettingsManager().createDefaultSettings(netherIslandId);
            plugin.getIslandSettingsManager().applySettingsToWorld(netherIslandId, islandWorld);
            
            // Set the nether area biome to NETHER_WASTES
            setNetherBiome(netherIslandLocation, 32);
            
            plugin.getLogger().info("Created nether area in main island world " + netherIslandId + " for player " + playerUUID);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create nether island for " + playerUUID + ": " + e.getMessage());
        }
    }
    
    /**
     * Set a square area around the location to nether biome
     */
    private void setNetherBiome(Location center, int radius) {
        try {
            World world = center.getWorld();
            if (world == null) return;
            
            org.bukkit.block.Biome netherBiome = org.bukkit.block.Biome.NETHER_WASTES;
            int centerX = center.getBlockX();
            int centerZ = center.getBlockZ();
            
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    world.setBiome(x, 64, z, netherBiome); // Set at nether level
                }
            }
            
            plugin.getLogger().info("Set nether biome in " + (radius * 2 + 1) + "x" + (radius * 2 + 1) + " area");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set nether biome: " + e.getMessage());
        }
    }

    public boolean teleportToIsland(Player player) {
        UUID playerUUID = player.getUniqueId();
        Island island = getIsland(playerUUID);
        
        if (island == null) {
            return false;
        }

        return teleportToIsland(player, island);
    }

    public boolean teleportToIsland(Player player, Island island) {
        // Get the island's world
        World islandWorld = plugin.getWorldManager().getIslandWorld(island.getIslandId());
        if (islandWorld == null) {
            return false;
        }

        // Check if player is visiting (not owner) and island is locked
        UUID playerUUID = player.getUniqueId();
        boolean isOwner = island.getOwnerUUID().equals(playerUUID);
        boolean hasCoopAccess = island.hasCoopAccess(playerUUID);
        
        if (!isOwner && island.isLocked() && !hasCoopAccess) {
            player.sendMessage(miniMessage.deserialize("<red>This island is locked!</red>"));
            return false;
        }

        // Get appropriate location
        Location targetLocation;
        if (isOwner) {
            targetLocation = island.getHomeLocation();
        } else {
            targetLocation = island.getVisitLocation();
        }

        // Try to find a safe location within range
        Location safeLocation = findSafeLocation(targetLocation);
        if (safeLocation == null) {
            if (!isOwner) {
                // For visitors, try home location as fallback
                safeLocation = findSafeLocation(island.getHomeLocation());
                if (safeLocation == null) {
                    // Final fallback to a safe location near the island center
                    Location islandCenter = island.getLocation();
                    // Try a few blocks above the island center
                    Location fallbackLocation = new Location(islandCenter.getWorld(), 
                        islandCenter.getX(), islandCenter.getY() + 5, islandCenter.getZ());
                    safeLocation = findSafeLocation(fallbackLocation);
                }
                
                if (safeLocation == null) {
                    player.sendMessage(miniMessage.deserialize(
                        "<red>Cannot find a safe teleport location! Island may be obstructed.</red>"));
                    return false;
                }
            } else {
                // For owners, always teleport even if location might be unsafe
                safeLocation = targetLocation;
            }
        }

        // Set gamemode based on player's status on the island
        Island.CoopRole role = island.getCoopRole(playerUUID);
        if (isOwner || role.getLevel() >= Island.CoopRole.MEMBER.getLevel()) {
            // Set to survival mode if player is owner or has MEMBER or higher role
            player.setGameMode(GameMode.SURVIVAL);
        } else if (island.isAdventureModeForVisitors()) {
            player.setGameMode(GameMode.ADVENTURE);
            player.sendMessage(miniMessage.deserialize(
                "<yellow>You are visiting in adventure mode. You cannot break blocks or access containers.</yellow>"));
        }

        player.teleport(safeLocation);
        
        // Update last online time for the island owner
        if (isOwner) {
            island.updateLastOnlineTime();
        }
        
        return true;
    }

    private Location findSafeLocation(Location target) {
        if (target == null) return null;
        
        World world = target.getWorld();
        if (world == null) return null;
        
        int baseX = target.getBlockX();
        int baseY = target.getBlockY();
        int baseZ = target.getBlockZ();
        
        // Check original location first
        if (isSafeLocation(world, baseX, baseY, baseZ)) {
            return new Location(world, baseX + 0.5, baseY, baseZ + 0.5);
        }
        
        // Check up to 10 blocks above
        for (int y = baseY + 1; y <= baseY + 10; y++) {
            if (isSafeLocation(world, baseX, y, baseZ)) {
                return new Location(world, baseX + 0.5, y, baseZ + 0.5);
            }
        }
        
        // Check up to 20 blocks below
        for (int y = baseY - 1; y >= baseY - 20; y--) {
            if (isSafeLocation(world, baseX, y, baseZ)) {
                return new Location(world, baseX + 0.5, y, baseZ + 0.5);
            }
        }
        
        return null; // No safe location found
    }

    private boolean isSafeLocation(World world, int x, int y, int z) {
        if (y < 0 || y > world.getMaxHeight() - 2) return false;
        
        // Check if feet and head locations are safe (air or passable)
        if (!world.getBlockAt(x, y, z).isPassable() || 
            !world.getBlockAt(x, y + 1, z).isPassable()) {
            return false;
        }
        
        // Check if there's solid ground below (within 3 blocks)
        for (int belowY = y - 1; belowY >= y - 3; belowY--) {
            if (belowY < 0) break;
            if (world.getBlockAt(x, belowY, z).isSolid()) {
                return true;
            }
        }
        
        return false;
    }

    public boolean teleportToIsland(Player player, UUID targetOwnerUUID) {
        Island targetIsland = getIsland(targetOwnerUUID);
        if (targetIsland == null) {
            return false;
        }
        
        return teleportToIsland(player, targetIsland);
    }
    
    public void saveIsland(Island island) {
        // Update the reference in our map
        playerIslands.put(island.getOwnerUUID(), island);
        
        // Save to persistent storage
        if (dataManager != null) {
            dataManager.saveIsland(island);
        }
    }

    public boolean deleteIsland(UUID playerUUID) {
        Island island = playerIslands.remove(playerUUID);
        if (island == null) {
            return false;
        }

        // Delete from persistent storage
        if (dataManager != null) {
            dataManager.deleteIsland(playerUUID);
        }

        // Delete the island's world
        boolean worldDeleted = plugin.getWorldManager().deleteIslandWorld(island.getIslandId());
        
        // Delete the island's settings
        plugin.getIslandSettingsManager().deleteIslandSettings(island.getIslandId());
        
        plugin.getLogger().info("Deleted island " + island.getIslandId() + " for player " + playerUUID);
        return worldDeleted;
    }

    public boolean deleteIsland(Player player) {
        UUID playerUUID = player.getUniqueId();
        Island island = getIsland(playerUUID);
        
        if (island == null) {
            return false;
        }

        // Delete the island
        boolean deleted = deleteIsland(playerUUID);
        
        if (deleted) {
            // Teleport player to hub
            teleportToHub(player);
            plugin.sendMessage(player, "island-deleted");
        }
        
        return deleted;
    }

    private void teleportToHub(Player player) {
        if (!plugin.getConfig().getBoolean("hub.enabled", true)) {
            World mainWorld = plugin.getWorldManager().getSkyBlockWorld();
            if (mainWorld != null) {
                player.teleport(mainWorld.getSpawnLocation());
            }
            return;
        }

        String hubWorldName = plugin.getConfig().getString("hub.world", "world");
        World hubWorld = org.bukkit.Bukkit.getWorld(hubWorldName);

        if (hubWorld == null) {
            World mainWorld = plugin.getWorldManager().getSkyBlockWorld();
            if (mainWorld != null) {
                player.teleport(mainWorld.getSpawnLocation());
            }
            return;
        }

        double x = plugin.getConfig().getDouble("hub.spawn.x", 0);
        double y = plugin.getConfig().getDouble("hub.spawn.y", 100);
        double z = plugin.getConfig().getDouble("hub.spawn.z", 0);

        Location hubLocation = new Location(hubWorld, x, y, z);
        player.teleport(hubLocation);
    }

    private boolean isValidIslandType(String type) {
        // Check if the type exists in our schematic manager
        String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
        for (String availableType : availableTypes) {
            if (availableType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    public Map<UUID, Island> getPlayerIslands() {
        return new HashMap<>(playerIslands);
    }
}
