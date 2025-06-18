package skyeblock.nobleskye.dev.skyeblock.listeners;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.block.Action;
import org.bukkit.block.Block;

/**
 * Comprehensive visitor protection system that prevents all unauthorized interactions
 * when players are visiting islands as VISITORs
 */
public class VisitorProtectionListener implements Listener {
    
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;

    public VisitorProtectionListener(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Check if a player is a visitor and a specific action should be restricted
     */
    private boolean isVisitorRestricted(Player player, String action) {
        // Admin bypass
        if (player.hasPermission("skyeblock.admin.bypass")) {
            return false;
        }

        // Get the island from the world the player is in
        String worldName = player.getWorld().getName();
        Island island = plugin.getIslandManager().getIslandById(worldName);
        
        if (island == null) {
            return false; // Not on an island
        }

        // Check player's role on this island
        Island.CoopRole role = island.getCoopRole(player.getUniqueId());
        if (role != Island.CoopRole.VISITOR) {
            return false; // Not a visitor
        }
        
        // Check specific action permissions for visitors
        switch (action.toLowerCase()) {
            case "break_blocks":
                return !island.canVisitorBreakBlocks();
            case "place_blocks":
                return !island.canVisitorPlaceBlocks();
            case "open_containers":
                return !island.canVisitorOpenContainers();
            case "pickup_items":
                return !island.canVisitorPickupItems();
            case "drop_items":
                return !island.canVisitorDropItems();
            case "interact_entities":
                return !island.canVisitorInteractWithEntities();
            case "pvp":
                return !island.canVisitorUsePvp();
            case "redstone":
                return !island.canVisitorUseRedstone();
            default:
                // If adventure mode is enabled, block unknown actions
                return island.isAdventureModeForVisitors();
        }
    }

    /**
     * Check if a player is a visitor on the current island and should be restricted (legacy method)
     */
    private boolean isVisitorRestricted(Player player) {
        // Admin bypass
        if (player.hasPermission("skyeblock.admin.bypass")) {
            return false;
        }

        // Get the island from the world the player is in
        String worldName = player.getWorld().getName();
        Island island = plugin.getIslandManager().getIslandById(worldName);
        
        if (island == null) {
            return false; // Not on an island
        }

        // Check player's role on this island
        Island.CoopRole role = island.getCoopRole(player.getUniqueId());
        return role == Island.CoopRole.VISITOR && island.isAdventureModeForVisitors();
    }

    /**
     * Send restriction message to player
     */
    private void sendRestrictionMessage(Player player, String action) {
        player.sendMessage(miniMessage.deserialize(
            "<red>You cannot " + action + " while visiting this island!</red>"));
    }

    /**
     * Prevent block breaking for visitors
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (isVisitorRestricted(player, "break_blocks")) {
            event.setCancelled(true);
            sendRestrictionMessage(player, "break blocks");
        }
    }

    /**
     * Prevent block placing for visitors
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (isVisitorRestricted(player, "place_blocks")) {
            event.setCancelled(true);
            sendRestrictionMessage(player, "place blocks");
        }
    }

    /**
     * Prevent most interactions for visitors but allow basic navigation
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!isVisitorRestricted(player, "redstone")) {
            return;
        }

        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();
        
        // Allow some basic interactions for navigation
        if (clickedBlock != null) {
            Material blockType = clickedBlock.getType();
            
            // Allow these basic interaction blocks for navigation
            if (blockType == Material.OAK_DOOR || blockType == Material.SPRUCE_DOOR || 
                blockType == Material.BIRCH_DOOR || blockType == Material.JUNGLE_DOOR ||
                blockType == Material.ACACIA_DOOR || blockType == Material.DARK_OAK_DOOR ||
                blockType == Material.CRIMSON_DOOR || blockType == Material.WARPED_DOOR ||
                blockType == Material.IRON_DOOR || blockType == Material.OAK_TRAPDOOR ||
                blockType == Material.SPRUCE_TRAPDOOR || blockType == Material.BIRCH_TRAPDOOR ||
                blockType == Material.JUNGLE_TRAPDOOR || blockType == Material.ACACIA_TRAPDOOR ||
                blockType == Material.DARK_OAK_TRAPDOOR || blockType == Material.CRIMSON_TRAPDOOR ||
                blockType == Material.WARPED_TRAPDOOR || blockType == Material.IRON_TRAPDOOR ||
                blockType == Material.OAK_BUTTON || blockType == Material.SPRUCE_BUTTON ||
                blockType == Material.BIRCH_BUTTON || blockType == Material.JUNGLE_BUTTON ||
                blockType == Material.ACACIA_BUTTON || blockType == Material.DARK_OAK_BUTTON ||
                blockType == Material.CRIMSON_BUTTON || blockType == Material.WARPED_BUTTON ||
                blockType == Material.STONE_BUTTON || blockType == Material.POLISHED_BLACKSTONE_BUTTON ||
                blockType == Material.OAK_PRESSURE_PLATE || blockType == Material.SPRUCE_PRESSURE_PLATE ||
                blockType == Material.BIRCH_PRESSURE_PLATE || blockType == Material.JUNGLE_PRESSURE_PLATE ||
                blockType == Material.ACACIA_PRESSURE_PLATE || blockType == Material.DARK_OAK_PRESSURE_PLATE ||
                blockType == Material.CRIMSON_PRESSURE_PLATE || blockType == Material.WARPED_PRESSURE_PLATE ||
                blockType == Material.STONE_PRESSURE_PLATE || blockType == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                blockType == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                // Allow door, button, and pressure plate interactions for navigation
                return;
            }
            
            // Block everything else including containers, redstone devices, etc.
            if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
                event.setCancelled(true);
                
                // Provide specific messages for common blocked actions
                if (blockType.name().contains("CHEST") || blockType.name().contains("BARREL") ||
                    blockType.name().contains("SHULKER") || blockType.name().contains("HOPPER")) {
                    sendRestrictionMessage(player, "open containers");
                } else if (blockType.name().contains("FURNACE") || blockType == Material.SMOKER ||
                          blockType == Material.BLAST_FURNACE || blockType == Material.BREWING_STAND) {
                    sendRestrictionMessage(player, "use crafting blocks");
                } else if (blockType == Material.CRAFTING_TABLE || blockType == Material.ENCHANTING_TABLE ||
                          blockType == Material.ANVIL || blockType == Material.CHIPPED_ANVIL ||
                          blockType == Material.DAMAGED_ANVIL) {
                    sendRestrictionMessage(player, "use workstations");
                } else if (blockType.name().contains("BED")) {
                    sendRestrictionMessage(player, "use beds");
                } else if (blockType.name().contains("LEVER") || blockType.name().contains("REDSTONE") ||
                          blockType == Material.REPEATER || blockType == Material.COMPARATOR) {
                    sendRestrictionMessage(player, "interact with redstone");
                } else {
                    sendRestrictionMessage(player, "interact with blocks");
                }
            }
        }
    }

    /**
     * Prevent entity interactions for visitors (including item frames, armor stands, animals, etc.)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        if (isVisitorRestricted(player, "interact_entities")) {
            event.setCancelled(true);
            
            EntityType entityType = event.getRightClicked().getType();
            
            // Provide specific messages based on entity type
            if (entityType == EntityType.ITEM_FRAME || entityType == EntityType.GLOW_ITEM_FRAME) {
                sendRestrictionMessage(player, "modify item frames");
            } else if (entityType == EntityType.ARMOR_STAND) {
                sendRestrictionMessage(player, "interact with armor stands");
            } else if (entityType == EntityType.VILLAGER || entityType == EntityType.WANDERING_TRADER) {
                sendRestrictionMessage(player, "trade with villagers");
            } else if (entityType.name().contains("MINECART") || entityType == EntityType.BOAT ||
                      entityType.name().contains("BOAT")) {
                sendRestrictionMessage(player, "use vehicles");
            } else if (entityType == EntityType.COW || entityType == EntityType.SHEEP ||
                      entityType == EntityType.PIG || entityType == EntityType.CHICKEN ||
                      entityType == EntityType.HORSE || entityType == EntityType.LLAMA) {
                sendRestrictionMessage(player, "interact with animals");
            } else {
                sendRestrictionMessage(player, "interact with entities");
            }
        }
    }

    /**
     * Prevent item pickup for visitors
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            
            if (isVisitorRestricted(player, "pickup_items")) {
                event.setCancelled(true);
                sendRestrictionMessage(player, "pick up items");
            }
        }
    }

    /**
     * Prevent item dropping for visitors
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (isVisitorRestricted(player, "drop_items")) {
            event.setCancelled(true);
            sendRestrictionMessage(player, "drop items");
        }
    }

    /**
     * Prevent inventory opening for visitors (containers)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            
            if (isVisitorRestricted(player, "open_containers")) {
                InventoryType type = event.getInventory().getType();
                
                // Block container access but allow player's own inventory
                if (type != InventoryType.PLAYER && type != InventoryType.CREATIVE) {
                    event.setCancelled(true);
                    
                    // Provide specific messages based on container type
                    switch (type) {
                        case CHEST:
                        case ENDER_CHEST:
                            sendRestrictionMessage(player, "open chests");
                            break;
                        case FURNACE:
                        case BLAST_FURNACE:
                        case SMOKER:
                            sendRestrictionMessage(player, "use furnaces");
                            break;
                        case BREWING:
                            sendRestrictionMessage(player, "use brewing stands");
                            break;
                        case ENCHANTING:
                            sendRestrictionMessage(player, "use enchanting tables");
                            break;
                        case ANVIL:
                            sendRestrictionMessage(player, "use anvils");
                            break;
                        case HOPPER:
                            sendRestrictionMessage(player, "access hoppers");
                            break;
                        case DISPENSER:
                        case DROPPER:
                            sendRestrictionMessage(player, "access dispensers");
                            break;
                        case BARREL:
                            sendRestrictionMessage(player, "open barrels");
                            break;
                        case SHULKER_BOX:
                            sendRestrictionMessage(player, "open shulker boxes");
                            break;
                        default:
                            sendRestrictionMessage(player, "open containers");
                            break;
                    }
                }
            }
        }
    }

    /**
     * Prevent inventory manipulation for visitors in containers
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            
            if (isVisitorRestricted(player, "open_containers")) {
                InventoryType type = event.getInventory().getType();
                
                // Block container interactions but allow player's own inventory
                if (type != InventoryType.PLAYER && type != InventoryType.CREATIVE) {
                    event.setCancelled(true);
                    sendRestrictionMessage(player, "modify containers");
                }
            }
        }
    }

    /**
     * Prevent PVP for visitors
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            
            if (isVisitorRestricted(damager, "pvp")) {
                event.setCancelled(true);
                sendRestrictionMessage(damager, "engage in PVP");
            }
        }
    }

    /**
     * Prevent gamemode changes for visitors (unless by admin)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        
        if (isVisitorRestricted(player)) {
            // Only allow adventure mode for visitors
            if (event.getNewGameMode() != GameMode.ADVENTURE) {
                event.setCancelled(true);
                sendRestrictionMessage(player, "change game mode");
            }
        }
    }
}
