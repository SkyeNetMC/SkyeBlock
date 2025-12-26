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
        
        // Initialize hub world instead of skyblock world
        createHubWorld();
        
        // Note: No longer auto-creating nether/end worlds here
        // Resource worlds are managed by ResourceWorldManager
    }

    private void checkForASWM() {
        try {
            // Check for built-in ASWM (ASP servers)
            if (checkForBuiltInASWM()) {
                return;
            }
            
            // Check for ASWM plugin
            if (checkForASWMPlugin()) {
                return;
            }
            
            // Check for legacy SWM plugin
            if (checkForLegacySWM()) {
                return;
            }
            
            plugin.getLogger().info("⚠ No SlimeWorldManager found. Islands will use standard Bukkit worlds.");
            plugin.getLogger().info("⚠ For better performance, install ASWM plugin or use an ASP server with built-in ASWM.");
            
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

    private void createHubWorld() {
        // Get hub world name from config, default to "hub"
        String hubWorldName = plugin.getConfig().getString("hub.world", "hub");
        
        // Check if hub world already exists
        skyBlockWorld = Bukkit.getWorld(hubWorldName);
        
        if (skyBlockWorld == null) {
            plugin.getLogger().info("Creating void hub world: " + hubWorldName);
            
            // Create new hub world as void
            WorldCreator creator = new WorldCreator(hubWorldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            creator.generator(new VoidWorldGenerator());
            
            skyBlockWorld = creator.createWorld();
            
            if (skyBlockWorld != null) {
                // Set world properties - use config settings for mob spawning
                boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
                boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
                skyBlockWorld.setSpawnFlags(allowAnimals, allowMonsters);
                skyBlockWorld.setTime(6000);
                skyBlockWorld.setStorm(false);
                skyBlockWorld.setThundering(false);
                skyBlockWorld.setWeatherDuration(Integer.MAX_VALUE);
                
                // Set difficulty from config
                String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
                try {
                    org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(difficultyStr.toUpperCase());
                    skyBlockWorld.setDifficulty(difficulty);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid difficulty setting: " + difficultyStr + ", using NORMAL");
                    skyBlockWorld.setDifficulty(org.bukkit.Difficulty.NORMAL);
                }
                
                // Set spawn location from config
                double spawnX = plugin.getConfig().getDouble("hub.spawn.x", 0);
                double spawnY = plugin.getConfig().getDouble("hub.spawn.y", 100);
                double spawnZ = plugin.getConfig().getDouble("hub.spawn.z", 0);
                
                skyBlockWorld.setSpawnLocation((int) spawnX, (int) spawnY, (int) spawnZ);
                
                plugin.getLogger().info("✓ Created void hub world: " + hubWorldName);
                plugin.getLogger().info("✓ Hub spawn set to: " + spawnX + ", " + spawnY + ", " + spawnZ);
            } else {
                plugin.getLogger().severe("✗ Failed to create hub world!");
            }
        } else {
            plugin.getLogger().info("✓ Loaded existing hub world: " + hubWorldName);
        }
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
        
        // Use config settings for mob spawning and difficulty
        String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
        boolean allowMonstersConfig = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
        boolean allowAnimalsConfig = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
        
        slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, difficulty, difficultyStr.toLowerCase());
        slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, allowMonsters, allowMonstersConfig);
        slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, allowAnimals, allowAnimalsConfig);
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
            
            // Set world border from config
            setupWorldBorder(bukkitWorld);
            
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
        
        // Use config settings for mob spawning and difficulty
        String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
        boolean allowMonstersConfig = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
        boolean allowAnimalsConfig = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
        
        slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, difficulty, difficultyStr.toLowerCase());
        slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, allowMonsters, allowMonstersConfig);
        slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(properties, allowAnimals, allowAnimalsConfig);
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
            
            // Set world border from config
            setupWorldBorder(bukkitWorld);
            
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
                // Set world properties - use config settings for mob spawning
                boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
                boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
                world.setSpawnFlags(allowAnimals, allowMonsters);
                world.setTime(6000);
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);
                
                // Set difficulty from config
                String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
                try {
                    org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(difficultyStr.toUpperCase());
                    world.setDifficulty(difficulty);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid difficulty setting: " + difficultyStr + ", using NORMAL");
                    world.setDifficulty(org.bukkit.Difficulty.NORMAL);
                }
                
                // Set world border from config
                setupWorldBorder(world);
                
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
                teleportToSpawn(player);
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
        // This ensures gamerules are always correct when getting an island world
        if (world != null && !world.equals(skyBlockWorld)) {
            plugin.getIslandSettingsManager().applySettingsToWorld(islandId, world);
            plugin.getLogger().fine("Reapplied gamerules to island world: " + islandId);
        }
        
        return world != null ? world : skyBlockWorld;
    }

    /**
     * Gets an island world, loading it if necessary
     * This method ensures that island worlds are available even after server restart
     */
    public World getOrLoadIslandWorld(String islandId) {
        // First try to get the world if it's already loaded
        World world = getIslandWorld(islandId);
        
        // If we got the default skyblock world back, it means the island world wasn't found
        if (world == skyBlockWorld) {
            plugin.getLogger().info("Island world not loaded for " + islandId + " - attempting to load/create it");
            
            // Try to load/create the island world
            World loadedWorld = createIslandWorld(islandId);
            if (loadedWorld != null && !loadedWorld.equals(skyBlockWorld)) {
                plugin.getLogger().info("Successfully loaded island world for " + islandId + ": " + loadedWorld.getName());
                // Apply current mob spawning settings to the newly loaded world
                applyMobSpawningSettings(loadedWorld);
                // Apply island-specific gamerules from settings
                plugin.getIslandSettingsManager().applySettingsToWorld(islandId, loadedWorld);
                plugin.getLogger().info("Applied gamerules to newly loaded world: " + islandId);
                return loadedWorld;
            } else {
                plugin.getLogger().warning("Failed to load island world for " + islandId + " - using default world");
            }
        } else if (world != null && !world.equals(skyBlockWorld)) {
            // Apply current mob spawning settings to existing loaded world
            applyMobSpawningSettings(world);
            // Reapply island-specific gamerules to ensure they're correct
            plugin.getIslandSettingsManager().applySettingsToWorld(islandId, world);
        }
        
        return world;
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

    public void teleportToSpawn(org.bukkit.entity.Player player) {
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
        float yaw = (float) plugin.getConfig().getDouble("hub.spawn.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("hub.spawn.pitch", 0.0);

        org.bukkit.Location hubLocation = new org.bukkit.Location(hubWorld, x, y, z, yaw, pitch);
        player.teleport(hubLocation);
        plugin.sendMessage(player, "teleported-to-hub");
    }
    
    /**
     * Set up world border for an island world based on config settings
     */
    private void setupWorldBorder(World world) {
        try {
            boolean borderEnabled = plugin.getConfig().getBoolean("world.border.enabled", true);
            if (!borderEnabled) {
                plugin.getLogger().info("World border disabled in config for world: " + world.getName());
                return;
            }
            
            double borderSize = plugin.getConfig().getDouble("world.border.size", 10000);
            
            // Get world border
            org.bukkit.WorldBorder worldBorder = world.getWorldBorder();
            
            // Set border center to world spawn (0,0)
            worldBorder.setCenter(0, 0);
            
            // Set border size
            worldBorder.setSize(borderSize);
            
            // Optional: Set damage and warning settings
            worldBorder.setDamageAmount(0.2); // Damage per second when outside border
            worldBorder.setDamageBuffer(5.0); // Distance past border before damage starts
            worldBorder.setWarningDistance(5); // Distance from border to start warning
            worldBorder.setWarningTime(15); // Seconds of warning when border is moving
            
            plugin.getLogger().info("World border set for " + world.getName() + " - Size: " + borderSize + " blocks");
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set world border for " + world.getName() + ": " + e.getMessage());
        }
    }
    
    private boolean checkForASWMPlugin() {
        try {
            plugin.getLogger().info("Checking for ASWM plugin...");
            
            // Check if ASWM plugin is loaded
            Plugin aswmPlugin = plugin.getServer().getPluginManager().getPlugin("AdvancedSlimeWorldManager");
            if (aswmPlugin != null && aswmPlugin.isEnabled()) {
                plugin.getLogger().info("Found AdvancedSlimeWorldManager plugin, attempting to initialize...");
                
                // Try to get the API
                Class<?> apiClass = Class.forName("com.infernalsuite.aswm.api.AdvancedSlimeWorldManagerAPI");
                Object api = apiClass.getMethod("instance").invoke(null);
                
                if (api != null) {
                    Object loader = apiClass.getMethod("getLoader", String.class).invoke(api, "file");
                    if (loader != null) {
                        slimePlugin = aswmPlugin;
                        slimeLoader = loader;
                        slimeWorldEnabled = true;
                        plugin.getLogger().info("✓ Advanced Slime World Manager integration initialized successfully!");
                        plugin.getLogger().info("✓ ASWM plugin detected and configured - islands will use slime worlds");
                        return true;
                    } else {
                        plugin.getLogger().warning("ASWM plugin getLoader returned null");
                    }
                } else {
                    plugin.getLogger().warning("ASWM plugin API instance returned null");
                }
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("ASWM plugin API classes not found");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize ASWM plugin integration: " + e.getMessage());
        }
        
        return false;
    }
    
    private boolean checkForLegacySWM() {
        try {
            plugin.getLogger().info("Checking for legacy SlimeWorldManager plugin...");
            
            // Check if legacy SWM plugin is loaded
            Plugin swmPlugin = plugin.getServer().getPluginManager().getPlugin("SlimeWorldManager");
            if (swmPlugin != null && swmPlugin.isEnabled()) {
                plugin.getLogger().info("Found SlimeWorldManager plugin, attempting to initialize...");
                
                // Try to get the API
                Class<?> apiClass = Class.forName("com.grinderwolf.swm.api.SlimePlugin");
                Object api = apiClass.getMethod("getInstance").invoke(null);
                
                if (api != null) {
                    Object loader = apiClass.getMethod("getLoader", String.class).invoke(api, "file");
                    if (loader != null) {
                        slimePlugin = swmPlugin;
                        slimeLoader = loader;
                        slimeWorldEnabled = true;
                        plugin.getLogger().info("✓ SlimeWorldManager integration initialized successfully!");
                        plugin.getLogger().info("✓ Legacy SWM plugin detected and configured - islands will use slime worlds");
                        return true;
                    } else {
                        plugin.getLogger().warning("Legacy SWM plugin getLoader returned null");
                    }
                } else {
                    plugin.getLogger().warning("Legacy SWM plugin API instance returned null");
                }
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Legacy SWM plugin API classes not found");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize legacy SWM plugin integration: " + e.getMessage());
        }
        
        return false;
    }

    /**
     * Apply mob spawning settings from config to an existing world
     */
    private void applyMobSpawningSettings(World world) {
        if (world == null) return;
        
        // Set spawn flags from config
        boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
        boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
        world.setSpawnFlags(allowAnimals, allowMonsters);
        
        // Set difficulty from config
        String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
        try {
            org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(difficultyStr.toUpperCase());
            world.setDifficulty(difficulty);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid difficulty setting: " + difficultyStr + ", using NORMAL");
            world.setDifficulty(org.bukkit.Difficulty.NORMAL);
        }
        
        plugin.getLogger().info("Applied mob spawning settings to world " + world.getName() + 
                               " - Monsters: " + allowMonsters + ", Animals: " + allowAnimals + 
                               ", Difficulty: " + world.getDifficulty());
    }
    
    /**
     * Update mob spawning settings for all loaded island worlds
     * This can be used after changing config settings to apply them to existing worlds
     */
    public void updateMobSpawningForAllIslands() {
        plugin.getLogger().info("Updating mob spawning settings for all loaded island worlds...");
        int updatedWorlds = 0;
        
        for (World world : Bukkit.getWorlds()) {
            // Check if this is an island world (not the main skyblock world or other worlds)
            if (world.getName().startsWith("skyeblock_islands_") || 
                world.getName().contains("island-")) {
                applyMobSpawningSettings(world);
                updatedWorlds++;
            }
        }
        
        plugin.getLogger().info("Updated mob spawning settings for " + updatedWorlds + " island worlds");
    }
}
