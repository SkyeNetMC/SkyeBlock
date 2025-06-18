package skyeblock.nobleskye.dev.skyeblock.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;

/**
 * Listener to handle player join events
 */
public class PlayerJoinListener implements Listener {
    private final SkyeBlockPlugin plugin;

    public PlayerJoinListener(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Log when a player joins
        plugin.getLogger().info("Player " + player.getName() + " joined");
        
        // Small delay to ensure the player is fully connected
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Check if we should teleport to island or spawn/hub
            handlePlayerJoinTeleport(player);
        }, 20L); // 1 second delay (20 ticks)
    }
    
    /**
     * Handles player teleportation on join - either to their island or spawn/hub
     */
    private void handlePlayerJoinTeleport(Player player) {
        try {
            // Check if island teleport on join is enabled
            boolean teleportToIslandOnJoin = plugin.getConfig().getBoolean("island.teleport-to-island-on-join", false);
            
            if (teleportToIslandOnJoin) {
                // Try to teleport to their island first
                if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
                    plugin.getLogger().info("Teleporting " + player.getName() + " to their island on join");
                    if (plugin.getIslandManager().teleportToIsland(player)) {
                        plugin.getLogger().info("Successfully teleported " + player.getName() + " to their island");
                        return;
                    } else {
                        plugin.getLogger().warning("Failed to teleport " + player.getName() + " to their island, falling back to spawn");
                    }
                } else {
                    plugin.getLogger().info("Player " + player.getName() + " has no island, teleporting to spawn");
                }
            }
            
            // Fall back to spawn/hub teleportation
            teleportToSpawnOnJoin(player);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to handle join teleport for " + player.getName() + ": " + e.getMessage());
            // Emergency fallback - try to teleport to spawn
            try {
                teleportToSpawnOnJoin(player);
            } catch (Exception fallbackError) {
                plugin.getLogger().severe("Critical error: Failed to teleport " + player.getName() + " even to spawn: " + fallbackError.getMessage());
            }
        }
    }
    
    /**
     * Teleports player to their hub/spawn location on join
     */
    private void teleportToSpawnOnJoin(Player player) {
        try {
            // Check if hub teleportation is enabled
            if (!plugin.getConfig().getBoolean("hub.teleport-on-join", true)) {
                plugin.getLogger().info("Hub teleport on join is disabled for " + player.getName());
                return;
            }
            
            if (!plugin.getConfig().getBoolean("hub.enabled", true)) {
                plugin.getLogger().info("Hub is not enabled, skipping teleport for " + player.getName());
                return;
            }
            
            // Use the WorldManager's teleportToSpawn method as fallback
            plugin.getWorldManager().teleportToSpawn(player);
            plugin.getLogger().info("Teleported " + player.getName() + " to spawn/hub location");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to teleport " + player.getName() + " on join: " + e.getMessage());
        }
    }
}
