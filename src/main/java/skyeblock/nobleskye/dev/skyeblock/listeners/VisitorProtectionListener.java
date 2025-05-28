package skyeblock.nobleskye.dev.skyeblock.listeners;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
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
        
        // Block interaction with specific blocks - comprehensive protection like WorldGuard
        switch (block.getType()) {
            // Anvils - block all interactions
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize("<red>You cannot use anvils while visiting this island!</red>"));
                break;
                
            // Interactive blocks that should be blocked
            case LEVER:
            case TRIPWIRE_HOOK:
            case REDSTONE_TORCH:
            case REDSTONE_WALL_TORCH:
            case REDSTONE_WIRE:
            case COMPARATOR:
            case REPEATER:
            case OBSERVER:
            case PISTON:
            case STICKY_PISTON:
            case DROPPER:
            case DISPENSER:
            case HOPPER:
            case NOTE_BLOCK:
            case JUKEBOX:
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize("<red>You cannot interact with redstone devices while visiting!</red>"));
                break;
                
            // Buttons and pressure plates - allow these (they're usually safe for visitors)
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
                // Allow these for visitors (buttons, pressure plates, etc.) - they're usually safe
                break;
                
            // Crafting stations - allow these
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
                
            // Beds - block interaction
            case WHITE_BED:
            case ORANGE_BED:
            case MAGENTA_BED:
            case LIGHT_BLUE_BED:
            case YELLOW_BED:
            case LIME_BED:
            case PINK_BED:
            case GRAY_BED:
            case LIGHT_GRAY_BED:
            case CYAN_BED:
            case PURPLE_BED:
            case BLUE_BED:
            case BROWN_BED:
            case GREEN_BED:
            case RED_BED:
            case BLACK_BED:
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize("<red>You cannot use beds while visiting this island!</red>"));
                break;
                
            default:
                // For other interactive blocks, allow them unless they're problematic
                break;
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
            case BEACON:
                event.setCancelled(true);
                player.sendMessage(miniMessage.deserialize("<red>You cannot access containers while visiting this island!</red>"));
                break;
            default:
                // Allow other inventory types (crafting table, enchanting table, etc.)
                break;
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST) // Use HIGHEST priority to ensure this runs last
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        // Send message less frequently to avoid spam
        if (System.currentTimeMillis() % 1000 < 100) { // Only send message ~10% of the time
            player.sendMessage(miniMessage.deserialize("<red>You cannot pick up items while visiting this island!</red>"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST) // Use HIGHEST priority to ensure this runs last
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
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        // Block interaction with item frames, armor stands, and animals
        if (entity instanceof ItemFrame || entity instanceof ArmorStand || entity instanceof Animals) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize("<red>You cannot interact with entities while visiting this island!</red>"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityInteractWithContainer(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        // Block interaction with container entities like minecarts with chests, etc.
        if (entity.toString().contains("StorageMinecart") || 
            entity.toString().contains("HopperMinecart") || 
            entity.toString().contains("ChestMinecart") ||
            entity.toString().contains("PoweredMinecart") ||
            entity instanceof org.bukkit.entity.Villager ||
            entity.toString().toLowerCase().contains("merchant") ||
            (entity instanceof org.bukkit.entity.Vehicle && entity.toString().toLowerCase().contains("chest"))) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize("<red>You cannot access containers while visiting this island!</red>"));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        Entity entity = event.getEntity();
        
        // Block attacking animals and item frames/armor stands, but allow attacking monsters
        if (entity instanceof Animals || entity instanceof ItemFrame || entity instanceof ArmorStand) {
            event.setCancelled(true);
            player.sendMessage(miniMessage.deserialize("<red>You cannot attack entities while visiting this island!</red>"));
        }
        // Allow attacking monsters (they're hostile anyway)
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) {
            return;
        }
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot break item frames or paintings while visiting this island!</red>"));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (player == null || !isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot place item frames or paintings while visiting this island!</red>"));
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        
        if (!isVisitorOnIsland(player)) {
            return;
        }
        
        event.setCancelled(true);
        player.sendMessage(miniMessage.deserialize("<red>You cannot modify armor stands while visiting this island!</red>"));
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
                
                // Check the player's coop role - only VISITOR role is restricted
                Island.CoopRole playerRole = island.getCoopRole(playerUUID);
                if (playerRole == Island.CoopRole.VISITOR) {
                    return true; // VISITOR role is restricted
                }
                
                // MEMBER, ADMIN, CO_OWNER roles have full access
                return false;
            }
        }
        
        return false;
    }
}
