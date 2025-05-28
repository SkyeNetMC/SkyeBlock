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
    private int nextIslandX;
    private int nextIslandZ;

    public IslandManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.playerIslands = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
        this.nextIslandX = plugin.getConfig().getInt("island.start-x", 0);
        this.nextIslandZ = plugin.getConfig().getInt("island.start-z", 0);
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
        
        // Paste the schematic using custom schematic manager
        boolean success = plugin.getCustomSchematicManager().pasteSchematic(islandType, islandLocation);
        if (!success) {
            // Clean up the world if schematic failed
            plugin.getWorldManager().deleteIslandWorld(islandId);
            return false;
        }

        // Store the island
        playerIslands.put(playerUUID, island);
        
        // Create default settings for the island and apply them to the world
        plugin.getIslandSettingsManager().createDefaultSettings(islandId);
        plugin.getIslandSettingsManager().applySettingsToWorld(islandId, islandWorld);
        
        plugin.getLogger().info("Created island " + islandId + " for player " + player.getName());
        return true;
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
                    // Final fallback to original spawn
                    safeLocation = findSafeLocation(plugin.getCustomSchematicManager().getSpawnLocation(
                        island.getIslandType(), island.getLocation()));
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
        // Islands are automatically saved in memory
        // In a real implementation, you might want to save to database or file
        // For now, we just update the reference in our map
        playerIslands.put(island.getOwnerUUID(), island);
    }

    public boolean deleteIsland(UUID playerUUID) {
        Island island = playerIslands.remove(playerUUID);
        if (island == null) {
            return false;
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
        // Check if the type exists in our custom schematic manager
        String[] availableTypes = plugin.getCustomSchematicManager().getAvailableSchematics();
        for (String availableType : availableTypes) {
            if (availableType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    private Location getNextIslandLocation(World world) {
        return new Location(world, nextIslandX, 100, nextIslandZ);
    }

    private void moveToNextIslandPosition() {
        int distance = plugin.getConfig().getInt("island.distance", 200);
        
        // Simple grid pattern: move east, then when we've gone far enough, move south and reset x
        nextIslandX += distance;
        
        // Move to next row after every 10 islands
        if (nextIslandX >= distance * 10) {
            nextIslandX = plugin.getConfig().getInt("island.start-x", 0);
            nextIslandZ += distance;
        }
    }

    public Map<UUID, Island> getPlayerIslands() {
        return new HashMap<>(playerIslands);
    }
}
