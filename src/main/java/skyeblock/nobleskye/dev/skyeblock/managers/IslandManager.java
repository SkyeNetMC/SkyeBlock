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
    
    // Cooldown tracking
    private final Map<UUID, Long> deletionTimestamps; // When player last deleted island
    private final Map<UUID, Integer> deletionCounts; // How many times player has deleted islands

    public IslandManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.playerIslands = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
        this.deletionTimestamps = new HashMap<>();
        this.deletionCounts = new HashMap<>();
    }
    
    /**
     * Initialize the data manager and load existing islands
     */
    public void initialize() {
        plugin.getLogger().info("Initializing Island Manager...");
        this.dataManager = new IslandDataManager(plugin);
        loadAllIslands();
        plugin.getLogger().info("Island Manager initialization complete. Players can now be teleported to their islands immediately.");
    }
    
    /**
     * Load all islands from persistent storage
     */
    private void loadAllIslands() {
        plugin.getLogger().info("Loading all island data from storage...");
        long startTime = System.currentTimeMillis();
        
        Map<UUID, Island> loadedIslands = dataManager.loadAllIslands();
        playerIslands.putAll(loadedIslands);
        
        long loadTime = System.currentTimeMillis() - startTime;
        plugin.getLogger().info("Successfully loaded " + loadedIslands.size() + " islands in " + loadTime + "ms");
        
        // Log some additional details for debugging
        if (loadedIslands.size() > 0) {
            plugin.getLogger().info("Island data preloaded - /is tp will work immediately after server restart");
            
            // Preload a few island worlds to test the system
            preloadSampleIslandWorlds(loadedIslands);
        } else {
            plugin.getLogger().info("No existing islands found to preload");
        }
    }
    
    /**
     * Preload a few island worlds to ensure the world loading system is working
     */
    private void preloadSampleIslandWorlds(Map<UUID, Island> islands) {
        plugin.getLogger().info("Testing island world loading system...");
        int tested = 0;
        int maxToTest = Math.min(3, islands.size()); // Test up to 3 islands
        
        for (Island island : islands.values()) {
            if (tested >= maxToTest) break;
            
            try {
                String islandId = island.getIslandId();
                plugin.getLogger().info("Testing world loading for island: " + islandId);
                
                World world = plugin.getWorldManager().getOrLoadIslandWorld(islandId);
                if (world != null && !world.equals(plugin.getWorldManager().getSkyBlockWorld())) {
                    plugin.getLogger().info("✓ Successfully preloaded world: " + world.getName());
                } else {
                    plugin.getLogger().warning("✗ Failed to preload world for island: " + islandId);
                }
                tested++;
            } catch (Exception e) {
                plugin.getLogger().warning("Error testing island world loading: " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Island world loading test complete (" + tested + "/" + maxToTest + " tested)");
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
    
    public Island getIslandById(String islandId) {
        for (Island island : playerIslands.values()) {
            if (island.getIslandId().equals(islandId)) {
                return island;
            }
        }
        return null;
    }
    
    public List<Island> getAllIslands() {
        return new ArrayList<>(playerIslands.values());
    }

    public boolean createIsland(Player player, String islandType) {
        UUID playerUUID = player.getUniqueId();
        plugin.getLogger().info("Starting island creation for player " + player.getName() + " with type: " + islandType);
        
        // Check if player already has an island
        if (hasIsland(playerUUID)) {
            plugin.getLogger().warning("ISLAND CREATION FAILED: Player " + player.getName() + " already has an island");
            return false;
        }
        
        // Check cooldown and try limits
        if (!canCreateIsland(player)) {
            return false;
        }

        // Validate island type
        if (!isValidIslandType(islandType)) {
            plugin.getLogger().severe("ISLAND CREATION FAILED: Invalid island type '" + islandType + "' for player " + player.getName());
            String[] validTypes = plugin.getSchematicManager().getAvailableSchematics();
            plugin.getLogger().severe("  Available types: " + String.join(", ", validTypes));
            return false;
        }

        // Create island ID
        String islandId = "island-" + islandType + "-" + playerUUID.toString();
        plugin.getLogger().info("Creating island with ID: " + islandId);
        
        // Create individual world for this island
        World islandWorld = plugin.getWorldManager().createIslandWorld(islandId);
        if (islandWorld == null) {
            plugin.getLogger().severe("ISLAND CREATION FAILED: Could not create world for island " + islandId);
            return false;
        }
        plugin.getLogger().info("Island world created successfully: " + islandWorld.getName());

        // Calculate location for new island (center of the world)
        Location islandLocation = new Location(islandWorld, 0, 100, 0);
        plugin.getLogger().info("Island location set to: " + islandLocation);
        
        // Create the island object
        Island island = new Island(playerUUID, islandType, islandLocation);
        
        // Paste the schematic using WorldEdit schematic manager with template mapping
        plugin.getLogger().info("Attempting to paste island template...");
        boolean success = plugin.getSchematicManager().pasteIslandTemplate(islandType, islandLocation);
        if (!success) {
            plugin.getLogger().severe("ISLAND CREATION FAILED: Could not paste schematic for island type '" + islandType + "'");
            plugin.getLogger().severe("  Island ID: " + islandId);
            plugin.getLogger().severe("  Player: " + player.getName());
            plugin.getLogger().severe("  Location: " + islandLocation);
            
            // Clean up the world if schematic failed
            plugin.getLogger().info("Cleaning up failed island world: " + islandId);
            plugin.getWorldManager().deleteIslandWorld(islandId);
            return false;
        }
        plugin.getLogger().info("Island schematic pasted successfully");

        // Store the island
        playerIslands.put(playerUUID, island);
        plugin.getLogger().info("Island stored in memory");
        
        // Save island to persistent storage
        if (dataManager != null) {
            dataManager.saveIsland(island);
            plugin.getLogger().info("Island saved to persistent storage");
        } else {
            plugin.getLogger().warning("Data manager is null - island not saved to persistent storage");
        }
        
        // Create default settings for the island and apply them to the world
        plugin.getIslandSettingsManager().createDefaultSettings(islandId);
        plugin.getIslandSettingsManager().applySettingsToWorld(islandId, islandWorld);
        plugin.getLogger().info("Island settings applied");
        
        // Create a corresponding nether island if this isn't already a nether island
        if (!islandType.equals("nether")) {
            plugin.getLogger().info("Creating corresponding nether island...");
            createNetherIsland(playerUUID, island);
        }
        
        plugin.getLogger().info("Successfully created island " + islandId + " for player " + player.getName());
        return true;
    }

    /**
     * Create a nether island for a player who already has a main island
     */
    private void createNetherIsland(UUID playerUUID, Island mainIsland) {
        try {
            // Check if auto-create-nether is enabled
            if (!plugin.getConfig().getBoolean("world.auto-create-nether", true)) {
                plugin.getLogger().info("Auto-create-nether disabled, skipping nether island creation for " + playerUUID);
                return;
            }
            
            // Create nether island ID with _nether suffix pattern
            String mainIslandId = mainIsland.getIslandId();
            String netherIslandId = mainIslandId + "_nether";
            
            // Create individual nether world for this island
            World netherIslandWorld = plugin.getWorldManager().createIslandWorld(netherIslandId);
            if (netherIslandWorld == null) {
                plugin.getLogger().warning("Failed to create nether world for island: " + netherIslandId);
                return;
            }
            
            // Set nether location in the center of the new nether world
            Location netherIslandLocation = new Location(netherIslandWorld, 0, 100, 0);
            
            // Create the nether island object
            Island netherIsland = new Island(playerUUID, "nether_portal_island", netherIslandLocation);
            
            // Get the nether template from config (should be "nether_portal_island")
            String netherTemplate = plugin.getConfig().getString("nether.default-template", "nether_portal_island");
            
            // Paste the nether portal island schematic using template mapping
            boolean success = plugin.getSchematicManager().pasteIslandTemplate(netherTemplate, netherIslandLocation);
            if (!success) {
                plugin.getLogger().warning("Failed to paste nether portal island schematic for island: " + netherIslandId);
                // Clean up the world if schematic failed
                plugin.getWorldManager().deleteIslandWorld(netherIslandId);
                return;
            }

            // Save nether island to persistent storage
            if (dataManager != null) {
                dataManager.saveIsland(netherIsland);
            }
            
            // Create default settings for the nether island and apply them
            plugin.getIslandSettingsManager().createDefaultSettings(netherIslandId);
            plugin.getIslandSettingsManager().applySettingsToWorld(netherIslandId, netherIslandWorld);
            
            // Set the nether area biome to NETHER_WASTES
            setNetherBiome(netherIslandLocation, 32);
            
            plugin.getLogger().info("Created nether world " + netherIslandId + " with nether_portal_island template for player " + playerUUID);
            
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
        
        plugin.getLogger().info("Teleport request for player " + player.getName() + " (UUID: " + playerUUID + ")");
        
        if (island == null) {
            plugin.getLogger().warning("Player " + player.getName() + " has no island data in memory - attempting reload from disk");
            
            // Try to reload the specific island from disk (fallback for edge cases)
            if (dataManager != null) {
                Island reloadedIsland = dataManager.loadIsland(playerUUID);
                if (reloadedIsland != null) {
                    playerIslands.put(playerUUID, reloadedIsland);
                    island = reloadedIsland;
                    plugin.getLogger().info("Successfully reloaded island data for " + player.getName());
                } else {
                    plugin.getLogger().info("No island data found on disk for " + player.getName());
                }
            }
        } else {
            plugin.getLogger().info("Island data found in memory for " + player.getName() + " (preloaded at startup)");
        }
        
        if (island == null) {
            // If island can't be found, teleport to spawn/hub instead
            plugin.getLogger().info("Player " + player.getName() + " has no island - teleporting to spawn");
            teleportToSpawn(player);
            player.sendMessage(miniMessage.deserialize("<yellow>No island found! Teleporting to spawn.</yellow>"));
            return true;
        }

        plugin.getLogger().info("Found island for " + player.getName() + ": " + island.getIslandId());
        plugin.getLogger().info("Island home location: " + island.getHomeLocation());
        plugin.getLogger().info("Island world reference: " + (island.getHomeLocation().getWorld() != null ? island.getHomeLocation().getWorld().getName() : "null"));
        return teleportToIsland(player, island);
    }

    public boolean teleportToIsland(Player player, Island island) {
        // Get the island's world, loading it if necessary
        World islandWorld = plugin.getWorldManager().getOrLoadIslandWorld(island.getIslandId());
        if (islandWorld == null) {
            plugin.getLogger().warning("Could not find or load world for island " + island.getIslandId());
            player.sendMessage(miniMessage.deserialize("<red>Could not load your island world! Please contact an administrator.</red>"));
            return false;
        }
        
        plugin.getLogger().info("Using world " + islandWorld.getName() + " for island " + island.getIslandId());

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
        
        // If target location is null or has invalid world, create a safe location
        if (targetLocation == null || targetLocation.getWorld() == null) {
            plugin.getLogger().warning("Target location is null or has invalid world for island " + island.getIslandId() + ", creating fallback location");
            
            // Create a safe location in the correct world
            Location islandCenter = island.getLocation();
            if (islandCenter != null && islandCenter.getWorld() == null) {
                // If the island's main location also has a null world, update it with the loaded world
                islandCenter = new Location(islandWorld, islandCenter.getX(), islandCenter.getY(), islandCenter.getZ(), 
                                          islandCenter.getYaw(), islandCenter.getPitch());
            }
            
            if (islandCenter != null) {
                targetLocation = new Location(islandWorld, islandCenter.getX(), islandCenter.getY() + 1, islandCenter.getZ());
            } else {
                // Final fallback - center of the world at a safe height
                targetLocation = new Location(islandWorld, 0, 100, 0);
            }
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

        // Track deletion for cooldown system
        deletionTimestamps.put(playerUUID, System.currentTimeMillis());
        deletionCounts.put(playerUUID, deletionCounts.getOrDefault(playerUUID, 0) + 1);
        
        int currentTries = deletionCounts.get(playerUUID);
        int maxTries = getMaxDeletionTries();
        plugin.getLogger().info("Player " + playerUUID + " deleted island - deletion count: " + currentTries + "/" + maxTries);

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
            // Clear player inventory when island is deleted
            clearPlayerInventory(player);
            
            // Teleport player to hub
            teleportToSpawn(player);
            plugin.sendMessage(player, "island-deleted");
            
            // Show deletion count and cooldown information
            int currentTries = getDeletionTries(playerUUID);
            int maxTries = getMaxDeletionTries();
            int remainingTries = maxTries - currentTries;
            
            if (currentTries == 1) {
                // Player has made 1 deletion, warn about upcoming restriction
                String warningMessage = plugin.getConfig().getString("messages.deletion-warning", 
                    "<yellow>Warning: You have {remaining} deletion(s) remaining before cooldown restriction applies.</yellow>");
                warningMessage = warningMessage.replace("{remaining}", String.valueOf(remainingTries))
                                               .replace("{max}", String.valueOf(maxTries));
                plugin.sendMessage(player, warningMessage);
            } else if (currentTries >= 2) {
                // Player has used 2+ deletions - now they get cooldown for deletion
                long cooldownSeconds = plugin.getConfig().getLong("island.create-island.delay", 3600);
                String cooldownMessage = plugin.getConfig().getString("messages.deletion-cooldown-info", 
                    "<yellow>You have made {current} deletions. You must wait {time} before being able to delete your next island.</yellow>");
                cooldownMessage = cooldownMessage.replace("{current}", String.valueOf(currentTries))
                                                .replace("{time}", formatTime(cooldownSeconds));
                plugin.sendMessage(player, cooldownMessage);
            }
        }
        
        return deleted;
    }

    /**
     * Clear player's inventory completely when they delete their island
     */
    private void clearPlayerInventory(Player player) {
        // Clear main inventory
        player.getInventory().clear();
        
        // Clear armor slots
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        
        // Clear offhand
        player.getInventory().setItemInOffHand(null);
        
        // Clear ender chest
        player.getEnderChest().clear();
        
        // Reset experience
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);
        
        // Reset food and health
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
        
        // Send confirmation message
        plugin.sendMessage(player, "<yellow>Your inventory has been cleared as part of island deletion.</yellow>");
        
        plugin.getLogger().info("Cleared inventory for player " + player.getName() + " after island deletion");
    }

    private void teleportToSpawn(Player player) {
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
        float yaw = (float) plugin.getConfig().getDouble("hub.spawn.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("hub.spawn.pitch", 0.0);

        Location hubLocation = new Location(hubWorld, x, y, z, yaw, pitch);
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

    /**
     * Check if a player can create an island (check cooldown and try limits)
     */
    public boolean canCreateIsland(Player player) {
        // Admin bypass - admins can always create islands
        if (player.hasPermission("skyeblock.admin")) {
            return true;
        }

        // Players can always create islands (no restriction on creation)
        // The restriction is on deletion after 2 deletions
        return true;
    }

    /**
     * Check if a player can delete their island (admin bypass or not on cooldown)
     */
    public boolean canDeleteIsland(Player player) {
        UUID playerUUID = player.getUniqueId();
        
        // Admin bypass - admins can always delete islands
        if (player.hasPermission("skyeblock.admin")) {
            return true;
        }
        
        // Check if player has already made 2 deletions
        int currentTries = deletionCounts.getOrDefault(playerUUID, 0);
        
        if (currentTries >= 2) {
            // Player has made 2 deletions, check cooldown before allowing 3rd island deletion
            if (deletionTimestamps.containsKey(playerUUID)) {
                long lastDeletion = deletionTimestamps.get(playerUUID);
                long cooldownSeconds = plugin.getConfig().getLong("island.create-island.delay", 3600);
                long timeSinceDeletion = (System.currentTimeMillis() - lastDeletion) / 1000;
                
                if (timeSinceDeletion < cooldownSeconds) {
                    // Still on cooldown - cannot delete 3rd island
                    long remainingTime = cooldownSeconds - timeSinceDeletion;
                    String message = plugin.getConfig().getString("messages.deletion-blocked-cooldown", 
                        "<red>You cannot delete your island while on cooldown. Time remaining: {time}</red>");
                    message = message.replace("{time}", formatTime(remainingTime));
                    plugin.sendMessage(player, message);
                    plugin.getLogger().info("Player " + player.getName() + " denied island deletion - still on cooldown (" + remainingTime + "s remaining)");
                    return false;
                } else {
                    // Cooldown expired, reset deletion count to allow new cycle
                    deletionCounts.put(playerUUID, 0);
                    deletionTimestamps.remove(playerUUID);
                    plugin.getLogger().info("Player " + player.getName() + " cooldown expired - reset deletion count");
                }
            }
        }
        
        return true;
    }
    
    /**
     * Format time in seconds to a human-readable string with hours, minutes, and seconds
     */
    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        
        if (hours > 0) {
            return "<gold>" + hours + " hours, " + minutes + " minutes, and " + seconds + " seconds</gold>";
        } else if (minutes > 0) {
            return "<gold>" + minutes + " minutes and " + seconds + " seconds</gold>";
        } else {
            return "<gold>" + seconds + " seconds</gold>";
        }
    }

    /**
     * Get remaining cooldown time for a player in seconds
     */
    public long getRemainingCooldown(UUID playerUUID) {
        if (!deletionTimestamps.containsKey(playerUUID)) {
            return 0;
        }
        
        long lastDeletion = deletionTimestamps.get(playerUUID);
        long cooldownSeconds = plugin.getConfig().getLong("island.create-island.delay", 3600);
        long timeSinceDeletion = (System.currentTimeMillis() - lastDeletion) / 1000;
        
        return Math.max(0, cooldownSeconds - timeSinceDeletion);
    }
    
    /**
     * Get number of deletion tries used by a player
     */
    public int getDeletionTries(UUID playerUUID) {
        return deletionCounts.getOrDefault(playerUUID, 0);
    }
    
    /**
     * Get maximum allowed deletion tries from config
     */
    public int getMaxDeletionTries() {
        return plugin.getConfig().getInt("island.create-island.tries", 3);
    }

    public Map<UUID, Island> getPlayerIslands() {
        return new HashMap<>(playerIslands);
    }
}
