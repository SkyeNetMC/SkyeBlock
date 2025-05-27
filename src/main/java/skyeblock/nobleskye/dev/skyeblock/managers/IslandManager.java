package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandManager {
    private final SkyeBlockPlugin plugin;
    private final Map<UUID, Island> playerIslands;
    private int nextIslandX;
    private int nextIslandZ;

    public IslandManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.playerIslands = new HashMap<>();
        this.nextIslandX = plugin.getConfig().getInt("island.start-x", 0);
        this.nextIslandZ = plugin.getConfig().getInt("island.start-z", 0);
    }

    public boolean hasIsland(UUID playerUUID) {
        return playerIslands.containsKey(playerUUID);
    }

    public Island getIsland(UUID playerUUID) {
        return playerIslands.get(playerUUID);
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
        
        plugin.getLogger().info("Created island " + islandId + " for player " + player.getName());
        return true;
    }

    public boolean teleportToIsland(Player player) {
        UUID playerUUID = player.getUniqueId();
        Island island = getIsland(playerUUID);
        
        if (island == null) {
            return false;
        }

        // Get the island's world
        World islandWorld = plugin.getWorldManager().getIslandWorld(island.getIslandId());
        if (islandWorld == null) {
            return false;
        }

        // Use custom schematic manager to get proper spawn location
        Location spawnLocation = plugin.getCustomSchematicManager().getSpawnLocation(
            island.getIslandType(), island.getLocation());
        player.teleport(spawnLocation);
        return true;
    }

    public boolean deleteIsland(UUID playerUUID) {
        Island island = playerIslands.remove(playerUUID);
        if (island == null) {
            return false;
        }

        // Delete the island's world
        boolean worldDeleted = plugin.getWorldManager().deleteIslandWorld(island.getIslandId());
        
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
            player.sendMessage(plugin.getConfig().getString("messages.island-deleted", "&aYour island has been deleted! You have been teleported to the hub."));
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
