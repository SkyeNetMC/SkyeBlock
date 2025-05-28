package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IslandSettingsManager {
    private final SkyeBlockPlugin plugin;
    private final Map<String, Map<GameRule<?>, Object>> islandSettings;
    private File settingsFile;
    private FileConfiguration settingsConfig;
    
    // Supported gamerules with their default values
    private final Map<GameRule<?>, Object> defaultGameRules;
    
    public IslandSettingsManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.islandSettings = new HashMap<>();
        this.defaultGameRules = new HashMap<>();
        
        initializeDefaultGameRules();
        loadSettingsFile();
        loadIslandSettings();
    }
    
    @SuppressWarnings("unchecked")
    private void initializeDefaultGameRules() {
        // Boolean gamerules
        defaultGameRules.put(GameRule.DO_DAYLIGHT_CYCLE, true);
        defaultGameRules.put(GameRule.DO_WEATHER_CYCLE, true);
        defaultGameRules.put(GameRule.KEEP_INVENTORY, false);
        defaultGameRules.put(GameRule.MOB_GRIEFING, true);
        defaultGameRules.put(GameRule.DO_MOB_SPAWNING, true);
        defaultGameRules.put(GameRule.DO_FIRE_TICK, true);
        defaultGameRules.put(GameRule.FALL_DAMAGE, true);
        defaultGameRules.put(GameRule.FIRE_DAMAGE, true);
        defaultGameRules.put(GameRule.DROWNING_DAMAGE, true);
        defaultGameRules.put(GameRule.DO_INSOMNIA, true);
        defaultGameRules.put(GameRule.DO_IMMEDIATE_RESPAWN, false);
        defaultGameRules.put(GameRule.ANNOUNCE_ADVANCEMENTS, true);
        defaultGameRules.put(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, false);
        defaultGameRules.put(GameRule.DO_LIMITED_CRAFTING, false);
        defaultGameRules.put(GameRule.NATURAL_REGENERATION, true);
        defaultGameRules.put(GameRule.REDUCED_DEBUG_INFO, false);
        defaultGameRules.put(GameRule.SEND_COMMAND_FEEDBACK, true);
        defaultGameRules.put(GameRule.SHOW_DEATH_MESSAGES, true);
        defaultGameRules.put(GameRule.DO_ENTITY_DROPS, true);
        defaultGameRules.put(GameRule.DO_TILE_DROPS, true);
        defaultGameRules.put(GameRule.DO_MOB_LOOT, true);
        defaultGameRules.put(GameRule.DO_PATROL_SPAWNING, true);
        defaultGameRules.put(GameRule.DO_TRADER_SPAWNING, true);
        defaultGameRules.put(GameRule.FORGIVE_DEAD_PLAYERS, true);
        defaultGameRules.put(GameRule.UNIVERSAL_ANGER, false);
        
        // Integer gamerules
        defaultGameRules.put(GameRule.RANDOM_TICK_SPEED, 3);
        defaultGameRules.put(GameRule.SPAWN_RADIUS, 10);
        defaultGameRules.put(GameRule.MAX_ENTITY_CRAMMING, 24);
        defaultGameRules.put(GameRule.MAX_COMMAND_CHAIN_LENGTH, 65536);
        defaultGameRules.put(GameRule.PLAYERS_SLEEPING_PERCENTAGE, 100);
    }
    
    private void loadSettingsFile() {
        settingsFile = new File(plugin.getDataFolder(), "island-settings.yml");
        if (!settingsFile.exists()) {
            plugin.saveResource("island-settings.yml", false);
        }
        settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
    }
    
    private void loadIslandSettings() {
        if (settingsConfig.contains("islands")) {
            for (String islandId : settingsConfig.getConfigurationSection("islands").getKeys(false)) {
                Map<GameRule<?>, Object> settings = new HashMap<>();
                
                for (GameRule<?> gameRule : defaultGameRules.keySet()) {
                    String ruleName = gameRule.getName();
                    String path = "islands." + islandId + "." + ruleName;
                    
                    if (settingsConfig.contains(path)) {
                        if (gameRule.getType() == Boolean.class) {
                            settings.put(gameRule, settingsConfig.getBoolean(path));
                        } else if (gameRule.getType() == Integer.class) {
                            settings.put(gameRule, settingsConfig.getInt(path));
                        }
                    } else {
                        settings.put(gameRule, defaultGameRules.get(gameRule));
                    }
                }
                
                islandSettings.put(islandId, settings);
            }
        }
    }
    
    public void saveSettings() {
        try {
            // Clear existing settings
            settingsConfig.set("islands", null);
            
            // Save all island settings
            for (String islandId : islandSettings.keySet()) {
                Map<GameRule<?>, Object> settings = islandSettings.get(islandId);
                for (GameRule<?> gameRule : settings.keySet()) {
                    String path = "islands." + islandId + "." + gameRule.getName();
                    settingsConfig.set(path, settings.get(gameRule));
                }
            }
            
            settingsConfig.save(settingsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save island settings: " + e.getMessage());
        }
    }
    
    public Map<GameRule<?>, Object> getIslandSettings(String islandId) {
        return islandSettings.getOrDefault(islandId, new HashMap<>(defaultGameRules));
    }
    
    @SuppressWarnings("unchecked")
    public <T> void setGameRule(String islandId, GameRule<T> gameRule, T value) {
        islandSettings.computeIfAbsent(islandId, k -> new HashMap<>(defaultGameRules))
                .put(gameRule, value);
        
        // Apply to the actual world if it exists
        World world = plugin.getWorldManager().getIslandWorld(islandId);
        if (world != null && !world.equals(plugin.getWorldManager().getSkyBlockWorld())) {
            world.setGameRule(gameRule, value);
        }
        
        saveSettings();
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getGameRule(String islandId, GameRule<T> gameRule) {
        Map<GameRule<?>, Object> settings = getIslandSettings(islandId);
        return (T) settings.getOrDefault(gameRule, defaultGameRules.get(gameRule));
    }
    
    @SuppressWarnings("unchecked")
    public void applySettingsToWorld(String islandId, World world) {
        Map<GameRule<?>, Object> settings = getIslandSettings(islandId);
        
        for (Map.Entry<GameRule<?>, Object> entry : settings.entrySet()) {
            GameRule<?> gameRule = entry.getKey();
            Object value = entry.getValue();
            
            try {
                if (gameRule.getType() == Boolean.class) {
                    world.setGameRule((GameRule<Boolean>) gameRule, (Boolean) value);
                } else if (gameRule.getType() == Integer.class) {
                    world.setGameRule((GameRule<Integer>) gameRule, (Integer) value);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to apply gamerule " + gameRule.getName() + 
                    " to world " + world.getName() + ": " + e.getMessage());
            }
        }
    }
    
    public void createDefaultSettings(String islandId) {
        if (!islandSettings.containsKey(islandId)) {
            islandSettings.put(islandId, new HashMap<>(defaultGameRules));
            saveSettings();
        }
    }
    
    public void deleteIslandSettings(String islandId) {
        islandSettings.remove(islandId);
        saveSettings();
    }
    
    public List<GameRule<?>> getAvailableGameRules(Player player) {
        List<GameRule<?>> available = new ArrayList<>();
        
        // Check if player has admin bypass
        if (player.hasPermission("skyeblock.gamerules.adminbypass")) {
            return new ArrayList<>(defaultGameRules.keySet());
        }
        
        // Check individual permissions (LuckPerms style)
        // Show gamerule unless explicitly denied with false permission
        for (GameRule<?> gameRule : defaultGameRules.keySet()) {
            String permissionNode = "skyeblock.gamerule." + gameRule.getName().toLowerCase();
            
            // Check if permission is explicitly set to false
            if (player.isPermissionSet(permissionNode) && !player.hasPermission(permissionNode)) {
                // Permission is explicitly set to false, don't show this gamerule
                continue;
            }
            
            // Show the gamerule if:
            // 1. Permission is not set (default behavior)
            // 2. Permission is explicitly set to true
            available.add(gameRule);
        }
        
        return available;
    }
    
    public Map<GameRule<?>, Object> getDefaultGameRules() {
        return new HashMap<>(defaultGameRules);
    }
    
    public boolean hasPermissionForGameRule(Player player, GameRule<?> gameRule) {
        // Check admin bypass first
        if (player.hasPermission("skyeblock.gamerules.adminbypass")) {
            return true;
        }
        
        String permissionNode = "skyeblock.gamerule." + gameRule.getName().toLowerCase();
        
        // Check if permission is explicitly set to false
        if (player.isPermissionSet(permissionNode) && !player.hasPermission(permissionNode)) {
            return false;
        }
        
        // Allow access if:
        // 1. Permission is not set (default behavior)
        // 2. Permission is explicitly set to true
        return true;
    }
}
