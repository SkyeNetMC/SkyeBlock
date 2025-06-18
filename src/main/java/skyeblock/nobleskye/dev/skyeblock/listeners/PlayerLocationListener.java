package skyeblock.nobleskye.dev.skyeblock.listeners;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

/**
 * Listener to track player locations for restoration on login
 */
public class PlayerLocationListener implements Listener {
    private final SkyeBlockPlugin plugin;

    public PlayerLocationListener(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Save the player's location when they quit
        // Don't save if they're in the hub world (to avoid sending them back to hub)
        String hubWorldName = plugin.getConfig().getString("hub.world", "hub");
        if (!player.getWorld().getName().equals(hubWorldName)) {
            plugin.getPlayerDataManager().updateLastLocation(player);
            plugin.getLogger().info("Saved location for " + player.getName() + " in world " + player.getWorld().getName());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        
        // Update location when player teleports to a non-hub world
        String hubWorldName = plugin.getConfig().getString("hub.world", "hub");
        if (!event.getTo().getWorld().getName().equals(hubWorldName)) {
            // Small delay to ensure teleport is complete
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getPlayerDataManager().updateLastLocation(player);
            }, 10L); // 0.5 second delay
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Update location when player moves to a non-hub world
        String hubWorldName = plugin.getConfig().getString("hub.world", "hub");
        if (!player.getWorld().getName().equals(hubWorldName)) {
            // Small delay to ensure world change is complete
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getPlayerDataManager().updateLastLocation(player);
            }, 10L); // 0.5 second delay
        }
    }
}
