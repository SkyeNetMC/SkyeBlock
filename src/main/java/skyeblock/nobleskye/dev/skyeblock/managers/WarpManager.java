package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Warp;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages server warps and teleportation
 */
public class WarpManager {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<String, Warp> warps;
    private final Map<UUID, Long> cooldowns;
    
    public WarpManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.warps = new HashMap<>();
        this.cooldowns = new HashMap<>();
    }
    
    /**
     * Initialize warps from configuration
     */
    public void initializeWarps() {
        if (!plugin.getWarpConfig().getBoolean("warps.enabled", true)) {
            plugin.getLogger().info("Warp system is disabled in configuration");
            return;
        }
        
        loadWarpsFromConfig();
        plugin.getLogger().info("Warp manager initialized with " + warps.size() + " warps");
    }
    
    /**
     * Load warps from configuration
     */
    private void loadWarpsFromConfig() {
        warps.clear();
        
        ConfigurationSection warpSection = plugin.getWarpConfig().getConfigurationSection("warps.warp-list");
        if (warpSection == null) {
            plugin.getLogger().warning("No warp-list configuration found");
            return;
        }
        
        for (String warpName : warpSection.getKeys(false)) {
            ConfigurationSection warpConfig = warpSection.getConfigurationSection(warpName);
            if (warpConfig == null || !warpConfig.getBoolean("enabled", true)) {
                continue;
            }
            
            try {
                Warp warp = createWarpFromConfig(warpName, warpConfig);
                if (warp != null) {
                    warps.put(warpName.toLowerCase(), warp);
                    plugin.getLogger().fine("Loaded warp: " + warpName);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load warp '" + warpName + "': " + e.getMessage());
            }
        }
    }
    
    /**
     * Create a warp object from configuration
     */
    private Warp createWarpFromConfig(String name, ConfigurationSection config) {
        // Get location
        ConfigurationSection locationConfig = config.getConfigurationSection("location");
        if (locationConfig == null) {
            plugin.getLogger().warning("Warp '" + name + "' has no location configuration");
            return null;
        }
        
        String worldName = locationConfig.getString("world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("Warp '" + name + "' references unknown world: " + worldName);
            return null;
        }
        
        double x = locationConfig.getDouble("x", 0);
        double y = locationConfig.getDouble("y", 100);
        double z = locationConfig.getDouble("z", 0);
        float yaw = (float) locationConfig.getDouble("yaw", 0);
        float pitch = (float) locationConfig.getDouble("pitch", 0);
        
        Location location = new Location(world, x, y, z, yaw, pitch);
        
        // Get other warp properties
        String displayName = config.getString("name", name);
        String permission = config.getString("permission", "skyeblock.warp." + name);
        Material material;
        try {
            material = Material.valueOf(config.getString("material", "GRASS_BLOCK").toUpperCase());
        } catch (IllegalArgumentException e) {
            material = Material.GRASS_BLOCK;
            plugin.getLogger().warning("Invalid material for warp '" + name + "', using GRASS_BLOCK");
        }
        int slot = config.getInt("slot", -1);
        List<String> lore = config.getStringList("lore");
        double cost = config.getDouble("cost", 0);
        boolean enabled = config.getBoolean("enabled", true);
        
        // Create warp using constructor
        return new Warp(name, displayName, location, permission, material, slot, lore, cost, enabled);
    }
    
    /**
     * Teleport a player to a warp
     */
    public boolean teleportToWarp(Player player, String warpName) {
        Warp warp = warps.get(warpName.toLowerCase());
        if (warp == null) {
            sendMessage(player, "warp-not-found", Map.of("warp_name", warpName));
            return false;
        }
        
        if (!warp.isEnabled()) {
            sendMessage(player, "warp-not-found", Map.of("warp_name", warpName));
            return false;
        }
        
        // Check permission
        if (!player.hasPermission(warp.getPermission())) {
            sendMessage(player, "warp-no-permission");
            return false;
        }
        
        // Check cooldown
        if (isOnCooldown(player)) {
            long remainingTime = getCooldownRemaining(player);
            String timeFormat = formatCooldownTime(remainingTime);
            sendMessage(player, "warp-cooldown", Map.of("time", timeFormat));
            return false;
        }
        
        // Check cost (if economy plugin is present)
        if (warp.getCost() > 0 && !chargePlayer(player, warp.getCost())) {
            sendMessage(player, "warp-insufficient-funds", Map.of("cost", String.valueOf(warp.getCost())));
            return false;
        }
        
        // Teleport player
        Location location = warp.getLocation();
        if (location.getWorld() == null) {
            plugin.getLogger().warning("Warp '" + warpName + "' has null world");
            sendMessage(player, "warp-not-found", Map.of("warp_name", warpName));
            return false;
        }
        
        player.teleport(location);
        setCooldown(player);
        
        sendMessage(player, "warp-teleported", Map.of("warp_name", warp.getDisplayName()));
        
        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        
        return true;
    }
    
    /**
     * Get all available warps for a player
     */
    public List<Warp> getAvailableWarps(Player player) {
        return warps.values().stream()
            .filter(warp -> warp.isEnabled())
            .filter(warp -> player.hasPermission(warp.getPermission()))
            .sorted(Comparator.comparing(warp -> warp.getSlot() >= 0 ? warp.getSlot() : Integer.MAX_VALUE))
            .collect(Collectors.toList());
    }
    
    /**
     * Get all warps (admin)
     */
    public Collection<Warp> getAllWarps() {
        return warps.values();
    }
    
    /**
     * Get a specific warp
     */
    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }
    
    /**
     * Create a new warp
     */
    public boolean createWarp(String name, Location location, Player creator) {
        if (warps.containsKey(name.toLowerCase())) {
            return false;
        }
        
        Warp warp = new Warp(
            name.toLowerCase(),
            name,
            location,
            "skyeblock.warp." + name.toLowerCase(),
            Material.BEACON,
            -1,
            Arrays.asList("<gray>Custom warp", "<gray>Created by: <white>" + creator.getName()),
            0.0,
            true
        );
        
        warps.put(name.toLowerCase(), warp);
        
        // Note: This doesn't save to config automatically - would need separate method for that
        plugin.getLogger().info("Warp '" + name + "' created by " + creator.getName() + " at " + 
            location.getWorld().getName() + " " + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ());
        
        return true;
    }
    
    /**
     * Delete a warp
     */
    public boolean deleteWarp(String name) {
        return warps.remove(name.toLowerCase()) != null;
    }
    
    /**
     * Check if player is on warp cooldown
     */
    private boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        
        long lastWarp = cooldowns.get(playerId);
        long cooldownTime = plugin.getWarpConfig().getLong("warps.cooldown-seconds", 5) * 1000L;
        
        return System.currentTimeMillis() - lastWarp < cooldownTime;
    }
    
    /**
     * Get remaining cooldown time in milliseconds
     */
    private long getCooldownRemaining(Player player) {
        UUID playerId = player.getUniqueId();
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long lastWarp = cooldowns.get(playerId);
        long cooldownTime = plugin.getWarpConfig().getLong("warps.cooldown-seconds", 5) * 1000L;
        long remaining = cooldownTime - (System.currentTimeMillis() - lastWarp);
        
        return Math.max(0, remaining);
    }
    
    /**
     * Set cooldown for player
     */
    private void setCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    /**
     * Format cooldown time for display
     */
    private String formatCooldownTime(long milliseconds) {
        long seconds = milliseconds / 1000L;
        if (seconds <= 0) {
            return "0s";
        }
        return seconds + "s";
    }
    
    /**
     * Charge player for warp (if economy plugin present)
     */
    private boolean chargePlayer(Player player, double cost) {
        // TODO: Integrate with economy plugin if available
        // For now, assume player can always afford it
        return true;
    }
    
    /**
     * Send formatted message to player
     */
    private void sendMessage(Player player, String messageKey) {
        sendMessage(player, messageKey, Collections.emptyMap());
    }
    
    /**
     * Send formatted message to player with placeholders
     */
    private void sendMessage(Player player, String messageKey, Map<String, String> placeholders) {
        String prefix = plugin.getWarpConfig().getString("messages.prefix", "");
        String message = plugin.getWarpConfig().getString("messages." + messageKey, messageKey);
        
        // Replace placeholders
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        player.sendMessage(miniMessage.deserialize(prefix + message));
    }
    
    /**
     * Process warp lore with placeholders
     */
    public List<String> processWarpLore(Warp warp, Player player) {
        List<String> processedLore = new ArrayList<>();
        
        for (String line : warp.getLore()) {
            String processed = line;
            
            // Replace reset time placeholders
            if (processed.contains("{nether_reset_time}")) {
                String resetTime = plugin.getResourceWorldManager().getTimeUntilReset("nether");
                processed = processed.replace("{nether_reset_time}", resetTime);
            }
            
            if (processed.contains("{end_reset_time}")) {
                String resetTime = plugin.getResourceWorldManager().getTimeUntilReset("end");
                processed = processed.replace("{end_reset_time}", resetTime);
            }
            
            // Add other placeholders as needed
            processed = processed.replace("{player}", player.getName());
            
            processedLore.add(processed);
        }
        
        return processedLore;
    }
    
    /**
     * Reload warps from configuration
     */
    public void reloadWarps() {
        loadWarpsFromConfig();
        plugin.getLogger().info("Warps reloaded - " + warps.size() + " warps loaded");
    }
    
    /**
     * Get warp names for tab completion
     */
    public List<String> getWarpNames() {
        return new ArrayList<>(warps.keySet());
    }
    
    /**
     * Get warp names available to a specific player
     */
    public List<String> getAvailableWarpNames(Player player) {
        return warps.values().stream()
            .filter(warp -> warp.isEnabled())
            .filter(warp -> player.hasPermission(warp.getPermission()))
            .map(Warp::getName)
            .collect(Collectors.toList());
    }
    
    /**
     * Check if a warp exists
     */
    public boolean hasWarp(String warpName) {
        return warps.containsKey(warpName.toLowerCase());
    }
    
    /**
     * Check if player has permission for a specific warp
     */
    public boolean hasPermission(Player player, String warpName) {
        Warp warp = warps.get(warpName.toLowerCase());
        return warp != null && player.hasPermission(warp.getPermission());
    }
    
    /**
     * Warp a player (alias for teleportToWarp)
     */
    public boolean warpPlayer(Player player, String warpName) {
        return teleportToWarp(player, warpName);
    }
    
    /**
     * Create a new warp (overloaded method without Player parameter)
     */
    public boolean createWarp(String name, Location location) {
        if (hasWarp(name)) {
            return false;
        }
        
        // Create basic warp with default settings
        Warp warp = new Warp(
            name.toLowerCase(),
            name,
            location,
            "skyeblock.warp." + name.toLowerCase(),
            Material.GRASS_BLOCK,
            -1, // No GUI slot
            Arrays.asList("<gray>Teleport to " + name),
            0.0,
            true
        );
        
        warps.put(name.toLowerCase(), warp);
        return true;
    }
}
