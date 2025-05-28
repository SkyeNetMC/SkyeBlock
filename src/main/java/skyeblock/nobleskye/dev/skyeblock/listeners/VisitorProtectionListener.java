package skyeblock.nobleskye.dev.skyeblock.listeners;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.block.Block;
import org.bukkit.block.Container;

import java.util.UUID;

public class VisitorProtectionListener implements Listener {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    
    public VisitorProtectionListener(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot break blocks while visiting this island!</red>"));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot place blocks while visiting this island!</red>"));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        if (block == null || !isVisitorOnIsland(player)) {
            return;
        }
        
        // Check if it's a container (chest, furnace, etc.)
        if (block.getState() instanceof Container) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize("<red>You cannot access containers while visiting this island!</red>"));
            return;
        }
        
        // Block interaction with specific blocks that could be problematic
        switch (block.getType()) {
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case LEVER:
            case STONE_BUTTON:
            case OAK_BUTTON:
            case BIRCH_BUTTON:
            case SPRUCE_BUTTON:
            case JUNGLE_BUTTON:
            case ACACIA_BUTTON:
            case DARK_OAK_BUTTON:
            case CRIMSON_BUTTON:
            case WARPED_BUTTON:
            case POLISHED_BLACKSTONE_BUTTON:
            case STONE_PRESSURE_PLATE:
            case OAK_PRESSURE_PLATE:
            case BIRCH_PRESSURE_PLATE:
            case SPRUCE_PRESSURE_PLATE:
            case JUNGLE_PRESSURE_PLATE:
            case ACACIA_PRESSURE_PLATE:
            case DARK_OAK_PRESSURE_PLATE:
            case CRIMSON_PRESSURE_PLATE:
            case WARPED_PRESSURE_PLATE:
            case LIGHT_WEIGHTED_PRESSURE_PLATE:
            case HEAVY_WEIGHTED_PRESSURE_PLATE:
            case POLISHED_BLACKSTONE_PRESSURE_PLATE:
            case REDSTONE_WIRE:
            case COMPARATOR:
            case REPEATER:
            case DROPPER:
            case DISPENSER:
            case HOPPER:
            case OBSERVER:
            case PISTON:
            case STICKY_PISTON:
                // Allow these for visitors (buttons, pressure plates, etc.) - they're usually safe
                break;
            case CRAFTING_TABLE:
            case ENCHANTING_TABLE:
            case GRINDSTONE:
            case CARTOGRAPHY_TABLE:
            case FLETCHING_TABLE:
            case SMITHING_TABLE:
            case STONECUTTER:
            case LOOM:
                // Allow these crafting stations for visitors
                break;
            default:
                // For other interactive blocks, block interaction for visitors
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize("<red>You cannot interact with this block while visiting!</red>"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        // Block access to containers and storage inventories
        InventoryType type = event.getInventory().getType();
        switch (type) {
            case CHEST:
            case ENDER_CHEST:
            case SHULKER_BOX:
            case BARREL:
            case HOPPER:
            case DROPPER:
            case DISPENSER:
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
            case BREWING:
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize("<red>You cannot access containers while visiting this island!</red>"));
                break;
            default:
                // Allow other inventory types (crafting table, enchanting table, etc.)
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot pick up items while visiting this island!</red>"));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot drop items while visiting this island!</red>"));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        
        // Prevent visitors from changing game mode while on someone else's island
        if (isVisitorOnIsland(player)) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize("<red>You cannot change game mode while visiting this island!</red>"));
        }
    }
    
    private boolean isVisitorOnIsland(Player player) {
        UUID playerUUID = player.getUniqueId();
        String worldName = player.getWorld().getName();
        
        // Check if player is in an island world (starts with "island-")
        if (!worldName.startsWith("island-")) {
            return false;
        }
        
        // Find the island by checking all islands
        for (Island island : plugin.getIslandManager().getAllIslands()) {
            if (island.getIslandId().equals(worldName)) {
                // Check if player is the owner
                if (island.getOwnerUUID().equals(playerUUID)) {
                    return false; // Owner, not a visitor
                }
                
                // Check if player has coop access (they can bypass visitor restrictions)
                if (island.hasCoopAccess(playerUUID)) {
                    return false; // Has coop access, not restricted as visitor
                }
                
                // Player is on someone else's island and doesn't have coop access = visitor
                return true;
            }
        }
        
        return false;
    }
}
