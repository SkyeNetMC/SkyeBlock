package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WorldManager {
    private final SkyeBlockPlugin plugin;
    private World skyBlockWorld;
    private Object slimePlugin;
    private Object slimeLoader;
    private final Map<String, Object> islandWorlds;
    private boolean slimeWorldEnabled = false;

    public WorldManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.islandWorlds = new HashMap<>();
    }

    public void initializeWorld() {
        // Check for SlimeWorldManager (optional)
        try {
            slimePlugin = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
            if (slimePlugin != null) {
                // Try to get the loader using reflection to avoid compile-time dependency
                Class<?> slimePluginClass = slimePlugin.getClass();
                Object loader = slimePluginClass.getMethod("getLoader", String.class).invoke(slimePlugin, "file");
                if (loader != null) {
                    slimeLoader = loader;
                    slimeWorldEnabled = true;
                    plugin.getLogger().info("SlimeWorldManager integration initialized successfully!");
                }
            } else {
                plugin.getLogger().info("SlimeWorldManager not found. Using standard world creation.");
            }
        } catch (Exception e) {
            plugin.getLogger().info("SlimeWorldManager not available or incompatible. Using standard world creation.");
            slimeWorldEnabled = false;
        }

        // Initialize main world (fallback for compatibility)
        String worldName = plugin.getConfig().getString("world.name", "skyblock_world");
        
        // Check if world already exists
        skyBlockWorld = Bukkit.getWorld(worldName);
        
        if (skyBlockWorld == null) {
            // Create new skyblock world
            WorldCreator creator = new WorldCreator(worldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            creator.generator(new VoidWorldGenerator());
            
            skyBlockWorld = creator.createWorld();
            
            if (skyBlockWorld != null) {
                // Set world properties
                skyBlockWorld.setSpawnFlags(false, false); // No monsters, no animals by default
                skyBlockWorld.setTime(6000); // Set to noon
                skyBlockWorld.setStorm(false);
                skyBlockWorld.setThundering(false);
                skyBlockWorld.setWeatherDuration(Integer.MAX_VALUE);
                
                plugin.getLogger().info("Created skyblock world: " + worldName);
            } else {
                plugin.getLogger().severe("Failed to create skyblock world!");
            }
        } else {
            plugin.getLogger().info("Loaded existing skyblock world: " + worldName);
        }
    }

    public World getSkyBlockWorld() {
        return skyBlockWorld;
    }

    public World createIslandWorld(String islandId) {
        // If SlimeWorldManager is available, try to use it
        if (slimeWorldEnabled && slimePlugin != null && slimeLoader != null) {
            try {
                return createSlimeWorld(islandId);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create SlimeWorld for " + islandId + ", falling back to standard world: " + e.getMessage());
            }
        }

        // Fallback to creating individual standard worlds
        return createStandardWorld(islandId);
    }

    private World createSlimeWorld(String islandId) {
        try {
            // Use reflection to avoid compile-time dependency
            Class<?> slimePluginClass = slimePlugin.getClass();
            
            // Create properties using reflection
            Class<?> slimePropertyMapClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimePropertyMap");
            Object properties = slimePropertyMapClass.getConstructor().newInstance();
            
            Class<?> slimePropertiesClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimeProperties");
            
            // Set properties using reflection
            Object spawnX = slimePropertiesClass.getField("SPAWN_X").get(null);
            Object spawnY = slimePropertiesClass.getField("SPAWN_Y").get(null);
            Object spawnZ = slimePropertiesClass.getField("SPAWN_Z").get(null);
            Object difficulty = slimePropertiesClass.getField("DIFFICULTY").get(null);
            Object allowMonsters = slimePropertiesClass.getField("ALLOW_MONSTERS").get(null);
            Object allowAnimals = slimePropertiesClass.getField("ALLOW_ANIMALS").get(null);
            Object pvp = slimePropertiesClass.getField("PVP").get(null);
            
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, spawnX, 0);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, spawnY, 100);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, spawnZ, 0);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, difficulty, "peaceful");
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, allowMonsters, false);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, allowAnimals, false);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, pvp, false);

            // Create SlimeWorld
            Object slimeWorld = slimePluginClass.getMethod("createEmptyWorld", Object.class, String.class, boolean.class, Object.class)
                    .invoke(slimePlugin, slimeLoader, islandId, false, properties);
            
            // Generate the world in Bukkit
            slimePluginClass.getMethod("generateWorld", Object.class).invoke(slimePlugin, slimeWorld);
            
            // Store reference
            islandWorlds.put(islandId, slimeWorld);
            
            World bukkitWorld = Bukkit.getWorld(islandId);
            if (bukkitWorld != null) {
                bukkitWorld.setTime(6000); // Set to noon
                bukkitWorld.setStorm(false);
                bukkitWorld.setThundering(false);
                bukkitWorld.setWeatherDuration(Integer.MAX_VALUE);
                
                plugin.getLogger().info("Created SlimeWorld island: " + islandId);
                return bukkitWorld;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create SlimeWorld " + islandId + ": " + e.getMessage());
        }
        
        return null;
    }

    private World createStandardWorld(String islandId) {
        try {
            WorldCreator creator = new WorldCreator(islandId);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            creator.generator(new VoidWorldGenerator());
            
            World world = creator.createWorld();
            if (world != null) {
                world.setSpawnFlags(false, false);
                world.setTime(6000);
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                
                plugin.getLogger().info("Created standard island world: " + islandId);
                return world;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create standard world " + islandId + ": " + e.getMessage());
        }
        
        // Ultimate fallback to main world
        plugin.getLogger().warning("Falling back to main world for island: " + islandId);
        return skyBlockWorld;
    }

    public boolean deleteIslandWorld(String islandId) {
        // If it's the main world, don't delete it
        if (islandId.equals(skyBlockWorld.getName())) {
            plugin.getLogger().info("Cannot delete main world, skipping deletion for: " + islandId);
            return true;
        }

        World bukkitWorld = Bukkit.getWorld(islandId);
        if (bukkitWorld != null) {
            // Teleport any players out of the world first
            bukkitWorld.getPlayers().forEach(player -> {
                teleportToHub(player);
            });

            // Unload the world
            if (!Bukkit.unloadWorld(bukkitWorld, false)) {
                plugin.getLogger().warning("Failed to unload world: " + islandId);
            }
        }

        // Try SlimeWorld deletion if available
        if (slimeWorldEnabled && slimePlugin != null && slimeLoader != null) {
            try {
                // Use reflection to check if world exists and delete it
                Class<?> slimeLoaderClass = slimeLoader.getClass();
                boolean exists = (Boolean) slimeLoaderClass.getMethod("worldExists", String.class).invoke(slimeLoader, islandId);
                if (exists) {
                    slimeLoaderClass.getMethod("deleteWorld", String.class).invoke(slimeLoader, islandId);
                    plugin.getLogger().info("Deleted SlimeWorld: " + islandId);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete SlimeWorld " + islandId + ": " + e.getMessage());
            }
        } else {
            // Delete standard world folder
            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), islandId);
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                    plugin.getLogger().info("Deleted world folder: " + islandId);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete world folder " + islandId + ": " + e.getMessage());
            }
        }

        // Remove from our tracking
        islandWorlds.remove(islandId);

        return true;
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    public World getIslandWorld(String islandId) {
        World world = Bukkit.getWorld(islandId);
        return world != null ? world : skyBlockWorld;
    }

    private void teleportToHub(org.bukkit.entity.Player player) {
        if (!plugin.getConfig().getBoolean("hub.enabled", true)) {
            player.teleport(skyBlockWorld.getSpawnLocation());
            return;
        }

        String hubWorldName = plugin.getConfig().getString("hub.world", "world");
        World hubWorld = Bukkit.getWorld(hubWorldName);

        if (hubWorld == null) {
            player.teleport(skyBlockWorld.getSpawnLocation());
            return;
        }

        double x = plugin.getConfig().getDouble("hub.spawn.x", 0);
        double y = plugin.getConfig().getDouble("hub.spawn.y", 100);
        double z = plugin.getConfig().getDouble("hub.spawn.z", 0);

        org.bukkit.Location hubLocation = new org.bukkit.Location(hubWorld, x, y, z);
        player.teleport(hubLocation);
        player.sendMessage(plugin.getConfig().getString("messages.teleported-to-hub", "&aYou have been teleported to the hub!"));
    }
}
