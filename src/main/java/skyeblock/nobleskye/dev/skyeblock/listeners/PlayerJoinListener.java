package skyeblock.nobleskye.dev.skyeblock.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import java.util.logging.Level;
import java.nio.charset.StandardCharsets;

/**
 * Listener to send custom server brand to players when they join or respawn
 */
public class PlayerJoinListener implements Listener {
    private final SkyeBlockPlugin plugin;
    private final String customBrand;

    public PlayerJoinListener(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        
        // Get the custom brand from config
        this.customBrand = plugin.getConfig().getString("server-brand.name", "LegitiSkyeSlimePaper");
        
        // Register the brand channel when the listener is created
        try {
            plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "minecraft:brand");
            plugin.getLogger().info("Registered minecraft:brand channel for server brand updates");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to register minecraft:brand channel: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Log when a player joins so we know when to send brand info
        plugin.getLogger().info("Player " + event.getPlayer().getName() + " joined, updating server brand...");
        
        // Small delay to ensure the player is fully connected
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updatePlayerBrand(event.getPlayer());
        }, 20L); // 1 second delay (20 ticks)
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Update brand when player respawns
        plugin.getLogger().info("Player " + event.getPlayer().getName() + " respawned, updating server brand...");
        
        // Small delay to ensure the player is fully respawned
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            updatePlayerBrand(event.getPlayer());
        }, 20L); // 1 second delay (20 ticks)
    }
    
    /**
     * Updates the server brand for a player using all available methods
     */
    public void updatePlayerBrand(Player player) {
        // Send the custom brand to the player using multiple methods for redundancy
        
        // Method 1: Use plugin messaging if available
        try {
            if (plugin.getServerBrandChanger() != null) {
                plugin.getServerBrandChanger().updatePlayerBrand(player);
                plugin.getLogger().info("Updated server brand for " + player.getName() + " using plugin messaging");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update player brand with plugin messaging: " + e.getMessage());
        }
        
        // Method 2: Use reflection if available
        try {
            if (plugin.getServerBrandListener() != null) {
                plugin.getServerBrandListener().updatePlayerBrand(player);
                plugin.getLogger().info("Updated server brand for " + player.getName() + " using reflection");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update player brand with reflection: " + e.getMessage());
        }
        
        // Method 3: Try direct packet sending (last resort)
        try {
            // Try to send brand via direct packet using Bukkit API
            player.sendPluginMessage(plugin, "minecraft:brand", 
                customBrand.getBytes(StandardCharsets.UTF_8));
            plugin.getLogger().info("Sent direct plugin message with brand to " + player.getName());
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to send direct plugin message: " + e.getMessage());
        }
        
        // Method 4: Try Spigot-specific brand changer
        try {
            if (plugin.getSpigotBrandModifier() != null) {
                plugin.getSpigotBrandModifier().updatePlayerBrand(player);
                plugin.getLogger().info("Updated server brand for " + player.getName() + " using Spigot modifier");
            }
        } catch (Exception e) {
            plugin.getLogger().fine("Failed to update player brand with SpigotBrandModifier: " + e.getMessage());
        }
    }
}
