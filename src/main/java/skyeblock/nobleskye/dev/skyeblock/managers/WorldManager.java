package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WorldManager {
    private final SkyeBlockPlugin plugin;
    private World skyBlockWorld;
    private Plugin slimePlugin;
    private Object slimeLoader;
    private final Map<String, Object> islandWorlds;
    private boolean slimeWorldEnabled = false;

    public WorldManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.islandWorlds = new HashMap<>();
    }

    public void initializeWorld() {
        // Check for Advanced Slime World Manager (ASWM)
        checkForASWM();
        
        // Initialize main world
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
                skyBlockWorld.setSpawnFlags(false, false);
                skyBlockWorld.setTime(6000);
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

    private void checkForASWM() {
        try {
            // Check for Advanced Slime World Manager
            slimePlugin = Bukkit.getPluginManager().getPlugin("AdvancedSlimeWorldManager");
            if (slimePlugin == null) {
                // Fallback to old SlimeWorldManager
                slimePlugin = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
            }
            
            if (slimePlugin != null) {
                // Try to get the loader using reflection
                Class<?> slimePluginClass = slimePlugin.getClass();
                try {
                    // Try ASWM API first
                    Object loader = slimePluginClass.getMethod("getLoader", String.class).invoke(slimePlugin, "file");
                    if (loader != null) {
                        slimeLoader = loader;
                        slimeWorldEnabled = true;
                        plugin.getLogger().info("Advanced Slime World Manager integration initialized successfully!");
                        return;
                    }
                } catch (Exception e) {
                    // Try old SWM API
                    try {
                        Object loader = slimePluginClass.getMethod("getLoader", String.class).invoke(slimePlugin, "file");
                        if (loader != null) {
                            slimeLoader = loader;
                            slimeWorldEnabled = true;
                            plugin.getLogger().info("SlimeWorldManager integration initialized successfully!");
                            return;
                        }
                    } catch (Exception ex) {
                        plugin.getLogger().info("SlimeWorldManager found but couldn't initialize loader: " + ex.getMessage());
                    }
                }
            } else {
                plugin.getLogger().info("No SlimeWorldManager found. Using standard world creation.");
            }
        } catch (Exception e) {
            plugin.getLogger().info("SlimeWorldManager not available: " + e.getMessage());
        }
        slimeWorldEnabled = false;
    }

    public World getSkyBlockWorld() {
        return skyBlockWorld;
    }

    public World createIslandWorld(String islandId) {
        // If SlimeWorldManager/ASWM is available, try to use it
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
            Class<?> slimePluginClass = slimePlugin.getClass();
            
            // Check if this is ASWM or old SWM
            boolean isASWM = slimePlugin.getName().equals("AdvancedSlimeWorldManager");
            
            if (isASWM) {
                return createASWMWorld(islandId, slimePluginClass);
            } else {
                return createSWMWorld(islandId, slimePluginClass);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to create SlimeWorld " + islandId + ": " + e.getMessage());
        }
        
        return null;
    }

    private World createASWMWorld(String islandId, Class<?> slimePluginClass) throws Exception {
        // ASWM API implementation - create world with islands/ directory structure
        String worldName = "islands_" + islandId; // Use underscore for ASWM compatibility
        
        // Create properties using reflection for ASWM
        Class<?> slimePropertyMapClass = Class.forName("com.infernalsuite.aswm.api.world.properties.SlimePropertyMap");
        Object properties = slimePropertyMapClass.getConstructor().newInstance();
        
        Class<?> slimePropertiesClass = Class.forName("com.infernalsuite.aswm.api.world.properties.SlimeProperties");
        
        // Set properties for ASWM
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

        // Create SlimeWorld for ASWM using the structured world name
        Object slimeWorld = slimePluginClass.getMethod("createEmptyWorld", Object.class, String.class, boolean.class, Object.class)
                .invoke(slimePlugin, slimeLoader, worldName, false, properties);
        
        // Generate the world in Bukkit
        slimePluginClass.getMethod("generateWorld", Object.class).invoke(slimePlugin, slimeWorld);
        
        // Store reference using original islandId for tracking
        islandWorlds.put(islandId, slimeWorld);
        
        World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld != null) {
            bukkitWorld.setTime(6000);
            bukkitWorld.setStorm(false);
            bukkitWorld.setThundering(false);
            bukkitWorld.setWeatherDuration(Integer.MAX_VALUE);
            
            plugin.getLogger().info("Created ASWM island: " + islandId);
            return bukkitWorld;
        }
        
        return null;
    }

    private World createSWMWorld(String islandId, Class<?> slimePluginClass) throws Exception {
        // Old SWM API implementation - create world with islands/ directory structure
        String worldName = "islands_" + islandId; // Use underscore for SWM compatibility
        
        // Create properties using reflection for old SWM
        Class<?> slimePropertyMapClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimePropertyMap");
        Object properties = slimePropertyMapClass.getConstructor().newInstance();
        
        Class<?> slimePropertiesClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimeProperties");
        
        // Set properties for old SWM
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

        // Create SlimeWorld for old SWM using the structured world name
        Object slimeWorld = slimePluginClass.getMethod("createEmptyWorld", Object.class, String.class, boolean.class, Object.class)
                .invoke(slimePlugin, slimeLoader, worldName, false, properties);
        
        // Generate the world in Bukkit
        slimePluginClass.getMethod("generateWorld", Object.class).invoke(slimePlugin, slimeWorld);
        
        // Store reference using original islandId for tracking
        islandWorlds.put(islandId, slimeWorld);
        
        World bukkitWorld = Bukkit.getWorld(worldName);
        if (bukkitWorld != null) {
            bukkitWorld.setTime(6000);
            bukkitWorld.setStorm(false);
            bukkitWorld.setThundering(false);
            bukkitWorld.setWeatherDuration(Integer.MAX_VALUE);
            
            plugin.getLogger().info("Created SWM island: " + worldName + " for island " + islandId);
            return bukkitWorld;
        }
        
        return null;
    }

    private World createStandardWorld(String islandId) {
        try {
            // Create world in islands/ directory
            String worldPath = "islands/" + islandId;
            WorldCreator creator = new WorldCreator(worldPath);
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
                
                plugin.getLogger().info("Created standard island world: " + worldPath);
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

        // Check for both standard world names and slime world names
        String standardWorldPath = "islands/" + islandId;
        String slimeWorldName = "islands_" + islandId;
        
        // Try to find and unload the world (could be either name format)
        World bukkitWorld = Bukkit.getWorld(islandId);
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(standardWorldPath);
        }
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(slimeWorldName);
        }
        
        if (bukkitWorld != null) {
            // Teleport any players out of the world first
            bukkitWorld.getPlayers().forEach(player -> {
                teleportToHub(player);
            });

            // Unload the world
            if (!Bukkit.unloadWorld(bukkitWorld, false)) {
                plugin.getLogger().warning("Failed to unload world: " + bukkitWorld.getName());
            }
        }

        // Try SlimeWorld deletion if available
        if (slimeWorldEnabled && slimePlugin != null && slimeLoader != null) {
            try {
                // Use reflection to check if world exists and delete it
                Class<?> slimeLoaderClass = slimeLoader.getClass();
                
                // Try both naming conventions for slime worlds
                boolean exists = (Boolean) slimeLoaderClass.getMethod("worldExists", String.class).invoke(slimeLoader, slimeWorldName);
                if (!exists) {
                    exists = (Boolean) slimeLoaderClass.getMethod("worldExists", String.class).invoke(slimeLoader, islandId);
                }
                
                if (exists) {
                    String worldToDelete = exists ? slimeWorldName : islandId;
                    slimeLoaderClass.getMethod("deleteWorld", String.class).invoke(slimeLoader, worldToDelete);
                    plugin.getLogger().info("Deleted SlimeWorld: " + worldToDelete);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete SlimeWorld " + islandId + ": " + e.getMessage());
            }
        } else {
            // Delete standard world folder (check islands directory)
            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), standardWorldPath);
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                    plugin.getLogger().info("Deleted world folder: " + standardWorldPath);
                } else {
                    // Fallback to old naming convention
                    worldFolder = new File(Bukkit.getWorldContainer(), islandId);
                    if (worldFolder.exists()) {
                        deleteDirectory(worldFolder);
                        plugin.getLogger().info("Deleted world folder: " + islandId);
                    }
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
        // Try multiple world name formats
        World world = Bukkit.getWorld(islandId);
        if (world == null) {
            world = Bukkit.getWorld("islands/" + islandId);
        }
        if (world == null) {
            world = Bukkit.getWorld("islands_" + islandId);
        }
        
        // Apply island settings to the world if found and it's not the main world
        if (world != null && !world.equals(skyBlockWorld)) {
            plugin.getIslandSettingsManager().applySettingsToWorld(islandId, world);
        }
        
        return world != null ? world : skyBlockWorld;
    }

    public boolean isSlimeWorldEnabled() {
        return slimeWorldEnabled;
    }

    public String getSlimeWorldManagerType() {
        if (!slimeWorldEnabled || slimePlugin == null) {
            return "None";
        }
        return slimePlugin.getName().equals("AdvancedSlimeWorldManager") ? "ASWM" : "SWM";
    }

    public void teleportToHub(org.bukkit.entity.Player player) {
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
        plugin.sendMessage(player, "teleported-to-hub");
    }
}
