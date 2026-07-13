package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class IslandSettingsManager {
    private final SkyeBlockPlugin plugin;
    private final DatabaseManager db;
    private final Map<String, Map<GameRule<?>, Object>> islandSettings;

    // Supported gamerules with their default values
    private final Map<GameRule<?>, Object> defaultGameRules;

    public IslandSettingsManager(SkyeBlockPlugin plugin, DatabaseManager db) {
        this.plugin = plugin;
        this.db = db;
        this.islandSettings = new HashMap<>();
        this.defaultGameRules = new HashMap<>();

        initializeDefaultGameRules();
        loadIslandSettings();
    }

    @SuppressWarnings("unchecked")
    private void initializeDefaultGameRules() {
        // Boolean gamerules
        defaultGameRules.put(GameRule.DO_DAYLIGHT_CYCLE, true);
        defaultGameRules.put(GameRule.DO_WEATHER_CYCLE, true);
        defaultGameRules.put(GameRule.KEEP_INVENTORY, true);
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

    private void loadIslandSettings() {
        Map<String, Map<GameRule<?>, Object>> loaded = db.loadAllIslandSettings();

        // Merge with defaults for any missing gamerules
        for (Map.Entry<String, Map<GameRule<?>, Object>> entry : loaded.entrySet()) {
            Map<GameRule<?>, Object> settings = new HashMap<>(defaultGameRules);
            settings.putAll(entry.getValue());
            islandSettings.put(entry.getKey(), settings);
        }

        plugin.getLogger().info("Loaded island settings for " + islandSettings.size() + " islands from H2");
    }

    public void saveSettings() {
        db.saveAllIslandSettings(islandSettings);
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
            Map<GameRule<?>, Object> defaults = new HashMap<>(defaultGameRules);
            islandSettings.put(islandId, defaults);
            db.saveIslandSettings(islandId, defaults);
        }
    }

    public void deleteIslandSettings(String islandId) {
        islandSettings.remove(islandId);
        db.deleteIslandSettings(islandId);
    }

    public List<GameRule<?>> getAvailableGameRules(Player player) {
        List<GameRule<?>> available = new ArrayList<>();

        if (player.hasPermission("skyeblock.gamerules.adminbypass")) {
            return new ArrayList<>(defaultGameRules.keySet());
        }

        for (GameRule<?> gameRule : defaultGameRules.keySet()) {
            String permissionNode = "skyeblock.gamerule." + gameRule.getName().toLowerCase();
            if (player.hasPermission(permissionNode)) {
                available.add(gameRule);
            }
        }

        return available;
    }

    public Map<GameRule<?>, Object> getDefaultGameRules() {
        return new HashMap<>(defaultGameRules);
    }

    public boolean hasPermissionForGameRule(Player player, GameRule<?> gameRule) {
        if (player.hasPermission("skyeblock.gamerules.adminbypass")) {
            return true;
        }

        String permissionNode = "skyeblock.gamerule." + gameRule.getName().toLowerCase();
        return player.hasPermission(permissionNode);
    }
}
