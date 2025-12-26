package skyeblock.nobleskye.dev.skyeblock.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;

import java.util.UUID;

/**
 * Packet-level protection for visitors using ProtocolLib.
 */
public class VisitorPacketListener extends PacketAdapter {
    private final SkyeBlockPlugin plugin;
    private final java.util.Set<UUID> recentlyNotified = java.util.Collections.newSetFromMap(
        new java.util.concurrent.ConcurrentHashMap<UUID, Boolean>());

    public VisitorPacketListener(SkyeBlockPlugin plugin) {
        super(plugin, ListenerPriority.HIGHEST,  
              // Container interaction
              PacketType.Play.Client.WINDOW_CLICK,
              PacketType.Play.Client.CLOSE_WINDOW,
              PacketType.Play.Client.HELD_ITEM_SLOT,
              // Block interaction
              PacketType.Play.Client.USE_ENTITY,
              PacketType.Play.Client.USE_ITEM_ON,
              PacketType.Play.Client.BLOCK_DIG, 
              // Item interaction
              PacketType.Play.Client.SET_CREATIVE_SLOT,
              PacketType.Play.Client.ITEM_NAME,
              // Additional packets
              PacketType.Play.Client.USE_ITEM,
              PacketType.Play.Client.PICK_ITEM,
              PacketType.Play.Client.ENTITY_ACTION,
              PacketType.Play.Client.ARM_ANIMATION);
        this.plugin = plugin;
        
        // Schedule a task to clear the notification set every few seconds
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            recentlyNotified.clear();
        }, 100L, 100L); // Run every 5 seconds
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("skyeblock.admin.bypass")) return;
        
        // Get the island and check if player is a visitor
        Island island = plugin.getIslandManager().getIslandById(player.getWorld().getName());
        if (island == null) return;
        
        if (island.getCoopRole(player.getUniqueId()) == Island.CoopRole.VISITOR) {
            // Cancel the packet to block the interaction
            event.setCancelled(true);
            
            // Send notification message (throttled to avoid spam)
            if (!recentlyNotified.contains(player.getUniqueId())) {
                // Add player to recently notified set
                recentlyNotified.add(player.getUniqueId());
                
                // Get the packet type to give more specific messages
                final String packetName = event.getPacketType().name();
                final String actionMessage;
                
                if (packetName.contains("WINDOW") || packetName.contains("CREATIVE")) {
                    actionMessage = "open containers";
                } else if (packetName.contains("ENTITY")) {
                    actionMessage = "interact with entities";
                } else if (packetName.contains("BLOCK") || packetName.contains("USE_ITEM")) {
                    actionMessage = "interact with blocks";
                } else {
                    actionMessage = "interact with items";
                }
                
                // Schedule a sync task to send the message to avoid threading issues
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>You cannot " + actionMessage + " while visiting this island!</red>"));
                });
            }
        }
    }

    /**
     * Helper to register this listener
     */
    public static void register(SkyeBlockPlugin plugin) {
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        manager.addPacketListener(new VisitorPacketListener(plugin));
    }
}
