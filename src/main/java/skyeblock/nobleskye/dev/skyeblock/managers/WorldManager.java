package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.exceptions.UnknownWorldException;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimeProperties;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import com.infernalsuite.asp.loaders.file.FileLoader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class WorldManager {
    private final SkyeBlockPlugin plugin;
    private World skyBlockWorld;
    private World skyBlockNetherWorld;
    private AdvancedSlimePaperAPI aspAPI;
    private SlimeLoader slimeLoader;
    private boolean slimeWorldEnabled = false;
    private boolean aspApiAvailable = false;

    public WorldManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
    }

    public void initializeWorld() {
        checkForASWM();
        createHubWorld();
    }

    private void checkForASWM() {
        try {
            if (checkForASP() || checkForLegacySWM()) {
                return;
            }
            plugin.getLogger().info("No SlimeWorldManager found. Islands will use standard Bukkit worlds.");
        } catch (Exception e) {
            plugin.getLogger().warning("Unexpected error during SlimeWorldManager detection: " + e.getMessage());
        }
        slimeWorldEnabled = false;
    }

    private boolean checkForASP() {
        try {
            plugin.getLogger().info("Checking for AdvancedSlimePaper API (ASP 4.0.0)...");
            aspAPI = AdvancedSlimePaperAPI.instance();
            if (aspAPI != null) {
                plugin.getLogger().info("Found ASP API instance");
                File worldsDir = new File(plugin.getDataFolder(), "slime_worlds");
                slimeLoader = new FileLoader(worldsDir);
                aspApiAvailable = true;
                slimeWorldEnabled = true;
                plugin.getLogger().info("Advanced Slime World Manager (ASP 4.0.0) initialized");
                return true;
            }
        } catch (NoClassDefFoundError e) {
            plugin.getLogger().info("ASP API not available");
        } catch (Exception e) {
            plugin.getLogger().info("Failed to initialize ASP API: " + e.getMessage());
        }
        return false;
    }

    private boolean checkForLegacySWM() {
        try {
            plugin.getLogger().info("Checking for legacy SlimeWorldManager plugin...");
            Plugin swmPlugin = plugin.getServer().getPluginManager().getPlugin("SlimeWorldManager");
            if (swmPlugin != null && swmPlugin.isEnabled()) {
                Class<?> apiClass = Class.forName("com.grinderwolf.swm.api.SlimePlugin");
                Object api = apiClass.getMethod("getInstance").invoke(null);
                if (api != null) {
                    Object loader = apiClass.getMethod("getLoader", String.class).invoke(api, "file");
                    if (loader != null) {
                        aspAPI = null;
                        slimeLoader = new LegacySWMLoaderWrapper(loader, loader.getClass());
                        slimeWorldEnabled = true;
                        plugin.getLogger().info("Legacy SWM plugin detected and configured");
                        return true;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().info("Legacy SWM plugin API classes not found");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to initialize legacy SWM: " + e.getMessage());
        }
        return false;
    }

    private void createHubWorld() {
        String hubWorldName = plugin.getConfig().getString("hub.world", "hub");
        skyBlockWorld = Bukkit.getWorld(hubWorldName);
        if (skyBlockWorld == null) {
            plugin.getLogger().info("Creating void hub world: " + hubWorldName);
            WorldCreator creator = new WorldCreator(hubWorldName);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
            creator.generator(new VoidWorldGenerator());
            skyBlockWorld = creator.createWorld();
            if (skyBlockWorld != null) {
                boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
                boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
                skyBlockWorld.setSpawnFlags(allowAnimals, allowMonsters);
                skyBlockWorld.setTime(6000);
                skyBlockWorld.setStorm(false);
                skyBlockWorld.setThundering(false);
                skyBlockWorld.setWeatherDuration(Integer.MAX_VALUE);
                String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
                try {
                    org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(difficultyStr.toUpperCase());
                    skyBlockWorld.setDifficulty(difficulty);
                } catch (IllegalArgumentException e) {
                    skyBlockWorld.setDifficulty(org.bukkit.Difficulty.NORMAL);
                }
                double spawnX = plugin.getConfig().getDouble("hub.spawn.x", 0);
                double spawnY = plugin.getConfig().getDouble("hub.spawn.y", 100);
                double spawnZ = plugin.getConfig().getDouble("hub.spawn.z", 0);
                skyBlockWorld.setSpawnLocation((int) spawnX, (int) spawnY, (int) spawnZ);
                plugin.getLogger().info("Created void hub world: " + hubWorldName);
            } else {
                plugin.getLogger().severe("Failed to create hub world!");
            }
        } else {
            plugin.getLogger().info("Loaded existing hub world: " + hubWorldName);
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
        if (slimeWorldEnabled && slimeLoader != null) {
            try {
                World slimeWorld = createSlimeWorld(islandId);
                if (slimeWorld != null) {
                    return slimeWorld;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create slime world for " + islandId + ": " + e.getMessage());
                if (plugin.getConfig().getBoolean("debug", false)) {
                    e.printStackTrace();
                }
                plugin.getLogger().info("Falling back to standard Bukkit world creation...");
            }
        }
        return createStandardWorld(islandId);
    }

    private World createSlimeWorld(String islandId) {
        boolean isNetherIsland = islandId.contains("nether");
        String worldName = isNetherIsland
            ? "skyeblock_nether_" + islandId
            : "skyeblock_islands_" + islandId;

        if (aspApiAvailable) {
            return createASPWorld(worldName, islandId, isNetherIsland);
        }
        return createLegacySWMWorld(worldName, islandId, isNetherIsland);
    }

    private World createASPWorld(String worldName, String islandId, boolean isNetherIsland) {
        try {
            SlimePropertyMap properties = new SlimePropertyMap();
            properties.setValue(SlimeProperties.SPAWN_X, 0);
            properties.setValue(SlimeProperties.SPAWN_Y, 100);
            properties.setValue(SlimeProperties.SPAWN_Z, 0);
            String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
            boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
            boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
            properties.setValue(SlimeProperties.DIFFICULTY, difficultyStr.toLowerCase());
            properties.setValue(SlimeProperties.ALLOW_MONSTERS, allowMonsters);
            properties.setValue(SlimeProperties.ALLOW_ANIMALS, allowAnimals);
            properties.setValue(SlimeProperties.PVP, false);
            if (isNetherIsland) {
                properties.setValue(SlimeProperties.ENVIRONMENT, "nether");
            } else {
                properties.setValue(SlimeProperties.ENVIRONMENT, "normal");
            }
            properties.setValue(SlimeProperties.WORLD_TYPE, "flat");
            properties.setValue(SlimeProperties.DEFAULT_BIOME, "minecraft:plains");

            boolean worldExists = false;
            try {
                worldExists = slimeLoader.worldExists(worldName);
            } catch (IOException e) {
                plugin.getLogger().warning("Error checking if world exists: " + e.getMessage());
            }

            SlimeWorld slimeWorld;
            if (worldExists) {
                slimeWorld = aspAPI.readWorld(slimeLoader, worldName, false, properties);
            } else {
                slimeWorld = aspAPI.createEmptyWorld(worldName, false, properties, slimeLoader);
                aspAPI.saveWorld(slimeWorld);
            }

            SlimeWorldInstance instance = aspAPI.loadWorld(slimeWorld, true);
            World bukkitWorld = instance.getBukkitWorld();

            if (bukkitWorld != null) {
                bukkitWorld.setTime(6000);
                bukkitWorld.setStorm(false);
                bukkitWorld.setThundering(false);
                bukkitWorld.setWeatherDuration(Integer.MAX_VALUE);
                setupWorldBorder(bukkitWorld);
                plugin.getLogger().info("ASP world created/loaded: " + worldName);
                return bukkitWorld;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("ASP world creation failed for " + worldName + ": " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private World createLegacySWMWorld(String worldName, String islandId, boolean isNetherIsland) {
        try {
            Class<?> loaderClass = slimeLoader.getClass();
            String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
            boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
            boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);

            Class<?> slimePropertyMapClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimePropertyMap");
            Object properties = slimePropertyMapClass.getConstructor().newInstance();
            Class<?> slimePropertiesClass = Class.forName("com.grinderwolf.swm.api.world.properties.SlimeProperties");

            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("SPAWN_X").get(null), 0);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("SPAWN_Y").get(null), 100);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("SPAWN_Z").get(null), 0);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("DIFFICULTY").get(null), difficultyStr.toLowerCase());
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("ALLOW_MONSTERS").get(null), allowMonsters);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("ALLOW_ANIMALS").get(null), allowAnimals);
            slimePropertyMapClass.getMethod("setValue", Object.class, Object.class).invoke(
                properties, slimePropertiesClass.getField("PVP").get(null), false);

            Class<?> slimePluginClass = Class.forName("com.grinderwolf.swm.api.SlimePlugin");
            Object slimePlugin = slimePluginClass.getMethod("getInstance").invoke(null);

            Object slimeWorld = slimePluginClass.getMethod("createEmptyWorld", Object.class, String.class, boolean.class, Object.class)
                .invoke(slimePlugin, slimeLoader, worldName, false, properties);
            slimePluginClass.getMethod("generateWorld", Object.class).invoke(slimePlugin, slimeWorld);

            World bukkitWorld = Bukkit.getWorld(worldName);
            if (bukkitWorld != null) {
                bukkitWorld.setTime(6000);
                bukkitWorld.setStorm(false);
                bukkitWorld.setThundering(false);
                bukkitWorld.setWeatherDuration(Integer.MAX_VALUE);
                setupWorldBorder(bukkitWorld);
                plugin.getLogger().info("Legacy SWM world created: " + worldName);
                return bukkitWorld;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Legacy SWM world creation failed: " + e.getMessage());
            if (plugin.getConfig().getBoolean("debug", false)) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private World createStandardWorld(String islandId) {
        try {
            boolean isNetherIsland = islandId.contains("nether");
            String worldPath;
            if (isNetherIsland) {
                worldPath = "skyeblock/nether/" + islandId;
            } else {
                worldPath = "skyeblock/islands/" + islandId;
            }

            WorldCreator creator = new WorldCreator(worldPath);
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);

            if (isNetherIsland) {
                creator.environment(World.Environment.NETHER);
            }

            creator.generator(new VoidWorldGenerator());

            World world = creator.createWorld();
            if (world != null) {
                boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
                boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
                world.setSpawnFlags(allowAnimals, allowMonsters);
                world.setTime(6000);
                world.setStorm(false);
                world.setThundering(false);
                world.setWeatherDuration(Integer.MAX_VALUE);

                String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
                try {
                    org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(difficultyStr.toUpperCase());
                    world.setDifficulty(difficulty);
                } catch (IllegalArgumentException e) {
                    world.setDifficulty(org.bukkit.Difficulty.NORMAL);
                }

                setupWorldBorder(world);
                plugin.getLogger().info("Standard Bukkit world created: " + worldPath);
                return world;
            } else {
                plugin.getLogger().warning("Standard Bukkit world creation failed for: " + worldPath);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create standard world " + islandId + ": " + e.getMessage());
        }

        return skyBlockWorld;
    }

    public boolean deleteIslandWorld(String islandId) {
        if (islandId.equals(skyBlockWorld.getName())) {
            return true;
        }

        boolean isNetherIsland = islandId.contains("nether");
        String newSlimeWorldName = isNetherIsland
            ? "skyeblock_nether_" + islandId
            : "skyeblock_islands_" + islandId;
        String oldSlimeWorldName = "islands_" + islandId;

        World bukkitWorld = Bukkit.getWorld(islandId);
        if (bukkitWorld == null) bukkitWorld = Bukkit.getWorld("islands/" + islandId);
        if (bukkitWorld == null) bukkitWorld = Bukkit.getWorld("skyeblock/islands/" + islandId);
        if (bukkitWorld == null) bukkitWorld = Bukkit.getWorld("skyeblock/nether/" + islandId);
        if (bukkitWorld == null) bukkitWorld = Bukkit.getWorld(oldSlimeWorldName);
        if (bukkitWorld == null) bukkitWorld = Bukkit.getWorld(newSlimeWorldName);

        if (bukkitWorld != null) {
            bukkitWorld.getPlayers().forEach(this::teleportToSpawn);
            if (!Bukkit.unloadWorld(bukkitWorld, false)) {
                plugin.getLogger().warning("Failed to unload world: " + bukkitWorld.getName());
            }
        }

        if (slimeWorldEnabled && slimeLoader != null) {
            try {
                if (slimeLoader.worldExists(newSlimeWorldName)) {
                    slimeLoader.deleteWorld(newSlimeWorldName);
                    plugin.getLogger().info("Deleted slime world: " + newSlimeWorldName);
                } else if (slimeLoader.worldExists(oldSlimeWorldName)) {
                    slimeLoader.deleteWorld(oldSlimeWorldName);
                    plugin.getLogger().info("Deleted slime world: " + oldSlimeWorldName);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to delete slime world " + islandId + ": " + e.getMessage());
            }
        } else {
            String newStandardWorldPath = isNetherIsland
                ? "skyeblock/nether/" + islandId
                : "skyeblock/islands/" + islandId;
            String oldStandardWorldPath = "islands/" + islandId;

            try {
                File worldFolder = new File(Bukkit.getWorldContainer(), newStandardWorldPath);
                if (worldFolder.exists()) {
                    deleteDirectory(worldFolder);
                    plugin.getLogger().info("Deleted world folder: " + newStandardWorldPath);
                } else {
                    worldFolder = new File(Bukkit.getWorldContainer(), oldStandardWorldPath);
                    if (worldFolder.exists()) {
                        deleteDirectory(worldFolder);
                        plugin.getLogger().info("Deleted world folder: " + oldStandardWorldPath);
                    } else {
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
        if (world == null) world = Bukkit.getWorld("islands/" + islandId);
        if (world == null) world = Bukkit.getWorld("islands_" + islandId);

        boolean isNetherIsland = islandId.contains("nether");

        if (world == null) {
            if (isNetherIsland) {
                world = Bukkit.getWorld("skyeblock/nether/" + islandId);
            } else {
                world = Bukkit.getWorld("skyeblock/islands/" + islandId);
            }
        }

        if (world == null) {
            if (isNetherIsland) {
                world = Bukkit.getWorld("skyeblock_nether_" + islandId);
            } else {
                world = Bukkit.getWorld("skyeblock_islands_" + islandId);
            }
        }

        if (world != null && !world.equals(skyBlockWorld)) {
            plugin.getIslandSettingsManager().applySettingsToWorld(islandId, world);
        }

        return world != null ? world : skyBlockWorld;
    }

    public World getOrLoadIslandWorld(String islandId) {
        World world = getIslandWorld(islandId);

        if (world == skyBlockWorld) {
            plugin.getLogger().info("Island world not loaded for " + islandId + " - attempting to load/create it");
            World loadedWorld = createIslandWorld(islandId);
            if (loadedWorld != null && !loadedWorld.equals(skyBlockWorld)) {
                applyMobSpawningSettings(loadedWorld);
                plugin.getIslandSettingsManager().applySettingsToWorld(islandId, loadedWorld);
                return loadedWorld;
            } else {
                plugin.getLogger().warning("Failed to load island world for " + islandId + " - using default world");
            }
        } else if (world != null && !world.equals(skyBlockWorld)) {
            applyMobSpawningSettings(world);
            plugin.getIslandSettingsManager().applySettingsToWorld(islandId, world);
        }

        return world;
    }

    public boolean isSlimeWorldEnabled() {
        return slimeWorldEnabled;
    }

    public String getSlimeWorldManagerType() {
        if (!slimeWorldEnabled) return "None";
        if (aspApiAvailable) return "ASWM (ASP 4.0.0)";
        return "SWM (Legacy)";
    }

    public String getDetailedStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== WorldManager Status ===\n");
        status.append("ASWM Enabled: ").append(slimeWorldEnabled ? "Yes" : "No").append("\n");
        status.append("ASP API Available: ").append(aspApiAvailable ? "Yes" : "No").append("\n");
        status.append("Loader Available: ").append(slimeLoader != null ? "Yes" : "No").append("\n");
        status.append("Manager Type: ").append(getSlimeWorldManagerType()).append("\n");
        status.append("Main World: ").append(skyBlockWorld != null ? skyBlockWorld.getName() : "null").append("\n");
        status.append("Nether World: ").append(skyBlockNetherWorld != null ? skyBlockNetherWorld.getName() : "disabled").append("\n");
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

    private void setupWorldBorder(World world) {
        try {
            boolean borderEnabled = plugin.getConfig().getBoolean("world.border.enabled", true);
            if (!borderEnabled) return;

            double borderSize = plugin.getConfig().getDouble("world.border.size", 10000);
            org.bukkit.WorldBorder worldBorder = world.getWorldBorder();
            worldBorder.setCenter(0, 0);
            worldBorder.setSize(borderSize);
            worldBorder.setDamageAmount(0.2);
            worldBorder.setDamageBuffer(5.0);
            worldBorder.setWarningDistance(5);
            worldBorder.setWarningTime(15);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to set world border for " + world.getName() + ": " + e.getMessage());
        }
    }

    private void applyMobSpawningSettings(World world) {
        if (world == null) return;
        boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
        boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
        world.setSpawnFlags(allowAnimals, allowMonsters);
        String difficultyStr = plugin.getConfig().getString("world.spawning.difficulty", "normal");
        try {
            org.bukkit.Difficulty difficulty = org.bukkit.Difficulty.valueOf(difficultyStr.toUpperCase());
            world.setDifficulty(difficulty);
        } catch (IllegalArgumentException e) {
            world.setDifficulty(org.bukkit.Difficulty.NORMAL);
        }
    }

    private void safeSetTime(World world, long time) {
        try {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                world.setTime(time);
            }
        } catch (Exception ignored) {
        }
    }

    public void updateMobSpawningForAllIslands() {
        plugin.getLogger().info("Updating mob spawning settings for all loaded island worlds...");
        int updatedWorlds = 0;
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().startsWith("skyeblock_islands_") || world.getName().contains("island-")) {
                applyMobSpawningSettings(world);
                updatedWorlds++;
            }
        }
        plugin.getLogger().info("Updated mob spawning settings for " + updatedWorlds + " island worlds");
    }

    private static class LegacySWMLoaderWrapper implements SlimeLoader {
        private final Object delegate;
        private final Class<?> delegateClass;

        LegacySWMLoaderWrapper(Object delegate, Class<?> delegateClass) {
            this.delegate = delegate;
            this.delegateClass = delegateClass;
        }

        @Override
        public byte[] readWorld(String worldName) throws UnknownWorldException, IOException {
            try {
                return (byte[]) delegateClass.getMethod("readWorld", String.class).invoke(delegate, worldName);
            } catch (Exception e) {
                throw new IOException("Failed to read world", e);
            }
        }

        @Override
        public boolean worldExists(String worldName) throws IOException {
            try {
                return (Boolean) delegateClass.getMethod("worldExists", String.class).invoke(delegate, worldName);
            } catch (Exception e) {
                throw new IOException("Failed to check world existence", e);
            }
        }

        @Override
        public java.util.List<String> listWorlds() throws IOException {
            try {
                return (java.util.List<String>) delegateClass.getMethod("listWorlds").invoke(delegate);
            } catch (Exception e) {
                throw new IOException("Failed to list worlds", e);
            }
        }

        @Override
        public void saveWorld(String worldName, byte[] serializedWorld) throws IOException {
            try {
                delegateClass.getMethod("saveWorld", String.class, byte[].class).invoke(delegate, worldName, serializedWorld);
            } catch (Exception e) {
                throw new IOException("Failed to save world", e);
            }
        }

        @Override
        public void deleteWorld(String worldName) throws UnknownWorldException, IOException {
            try {
                delegateClass.getMethod("deleteWorld", String.class).invoke(delegate, worldName);
            } catch (Exception e) {
                throw new IOException("Failed to delete world", e);
            }
        }
    }
}
