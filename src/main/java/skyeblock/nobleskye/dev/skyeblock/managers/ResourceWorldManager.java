package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages resource worlds (Nether and End) with automatic reset functionality
 */
public class ResourceWorldManager {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<String, ResourceWorld> resourceWorlds;
    private final Map<String, Long> lastResetTimes;
    
    public ResourceWorldManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.resourceWorlds = new HashMap<>();
        this.lastResetTimes = new HashMap<>();
    }
    
    /**
     * Initialize resource worlds from configuration
     */
    public void initializeResourceWorlds() {
        if (!plugin.getWarpConfig().getBoolean("resource-worlds.enabled", true)) {
            plugin.getLogger().info("Resource worlds are disabled in configuration");
            return;
        }
        
        ConfigurationSection worldsSection = plugin.getWarpConfig().getConfigurationSection("resource-worlds");
        if (worldsSection == null) {
            plugin.getLogger().warning("No resource-worlds configuration found");
            return;
        }
        
        // Initialize Nether resource world
        if (worldsSection.getBoolean("nether.enabled", true)) {
            createResourceWorld("nether");
        }
        
        // Initialize End resource world
        if (worldsSection.getBoolean("end.enabled", true)) {
            createResourceWorld("end");
        }
        
        // Start reset scheduler
        startResetScheduler();
        
        plugin.getLogger().info("Resource world manager initialized with " + resourceWorlds.size() + " worlds");
    }
    
    /**
     * Create or load a resource world
     */
    private void createResourceWorld(String type) {
        try {
            ConfigurationSection config = plugin.getWarpConfig().getConfigurationSection("resource-worlds." + type);
            if (config == null) {
                plugin.getLogger().warning("No configuration found for resource world: " + type);
                return;
            }
            
            String worldName = config.getString("world-name", "resource_" + type);
            World.Environment environment = World.Environment.valueOf(config.getString("environment", "NORMAL"));
            
            // Check if world already exists
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                plugin.getLogger().info("Creating resource world: " + worldName);
                world = createWorld(worldName, environment, config);
                if (world == null) {
                    plugin.getLogger().severe("Failed to create resource world: " + worldName);
                    return;
                }
            } else {
                plugin.getLogger().info("Loaded existing resource world: " + worldName);
            }
            
            // Configure world settings
            configureWorld(world, config);
            
            // Create ResourceWorld object
            ResourceWorld resourceWorld = new ResourceWorld(type, worldName, world, config);
            resourceWorlds.put(type, resourceWorld);
            
            // Set last reset time to now if not exists
            if (!lastResetTimes.containsKey(type)) {
                lastResetTimes.put(type, System.currentTimeMillis());
            }
            
            plugin.getLogger().info("✓ Resource world '" + worldName + "' initialized successfully");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create resource world '" + type + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a new world with specified parameters
     */
    private World createWorld(String worldName, World.Environment environment, ConfigurationSection config) {
        try {
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            
            // Set generator
            String generator = config.getString("generator", "default");
            if ("void".equals(generator)) {
                creator.generator(new VoidWorldGenerator());
            }
            
            // Set seed if specified
            String seedStr = config.getString("settings.seed");
            if (seedStr != null && !seedStr.isEmpty()) {
                try {
                    long seed = Long.parseLong(seedStr);
                    creator.seed(seed);
                } catch (NumberFormatException e) {
                    creator.seed(seedStr.hashCode());
                }
            }
            
            // Set world type and generation settings
            creator.generateStructures(config.getBoolean("settings.structures", true));
            
            return creator.createWorld();
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create world: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Configure world settings after creation
     */
    private void configureWorld(World world, ConfigurationSection config) {
        // Set world border
        int borderSize = config.getInt("settings.size", 1000);
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(borderSize * 2); // Size is diameter
        border.setWarningDistance(50);
        border.setWarningTime(10);
        
        // Set difficulty
        String difficultyStr = config.getString("settings.difficulty", "normal");
        try {
            Difficulty difficulty = Difficulty.valueOf(difficultyStr.toUpperCase());
            world.setDifficulty(difficulty);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid difficulty '" + difficultyStr + "' for world " + world.getName());
        }
        
        // Set PvP
        world.setPVP(config.getBoolean("settings.pvp", false));
        
        // Set spawn flags
        boolean monsters = config.getBoolean("settings.monsters", true);
        boolean animals = config.getBoolean("settings.animals", true);
        world.setSpawnFlags(monsters, animals);
        
        // Set spawn location
        ConfigurationSection spawnConfig = config.getConfigurationSection("spawn");
        if (spawnConfig != null) {
            int x = spawnConfig.getInt("x", 0);
            int y = spawnConfig.getInt("y", 100);
            int z = spawnConfig.getInt("z", 0);
            float yaw = (float) spawnConfig.getDouble("yaw", 0);
            float pitch = (float) spawnConfig.getDouble("pitch", 0);
            
            Location spawnLocation = new Location(world, x, y, z, yaw, pitch);
            world.setSpawnLocation(spawnLocation);
        }
        
        // Set time and weather
        world.setTime(6000); // Noon
        world.setStorm(false);
        world.setThundering(false);
        world.setWeatherDuration(Integer.MAX_VALUE);
    }
    
    /**
     * Start the automatic reset scheduler
     */
    private void startResetScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForResets();
            }
        }.runTaskTimer(plugin, 20L * 60L, 20L * 60L); // Check every minute
    }
    
    /**
     * Check if any worlds need to be reset
     */
    private void checkForResets() {
        for (Map.Entry<String, ResourceWorld> entry : resourceWorlds.entrySet()) {
            String type = entry.getKey();
            ResourceWorld resourceWorld = entry.getValue();
            
            if (!resourceWorld.isResetEnabled()) {
                continue;
            }
            
            long lastReset = lastResetTimes.getOrDefault(type, System.currentTimeMillis());
            long timeSinceReset = System.currentTimeMillis() - lastReset;
            long resetInterval = resourceWorld.getResetIntervalHours() * 60L * 60L * 1000L; // Convert to milliseconds
            long timeUntilReset = resetInterval - timeSinceReset;
            
            // Check for warnings
            List<Integer> warningHours = resourceWorld.getWarningHours();
            for (int warningHour : warningHours) {
                long warningTime = warningHour * 60L * 60L * 1000L; // Convert to milliseconds
                
                // If we're within the warning window (give or take 1 minute for timing)
                if (Math.abs(timeUntilReset - warningTime) < 60000) {
                    sendResetWarning(resourceWorld, warningHour);
                    break;
                }
            }
            
            // Check if reset is needed
            if (timeUntilReset <= 0) {
                resetResourceWorld(type);
            }
        }
    }
    
    /**
     * Send reset warning to all players
     */
    private void sendResetWarning(ResourceWorld resourceWorld, int hoursUntilReset) {
        String timeFormat = formatTime(hoursUntilReset * 60); // Convert hours to minutes
        String message = plugin.getWarpConfig().getString("messages.resource-world-reset-warning")
            .replace("{world}", resourceWorld.getDisplayName())
            .replace("{time}", timeFormat);
            
        Bukkit.getOnlinePlayers().forEach(player -> 
            player.sendMessage(miniMessage.deserialize(plugin.getWarpConfig().getString("messages.prefix", "") + message)));
    }
    
    /**
     * Reset a resource world
     */
    public void resetResourceWorld(String type) {
        ResourceWorld resourceWorld = resourceWorlds.get(type);
        if (resourceWorld == null) {
            plugin.getLogger().warning("Attempted to reset unknown resource world: " + type);
            return;
        }
        
        plugin.getLogger().info("Resetting resource world: " + resourceWorld.getWorldName());
        
        try {
            // Teleport all players out of the world
            World world = resourceWorld.getWorld();
            Location hubLocation = getHubLocation();
            
            for (Player player : world.getPlayers()) {
                player.teleport(hubLocation);
                player.sendMessage(miniMessage.deserialize(
                    plugin.getWarpConfig().getString("messages.prefix", "") + 
                    "<yellow>You have been teleported to spawn due to world reset</yellow>"));
            }
            
            // Unload the world
            String worldName = resourceWorld.getWorldName();
            Bukkit.unloadWorld(world, false);
            
            // Delete world files
            deleteWorldFiles(worldName);
            
            // Recreate the world
            ConfigurationSection config = plugin.getWarpConfig().getConfigurationSection("resource-worlds." + type);
            World newWorld = createWorld(worldName, world.getEnvironment(), config);
            
            if (newWorld != null) {
                configureWorld(newWorld, config);
                resourceWorld.setWorld(newWorld);
                lastResetTimes.put(type, System.currentTimeMillis());
                
                // Broadcast reset message
                if (resourceWorld.isBroadcastReset()) {
                    String message = plugin.getWarpConfig().getString("messages.resource-world-reset-broadcast")
                        .replace("{world}", resourceWorld.getDisplayName());
                    Bukkit.broadcast(miniMessage.deserialize(plugin.getWarpConfig().getString("messages.prefix", "") + message));
                }
                
                plugin.getLogger().info("✓ Resource world '" + worldName + "' reset successfully");
            } else {
                plugin.getLogger().severe("✗ Failed to recreate resource world: " + worldName);
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to reset resource world '" + type + "': " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Force reset a resource world (admin command)
     */
    public boolean forceResetResourceWorld(String type) {
        if (!resourceWorlds.containsKey(type)) {
            return false;
        }
        
        resetResourceWorld(type);
        return true;
    }
    
    /**
     * Delete world files from disk
     */
    private void deleteWorldFiles(String worldName) {
        try {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                deleteDirectory(worldFolder);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to delete world files for: " + worldName);
        }
    }
    
    /**
     * Recursively delete a directory
     */
    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }
    
    /**
     * Get hub/spawn location for teleporting players
     */
    private Location getHubLocation() {
        // Try to get hub location from main config
        if (plugin.getConfig().getBoolean("hub.enabled", false)) {
            String hubWorldName = plugin.getConfig().getString("hub.world", "world");
            World hubWorld = Bukkit.getWorld(hubWorldName);
            if (hubWorld != null) {
                double x = plugin.getConfig().getDouble("hub.spawn.x", 0);
                double y = plugin.getConfig().getDouble("hub.spawn.y", 100);
                double z = plugin.getConfig().getDouble("hub.spawn.z", 0);
                return new Location(hubWorld, x, y, z);
            }
        }
        
        // Fallback to main world spawn
        World mainWorld = plugin.getWorldManager().getSkyBlockWorld();
        return mainWorld != null ? mainWorld.getSpawnLocation() : Bukkit.getWorlds().get(0).getSpawnLocation();
    }
    
    /**
     * Format time duration for display
     */
    private String formatTime(int totalMinutes) {
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int minutes = totalMinutes % 60;
        
        if (days > 0) {
            return plugin.getWarpConfig().getString("time-format.days", "{days}d {hours}h {minutes}m")
                .replace("{days}", String.valueOf(days))
                .replace("{hours}", String.valueOf(hours))
                .replace("{minutes}", String.valueOf(minutes));
        } else if (hours > 0) {
            return plugin.getWarpConfig().getString("time-format.hours", "{hours}h {minutes}m")
                .replace("{hours}", String.valueOf(hours))
                .replace("{minutes}", String.valueOf(minutes));
        } else {
            return plugin.getWarpConfig().getString("time-format.minutes", "{minutes}m")
                .replace("{minutes}", String.valueOf(minutes));
        }
    }
    
    /**
     * Get time until next reset for a resource world
     */
    public String getTimeUntilReset(String type) {
        ResourceWorld resourceWorld = resourceWorlds.get(type);
        if (resourceWorld == null || !resourceWorld.isResetEnabled()) {
            return "Never";
        }
        
        long lastReset = lastResetTimes.getOrDefault(type, System.currentTimeMillis());
        long timeSinceReset = System.currentTimeMillis() - lastReset;
        long resetInterval = resourceWorld.getResetIntervalHours() * 60L * 60L * 1000L;
        long timeUntilReset = resetInterval - timeSinceReset;
        
        if (timeUntilReset <= 0) {
            return "Resetting soon...";
        }
        
        int minutesUntilReset = (int) (timeUntilReset / (60L * 1000L));
        return formatTime(minutesUntilReset);
    }
    
    /**
     * Get a resource world by type
     */
    public ResourceWorld getResourceWorld(String type) {
        return resourceWorlds.get(type);
    }
    
    /**
     * Get all resource worlds
     */
    public Map<String, ResourceWorld> getResourceWorlds() {
        return new HashMap<>(resourceWorlds);
    }
    
    /**
     * Resource World data class
     */
    public static class ResourceWorld {
        private final String type;
        private final String worldName;
        private World world;
        private final ConfigurationSection config;
        
        public ResourceWorld(String type, String worldName, World world, ConfigurationSection config) {
            this.type = type;
            this.worldName = worldName;
            this.world = world;
            this.config = config;
        }
        
        public String getType() { return type; }
        public String getWorldName() { return worldName; }
        public World getWorld() { return world; }
        public void setWorld(World world) { this.world = world; }
        
        public String getDisplayName() {
            return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
        
        public boolean isResetEnabled() {
            return config.getBoolean("reset.enabled", false);
        }
        
        public int getResetIntervalHours() {
            return config.getInt("reset.interval-hours", 168); // Default 7 days
        }
        
        public List<Integer> getWarningHours() {
            return config.getIntegerList("reset.warning-hours");
        }
        
        public boolean isBroadcastReset() {
            return config.getBoolean("reset.broadcast-reset", true);
        }
        
        public Location getSpawnLocation() {
            ConfigurationSection spawnConfig = config.getConfigurationSection("spawn");
            if (spawnConfig != null && world != null) {
                int x = spawnConfig.getInt("x", 0);
                int y = spawnConfig.getInt("y", 100);
                int z = spawnConfig.getInt("z", 0);
                float yaw = (float) spawnConfig.getDouble("yaw", 0);
                float pitch = (float) spawnConfig.getDouble("pitch", 0);
                return new Location(world, x, y, z, yaw, pitch);
            }
            return world != null ? world.getSpawnLocation() : null;
        }
    }
    
    /**
     * Get next reset time for a resource world
     */
    public String getNextResetTime(String worldType) {
        ResourceWorld resourceWorld = resourceWorlds.get(worldType.toLowerCase());
        if (resourceWorld == null) {
            return "Unknown";
        }
        
        Long lastReset = lastResetTimes.get(worldType.toLowerCase());
        if (lastReset == null) {
            return "Soon";
        }
        
        long intervalMs = resourceWorld.getResetIntervalHours() * 60 * 60 * 1000L;
        long nextResetMs = lastReset + intervalMs;
        long currentMs = System.currentTimeMillis();
        
        if (nextResetMs <= currentMs) {
            return "Due for reset";
        }
        
        long remainingMs = nextResetMs - currentMs;
        long remainingHours = remainingMs / (60 * 60 * 1000L);
        long remainingDays = remainingHours / 24;
        
        if (remainingDays > 0) {
            return remainingDays + " day(s)";
        } else {
            return remainingHours + " hour(s)";
        }
    }
    
    /**
     * Force reset a resource world
     */
    public void forceResetWorld(String worldType) {
        ResourceWorld resourceWorld = resourceWorlds.get(worldType.toLowerCase());
        if (resourceWorld == null) {
            plugin.getLogger().warning("Cannot force reset unknown world type: " + worldType);
            return;
        }
        
        plugin.getLogger().info("Force resetting resource world: " + worldType);
        
        // Reset the world using existing method
        resetResourceWorld(worldType.toLowerCase());
        
        plugin.getLogger().info("Resource world '" + worldType + "' has been force reset");
    }
}
