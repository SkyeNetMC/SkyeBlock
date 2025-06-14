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
    private World skyBlockNetherWorld;
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
        
        // Initialize nether world if enabled
        if (plugin.getConfig().getBoolean("nether.enabled", false)) {
            String netherWorldName = plugin.getConfig().getString("nether.name", "skyblock_nether");
            
            // Check if nether world already exists
            skyBlockNetherWorld = Bukkit.getWorld(netherWorldName);
            
            if (skyBlockNetherWorld == null) {
                // Create new skyblock nether world
                WorldCreator netherCreator = new WorldCreator(netherWorldName);
                netherCreator.type(WorldType.FLAT);
                netherCreator.environment(World.Environment.NETHER);
                netherCreator.generateStructures(false);
                netherCreator.generator(new VoidWorldGenerator());
                
                skyBlockNetherWorld = netherCreator.createWorld();
                
                if (skyBlockNetherWorld != null) {
                    // Set nether world properties
                    skyBlockNetherWorld.setSpawnFlags(false, false);
                    skyBlockNetherWorld.setTime(6000);
                    
                    plugin.getLogger().info("Created skyblock nether world: " + netherWorldName);
                } else {
                    plugin.getLogger().severe("Failed to create skyblock nether world!");
                }
            } else {
                plugin.getLogger().info("Loaded existing skyblock nether world: " + netherWorldName);
            }
        }
    }

    private void checkForASWM() {
        try {
            // Check for built-in ASWM (ASP servers)
            if (checkForBuiltInASWM()) {
                return;
            }
            
            plugin.getLogger().info("⚠ No built-in ASWM found. This plugin is designed for ASP servers with built-in ASWM.");
            plugin.getLogger().info("⚠ Islands will use standard Bukkit worlds.");
            plugin.getLogger().info("⚠ For better performance, use this plugin on an ASP server with built-in ASWM.");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Unexpected error during SlimeWorldManager detection: " + e.getMessage());
            e.printStackTrace();
        }
        slimeWorldEnabled = false;
    }
    
    private boolean checkForBuiltInASWM() {
        try {
            plugin.getLogger().info("Checking for built-in ASWM (ASP server)...");
            
            // Try to access ASWM API directly using the new package structure
            Class<?> slimePluginClass = Class.forName("com.infernalsuite.aswm.SlimePlugin");
            plugin.getLogger().info("Found built-in ASWM API class: " + slimePluginClass.getName());
            
            // Try to get the plugin instance
            Object aswmInstance = slimePluginClass.getMethod("getInstance").invoke(null);
            if (aswmInstance != null) {
                plugin.getLogger().info("Successfully got built-in ASWM instance");
                
                // Try to get the file loader using the new API
                Class<?> loaderManagerClass = Class.forName("com.infernalsuite.aswm.api.AdvancedSlimeWorldManagerAPI");
                Object apiInstance = loaderManagerClass.getMethod("instance").invoke(null);
                
                if (apiInstance != null) {
                    Object loader = loaderManagerClass.getMethod("getLoader", String.class).invoke(apiInstance, "file");
                    if (loader != null) {
                        slimePlugin = (Plugin) aswmInstance;
                        slimeLoader = loader;
                        slimeWorldEnabled = true;
                        plugin.getLogger().info("✓ Built-in Advanced Slime World Manager API integration initialized successfully!");
                        plugin.getLogger().info("✓ Built-in ASWM (ASP server) detected and configured - islands will use slime worlds");
                        return true;
                    } else {
                        plugin.getLogger().warning("Built-in ASWM API getLoader returned null");
                    }
                } else {
                    plugin.getLogger().warning("Built-in ASWM API instance returned null");
                }
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Built-in ASWM API not found (not an ASP server or old version)");
        } catch (Exception e) {
            plugin.getLogger().info("Failed to initialize built-in ASWM API: " + e.getMessage());
            plugin.getLogger().info("This is normal if you're not using an ASP server with built-in ASWM");
        }
        
        return false;
    }

    public World getSkyBlockWorld() {
        return skyBlockWorld;
    }
    
    public World getSkyBlockNetherWorld() {
        return skyBlockNetherWorld;
    }
    
    public boolean hasNetherWorld() {
        return skyBlockNetherWorld != null;
    }

    public World createIslandWorld(String islandId) {
        // Priority 1: Use ASWM/SlimeWorldManager if available and enabled
        if (slimeWorldEnabled && slimePlugin != null && slimeLoader != null) {
            plugin.getLogger().info("ASWM is available - attempting to create slime world for island: " + islandId);
            try {
                World slimeWorld = createSlimeWorld(islandId);
                if (slimeWorld != null) {
                    plugin.getLogger().info("✓ Successfully created ASWM world for island: " + islandId + " (World: " + slimeWorld.getName() + ")");
                    return slimeWorld;
                } else {
                    plugin.getLogger().warning("✗ ASWM world creation returned null for island: " + islandId + " - falling back to standard world");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("✗ Failed to create ASWM world for island " + islandId + ": " + e.getMessage());
                plugin.getLogger().warning("Exception details: " + e.getClass().getSimpleName());
                if (plugin.getConfig().getBoolean("debug", false)) {
                    e.printStackTrace();
                }
                plugin.getLogger().info("Falling back to standard Bukkit world creation...");
            }
        } else {
            // Log why ASWM isn't being used
            if (!slimeWorldEnabled) {
                plugin.getLogger().info("ASWM not enabled - creating standard world for island: " + islandId);
            } else if (slimePlugin == null) {
                plugin.getLogger().info("ASWM plugin not found - creating standard world for island: " + islandId);
            } else if (slimeLoader == null) {
                plugin.getLogger().info("ASWM loader not available - creating standard world for island: " + islandId);
            }
        }

        // Fallback: Create standard Bukkit world
        plugin.getLogger().info("Creating standard Bukkit world for island: " + islandId);
        World standardWorld = createStandardWorld(islandId);
        if (standardWorld != null) {
            plugin.getLogger().info("✓ Successfully created standard world for island: " + islandId + " (World: " + standardWorld.getName() + ")");
        } else {
            plugin.getLogger().severe("✗ Failed to create any world for island: " + islandId);
        }
        return standardWorld;
    }

    private World createSlimeWorld(String islandId) {
        try {
            Class<?> slimePluginClass = slimePlugin.getClass();
            
            // Check if this is ASWM (plugin or built-in) or old SWM
            boolean isASWM = slimePlugin.getName().equals("AdvancedSlimeWorldManager") || 
                            slimePluginClass.getName().contains("com.infernalsuite.aswm");
            
            if (isASWM) {
                plugin.getLogger().info("Using ASWM API to create world for island: " + islandId);
                World world = createASWMWorld(islandId, slimePluginClass);
                if (world != null) {
                    plugin.getLogger().info("✓ ASWM world creation successful for: " + islandId);
                    return world;
                } else {
                    plugin.getLogger().warning("✗ ASWM world creation returned null for: " + islandId);
                }
            } else {
                plugin.getLogger().info("Using legacy SWM API to create world for island: " + islandId);
                World world = createSWMWorld(islandId, slimePluginClass);
                if (world != null) {
                    plugin.getLogger().info("✓ Legacy SWM world creation successful for: " + islandId);
                    return world;
                } else {
                    plugin.getLogger().warning("✗ Legacy SWM world creation returned null for: " + islandId);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Exception in createSlimeWorld for " + islandId + ": " + e.getMessage());
            plugin.getLogger().warning("Exception type: " + e.getClass().getSimpleName());
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    private World createASWMWorld(String islandId, Class<?> slimePluginClass) throws Exception {
        // ASWM API implementation - determine world type from island ID
        boolean isNetherIsland = islandId.contains("nether");
        
        // Use structured naming for SlimeWorlds to reflect folder organization
        String worldName;
        if (isNetherIsland) {
            worldName = "skyeblock_nether_" + islandId;
        } else {
            worldName = "skyeblock_islands_" + islandId;
        }
        
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
            
            plugin.getLogger().info("✓ ASWM world created successfully: " + worldName + " for island " + islandId);
            return bukkitWorld;
        } else {
            plugin.getLogger().warning("✗ ASWM world generation failed - Bukkit world is null for: " + worldName);
        }
        
        return null;
    }

    private World createSWMWorld(String islandId, Class<?> slimePluginClass) throws Exception {
        // Old SWM API implementation - determine world type from island ID
        boolean isNetherIsland = islandId.contains("nether");
        
        // Use structured naming for SlimeWorlds to reflect folder organization
        String worldName;
        if (isNetherIsland) {
            worldName = "skyeblock_nether_" + islandId;
        } else {
            worldName = "skyeblock_islands_" + islandId;
        }
        
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
            
            plugin.getLogger().info("✓ Legacy SWM world created successfully: " + worldName + " for island " + islandId);
            return bukkitWorld;
        } else {
            plugin.getLogger().warning("✗ Legacy SWM world generation failed - Bukkit world is null for: " + worldName);
        }
        
        return null;
    }

    private World createStandardWorld(String islandId) {
        try {
            // Determine if this is a nether island based on id format
            boolean isNetherIsland = islandId.contains("nether");
            
            // Create world in the appropriate directory structure
            // root/skyeblock/islands/ for normal islands
            // root/skyeblock/nether/ for nether islands
            String worldPath;
            if (isNetherIsland) {
                worldPath = "skyeblock/nether/" + islandId;
            } else {
                worldPath = "skyeblock/islands/" + islandId;
            }
            
            WorldCreator creator = new WorldCreator(worldPath);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            
            // Set nether environment for nether islands
            if (isNetherIsland) {
                creator.environment(World.Environment.NETHER);
            }
            
            creator.generator(new VoidWorldGenerator());
            
            World world = creator.createWorld();
            if (world != null) {
                world.setSpawnFlags(false, false);
                world.setTime(6000);
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                
                plugin.getLogger().info("✓ Standard Bukkit world created successfully: " + worldPath);
                return world;
            } else {
                plugin.getLogger().warning("✗ Standard Bukkit world creation failed for: " + worldPath);
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

        // Determine if this is a nether island
        boolean isNetherIsland = islandId.contains("nether");
        
        // Check for standard world names, new directory structure, and slime world names
        String oldStandardWorldPath = "islands/" + islandId;
        String newStandardWorldPath = isNetherIsland ? 
            "skyeblock/nether/" + islandId : 
            "skyeblock/islands/" + islandId;
        String oldSlimeWorldName = "islands_" + islandId;
        String newSlimeWorldName = isNetherIsland ?
            "skyeblock_nether_" + islandId :
            "skyeblock_islands_" + islandId;
        
        // Try to find and unload the world (could be either name format)
        World bukkitWorld = Bukkit.getWorld(islandId);
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(oldStandardWorldPath);
        }
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(newStandardWorldPath);
        }
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(oldSlimeWorldName);
        }
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld(newSlimeWorldName);
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
                
                // Try both old and new naming conventions for slime worlds
                boolean existsOld = (Boolean) slimeLoaderClass.getMethod("worldExists", String.class).invoke(slimeLoader, oldSlimeWorldName);
                boolean existsNew = (Boolean) slimeLoaderClass.getMethod("worldExists", String.class).invoke(slimeLoader, newSlimeWorldName);
                boolean existsPlain = (Boolean) slimeLoaderClass.getMethod("worldExists", String.class).invoke(slimeLoader, islandId);
                
                if (existsNew) {
                    slimeLoaderClass.getMethod("deleteWorld", String.class).invoke(slimeLoader, newSlimeWorldName);
                    plugin.getLogger().info("Deleted SlimeWorld: " + newSlimeWorldName);
                } else if (existsOld) {
                    slimeLoaderClass.getMethod("deleteWorld", String.class).invoke(slimeLoader, oldSlimeWorldName);
                    plugin.getLogger().info("Deleted SlimeWorld: " + oldSlimeWorldName);
                } else if (existsPlain) {
                    slimeLoaderClass.getMethod("deleteWorld", String.class).invoke(slimeLoader, islandId);
                    plugin.getLogger().info("Deleted SlimeWorld: " + islandId);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete SlimeWorld " + islandId + ": " + e.getMessage());
            }
        } else {
            // Delete standard world folder (check both old and new directory structures)
            try {
                // Try new directory structure first
                File worldFolder = new File(Bukkit.getWorldContainer(), newStandardWorldPath);
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                    plugin.getLogger().info("Deleted world folder: " + newStandardWorldPath);
                } else {
                    // Try old directory structure
                    worldFolder = new File(Bukkit.getWorldContainer(), oldStandardWorldPath);
                    if (worldFolder.exists()) {
                        deleteDirectory(worldFolder);
                        plugin.getLogger().info("Deleted world folder: " + oldStandardWorldPath);
                    } else {
                        // Fallback to original naming convention
                        worldFolder = new File(Bukkit.getWorldContainer(), islandId);
                        if (worldFolder.exists()) {
                            deleteDirectory(worldFolder);
                            plugin.getLogger().info("Deleted world folder: " + islandId);
                        }
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
        
        // Check old path format
        if (world == null) {
            world = Bukkit.getWorld("islands/" + islandId);
        }
        
        // Check old SlimeWorld format
        if (world == null) {
            world = Bukkit.getWorld("islands_" + islandId);
        }
        
        // Determine if nether or overworld based on id
        boolean isNetherIsland = islandId.contains("nether");
        
        // Check new standard world structure
        if (world == null) {
            if (isNetherIsland) {
                world = Bukkit.getWorld("skyeblock/nether/" + islandId);
            } else {
                world = Bukkit.getWorld("skyeblock/islands/" + islandId);
            }
        }
        
        // Check new SlimeWorld structure
        if (world == null) {
            if (isNetherIsland) {
                world = Bukkit.getWorld("skyeblock_nether_" + islandId);
            } else {
                world = Bukkit.getWorld("skyeblock_islands_" + islandId);
            }
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
        
        // Check if this is built-in ASWM (ASP server)
        try {
            if (slimePlugin.getClass().getName().contains("com.infernalsuite.aswm")) {
                return "ASWM (Built-in)";
            }
        } catch (Exception e) {
            // Ignore, fall through to plugin name check
        }
        
        // Check plugin name for plugin-based managers
        if (slimePlugin.getName().equals("AdvancedSlimeWorldManager")) {
            return "ASWM";
        } else if (slimePlugin.getName().equals("SlimeWorldManager")) {
            return "SWM";
        }
        
        // Fallback - try to identify by class name
        String className = slimePlugin.getClass().getName();
        if (className.contains("aswm") || className.contains("AdvancedSlimeWorldManager")) {
            return "ASWM";
        } else if (className.contains("swm") || className.contains("SlimeWorldManager")) {
            return "SWM";
        }
        
        return "Unknown";
    }

    /**
     * Get detailed status information about the world manager for debugging
     */
    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        status.append("§6=== WorldManager Status ===\n");
        status.append("§7ASWM Enabled: §").append(slimeWorldEnabled ? "a✓ Yes" : "c✗ No").append("\n");
        status.append("§7Plugin Found: §").append(slimePlugin != null ? "a✓ " + slimePlugin.getName() : "c✗ None").append("\n");
        status.append("§7Loader Available: §").append(slimeLoader != null ? "a✓ Yes" : "c✗ No").append("\n");
        status.append("§7Manager Type: §b").append(getSlimeWorldManagerType()).append("\n");
        status.append("§7Main World: §e").append(skyBlockWorld != null ? skyBlockWorld.getName() : "null").append("\n");
        status.append("§7Nether World: §c").append(skyBlockNetherWorld != null ? skyBlockNetherWorld.getName() : "disabled").append("\n");
        status.append("§7Tracked Islands: §d").append(islandWorlds.size()).append(" worlds");
        return status.toString();
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
