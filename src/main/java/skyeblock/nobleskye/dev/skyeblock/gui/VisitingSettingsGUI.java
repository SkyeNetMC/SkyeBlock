package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class VisitingSettingsGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, String> playerIslands;
    
    private static final int INVENTORY_SIZE = 54; // 6 rows for more granular controls
    
    public VisitingSettingsGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerIslands = new HashMap<>();
        
        // Register this as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public Inventory getInventory() {
        return null; // Not used in this implementation
    }
    
    public void openVisitingSettings(Player player, String islandId) {
        // Check if visiting is enabled
        if (!plugin.getConfig().getBoolean("island.visiting.enabled", false)) {
            plugin.sendMessage(player, "visiting-disabled");
            return;
        }
        
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null || !island.getIslandId().equals(islandId)) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }
        
        playerIslands.put(player.getUniqueId(), islandId);
        
        Inventory inventory = Bukkit.createInventory(this, INVENTORY_SIZE, 
            Component.text("Visiting Settings")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.BOLD, true));
        
        // Lock/Unlock Island
        ItemStack lockToggle = new ItemStack(island.isLocked() ? Material.IRON_DOOR : Material.OAK_DOOR);
        ItemMeta lockMeta = lockToggle.getItemMeta();
        lockMeta.displayName(Component.text(island.isLocked() ? "Unlock Island" : "Lock Island")
            .color(island.isLocked() ? NamedTextColor.GREEN : NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lockLore = new ArrayList<>();
        lockLore.add(Component.empty());
        lockLore.add(Component.text("Current status: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(island.isLocked() ? "LOCKED" : "UNLOCKED")
                .color(island.isLocked() ? NamedTextColor.RED : NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)));
        lockLore.add(Component.empty());
        if (island.isLocked()) {
            lockLore.add(Component.text("Visitors cannot teleport to your island")
                .color(NamedTextColor.GRAY));
            lockLore.add(Component.text("Click to unlock and allow visitors")
                .color(NamedTextColor.GREEN));
        } else {
            lockLore.add(Component.text("Visitors can teleport to your island")
                .color(NamedTextColor.GRAY));
            lockLore.add(Component.text("Click to lock and prevent visitors")
                .color(NamedTextColor.RED));
        }
        
        lockMeta.lore(lockLore);
        lockToggle.setItemMeta(lockMeta);
        inventory.setItem(11, lockToggle);
        
        // Adventure Mode Toggle (Master Toggle)
        ItemStack adventureToggle = new ItemStack(island.isAdventureModeForVisitors() ? Material.WOODEN_SWORD : Material.DIAMOND_SWORD);
        ItemMeta adventureMeta = adventureToggle.getItemMeta();
        adventureMeta.displayName(Component.text("Adventure Mode for Visitors")
            .color(NamedTextColor.BLUE)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> adventureLore = new ArrayList<>();
        adventureLore.add(Component.empty());
        adventureLore.add(Component.text("Current status: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(island.isAdventureModeForVisitors() ? "ENABLED" : "DISABLED")
                .color(island.isAdventureModeForVisitors() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)));
        adventureLore.add(Component.empty());
        if (island.isAdventureModeForVisitors()) {
            adventureLore.add(Component.text("Master protection enabled")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("Configure individual permissions below")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.empty());
            adventureLore.add(Component.text("Click to disable all protections")
                .color(NamedTextColor.YELLOW));
        } else {
            adventureLore.add(Component.text("All protections are disabled")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("Click to enable protection system")
                .color(NamedTextColor.GREEN));
        }
        
        adventureMeta.lore(adventureLore);
        adventureToggle.setItemMeta(adventureMeta);
        inventory.setItem(13, adventureToggle);
        
        // Only show granular controls when adventure mode is enabled
        if (island.isAdventureModeForVisitors()) {
            // Block Breaking Permission
            addPermissionToggle(inventory, 19, Material.IRON_PICKAXE, "Break Blocks", 
                island.canVisitorBreakBlocks(), NamedTextColor.RED);
            
            // Block Placing Permission  
            addPermissionToggle(inventory, 20, Material.GRASS_BLOCK, "Place Blocks",
                island.canVisitorPlaceBlocks(), NamedTextColor.GREEN);
            
            // Container Access Permission
            addPermissionToggle(inventory, 21, Material.CHEST, "Open Containers",
                island.canVisitorOpenContainers(), NamedTextColor.GOLD);
            
            // Item Pickup Permission
            addPermissionToggle(inventory, 22, Material.GOLDEN_APPLE, "Pick Up Items",
                island.canVisitorPickupItems(), NamedTextColor.YELLOW);
            
            // Item Drop Permission
            addPermissionToggle(inventory, 23, Material.DROPPER, "Drop Items",
                island.canVisitorDropItems(), NamedTextColor.AQUA);
            
            // Entity Interaction Permission
            addPermissionToggle(inventory, 28, Material.LEAD, "Interact with Entities",
                island.canVisitorInteractWithEntities(), NamedTextColor.LIGHT_PURPLE);
            
            // PVP Permission
            addPermissionToggle(inventory, 29, Material.DIAMOND_SWORD, "PVP",
                island.canVisitorUsePvp(), NamedTextColor.DARK_RED);
            
            // Redstone Permission
            addPermissionToggle(inventory, 30, Material.REDSTONE, "Use Redstone",
                island.canVisitorUseRedstone(), NamedTextColor.BLUE);
        }
        
        // Set Home Location
        ItemStack setHome = new ItemStack(Material.RED_BED);
        ItemMeta homeMeta = setHome.getItemMeta();
        homeMeta.displayName(Component.text("Set Home Location")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> homeLore = new ArrayList<>();
        homeLore.add(Component.empty());
        homeLore.add(Component.text("Set where /is home teleports you")
            .color(NamedTextColor.GRAY));
        homeLore.add(Component.text("Uses your current location")
            .color(NamedTextColor.GRAY));
        homeLore.add(Component.empty());
        homeLore.add(Component.text("Click to set home location")
            .color(NamedTextColor.GREEN));
        
        homeMeta.lore(homeLore);
        setHome.setItemMeta(homeMeta);
        inventory.setItem(15, setHome);
        
        // Set Visit Location
        ItemStack setVisit = new ItemStack(Material.ENDER_PEARL);
        ItemMeta visitMeta = setVisit.getItemMeta();
        visitMeta.displayName(Component.text("Set Visit Location")
            .color(NamedTextColor.AQUA)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> visitLore = new ArrayList<>();
        visitLore.add(Component.empty());
        visitLore.add(Component.text("Set where visitors teleport to")
            .color(NamedTextColor.GRAY));
        visitLore.add(Component.text("Uses your current location")
            .color(NamedTextColor.GRAY));
        visitLore.add(Component.empty());
        visitLore.add(Component.text("Click to set visit location")
            .color(NamedTextColor.GREEN));
        
        visitMeta.lore(visitLore);
        setVisit.setItemMeta(visitMeta);
        inventory.setItem(37, setVisit);
        
        // Reset Locations
        ItemStack resetLocations = new ItemStack(Material.COMPASS);
        ItemMeta resetMeta = resetLocations.getItemMeta();
        resetMeta.displayName(Component.text("Reset Locations")
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> resetLore = new ArrayList<>();
        resetLore.add(Component.empty());
        resetLore.add(Component.text("Reset home and visit locations")
            .color(NamedTextColor.GRAY));
        resetLore.add(Component.text("to the island spawn point")
            .color(NamedTextColor.GRAY));
        resetLore.add(Component.empty());
        resetLore.add(Component.text("Click to reset locations")
            .color(NamedTextColor.YELLOW));
        
        resetMeta.lore(resetLore);
        resetLocations.setItemMeta(resetMeta);
        inventory.setItem(39, resetLocations);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back to Settings")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        inventory.setItem(45, back);
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(closeMeta);
        inventory.setItem(53, close);
        
        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        player.openInventory(inventory);
    }
    
    private void refreshVisitingSettings(Player player, String islandId) {
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        if (currentInventory == null || !(currentInventory.getHolder() instanceof VisitingSettingsGUI)) {
            return;
        }
        
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) return;
        
        // Update Lock/Unlock Island item (slot 11)
        ItemStack lockToggle = new ItemStack(island.isLocked() ? Material.IRON_DOOR : Material.OAK_DOOR);
        ItemMeta lockMeta = lockToggle.getItemMeta();
        lockMeta.displayName(Component.text(island.isLocked() ? "Unlock Island" : "Lock Island")
            .color(island.isLocked() ? NamedTextColor.GREEN : NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lockLore = new ArrayList<>();
        lockLore.add(Component.empty());
        lockLore.add(Component.text("Current status: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(island.isLocked() ? "LOCKED" : "UNLOCKED")
                .color(island.isLocked() ? NamedTextColor.RED : NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true)));
        lockLore.add(Component.empty());
        if (island.isLocked()) {
            lockLore.add(Component.text("Visitors cannot teleport to your island")
                .color(NamedTextColor.GRAY));
            lockLore.add(Component.text("Click to unlock and allow visitors")
                .color(NamedTextColor.GREEN));
        } else {
            lockLore.add(Component.text("Visitors can teleport to your island")
                .color(NamedTextColor.GRAY));
            lockLore.add(Component.text("Click to lock and prevent visitors")
                .color(NamedTextColor.RED));
        }
        
        lockMeta.lore(lockLore);
        lockToggle.setItemMeta(lockMeta);
        currentInventory.setItem(11, lockToggle);
        
        // Update Adventure Mode Toggle item (slot 13)
        ItemStack adventureToggle = new ItemStack(island.isAdventureModeForVisitors() ? Material.WOODEN_SWORD : Material.DIAMOND_SWORD);
        ItemMeta adventureMeta = adventureToggle.getItemMeta();
        adventureMeta.displayName(Component.text("Adventure Mode for Visitors")
            .color(NamedTextColor.BLUE)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> adventureLore = new ArrayList<>();
        adventureLore.add(Component.empty());
        adventureLore.add(Component.text("Current status: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(island.isAdventureModeForVisitors() ? "ENABLED" : "DISABLED")
                .color(island.isAdventureModeForVisitors() ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)));
        adventureLore.add(Component.empty());
        if (island.isAdventureModeForVisitors()) {
            adventureLore.add(Component.text("Master protection enabled")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("Configure individual permissions below")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.empty());
            adventureLore.add(Component.text("Click to disable all protections")
                .color(NamedTextColor.YELLOW));
        } else {
            adventureLore.add(Component.text("All protections are disabled")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("Click to enable protection system")
                .color(NamedTextColor.GREEN));
        }
        
        adventureMeta.lore(adventureLore);
        adventureToggle.setItemMeta(adventureMeta);
        currentInventory.setItem(13, adventureToggle);
        
        // Clear granular controls first
        for (int slot : new int[]{19, 20, 21, 22, 23, 28, 29, 30}) {
            currentInventory.setItem(slot, null);
        }
        
        // Update granular controls if adventure mode is enabled
        if (island.isAdventureModeForVisitors()) {
            addPermissionToggle(currentInventory, 19, Material.IRON_PICKAXE, "Break Blocks", 
                island.canVisitorBreakBlocks(), NamedTextColor.RED);
            addPermissionToggle(currentInventory, 20, Material.GRASS_BLOCK, "Place Blocks",
                island.canVisitorPlaceBlocks(), NamedTextColor.GREEN);
            addPermissionToggle(currentInventory, 21, Material.CHEST, "Open Containers",
                island.canVisitorOpenContainers(), NamedTextColor.GOLD);
            addPermissionToggle(currentInventory, 22, Material.GOLDEN_APPLE, "Pick Up Items",
                island.canVisitorPickupItems(), NamedTextColor.YELLOW);
            addPermissionToggle(currentInventory, 23, Material.DROPPER, "Drop Items",
                island.canVisitorDropItems(), NamedTextColor.AQUA);
            addPermissionToggle(currentInventory, 28, Material.LEAD, "Interact with Entities",
                island.canVisitorInteractWithEntities(), NamedTextColor.LIGHT_PURPLE);
            addPermissionToggle(currentInventory, 29, Material.DIAMOND_SWORD, "PVP",
                island.canVisitorUsePvp(), NamedTextColor.DARK_RED);
            addPermissionToggle(currentInventory, 30, Material.REDSTONE, "Use Redstone",
                island.canVisitorUseRedstone(), NamedTextColor.BLUE);
        }
        
        // Update the player's view
        player.updateInventory();
    }

    private void addPermissionToggle(Inventory inventory, int slot, Material material, String permissionName, boolean isEnabled, NamedTextColor color) {
        ItemStack item = new ItemStack(isEnabled ? material : Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text("Visitor " + permissionName)
            .color(color)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Status: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(isEnabled ? "ALLOWED" : "DENIED")
                .color(isEnabled ? NamedTextColor.GREEN : NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)));
        lore.add(Component.empty());
        
        if (isEnabled) {
            lore.add(Component.text("Visitors can " + permissionName.toLowerCase())
                .color(NamedTextColor.GRAY));
            lore.add(Component.text("Click to deny this permission")
                .color(NamedTextColor.RED));
        } else {
            lore.add(Component.text("Visitors cannot " + permissionName.toLowerCase())
                .color(NamedTextColor.GRAY));
            lore.add(Component.text("Click to allow this permission")
                .color(NamedTextColor.GREEN));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        inventory.setItem(slot, item);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof VisitingSettingsGUI)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        String islandId = playerIslands.get(player.getUniqueId());
        if (islandId == null) return;
        
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) return;
        
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        switch (slot) {
            case 11: // Lock/Unlock toggle
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setLocked(!island.isLocked());
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize(
                    island.isLocked() ? "<red>Island locked! Visitors can no longer teleport here.</red>" 
                                     : "<green>Island unlocked! Visitors can now teleport here.</green>"));
                // Refresh the GUI immediately
                refreshVisitingSettings(player, islandId);
                break;
                
            case 13: // Adventure mode toggle
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setAdventureModeForVisitors(!island.isAdventureModeForVisitors());
                
                // When enabling adventure mode, set all permissions to false for security
                if (island.isAdventureModeForVisitors()) {
                    island.setVisitorCanBreakBlocks(false);
                    island.setVisitorCanPlaceBlocks(false);
                    island.setVisitorCanOpenContainers(false);
                    island.setVisitorCanPickupItems(false);
                    island.setVisitorCanDropItems(false);
                    island.setVisitorCanInteractWithEntities(false);
                    island.setVisitorCanUsePvp(false);
                    island.setVisitorCanUseRedstone(false);
                }
                
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize(
                    island.isAdventureModeForVisitors() ? "<green>Adventure mode enabled for visitors. Configure individual permissions below.</green>" 
                                                       : "<yellow>Adventure mode disabled for visitors.</yellow>"));
                // Schedule refresh for next tick to avoid inventory conflicts
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() instanceof VisitingSettingsGUI) {
                        refreshVisitingSettings(player, islandId);
                    }
                }, 1L);
                break;
                
            // Granular permission toggles
            case 19: // Break blocks
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanBreakBlocks(!island.canVisitorBreakBlocks());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorBreakBlocks() ? "<green>Visitors can now break blocks.</green>" 
                                                      : "<red>Visitors can no longer break blocks.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 20: // Place blocks
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanPlaceBlocks(!island.canVisitorPlaceBlocks());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorPlaceBlocks() ? "<green>Visitors can now place blocks.</green>" 
                                                      : "<red>Visitors can no longer place blocks.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 21: // Open containers
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanOpenContainers(!island.canVisitorOpenContainers());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorOpenContainers() ? "<green>Visitors can now open containers.</green>" 
                                                         : "<red>Visitors can no longer open containers.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 22: // Pick up items
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanPickupItems(!island.canVisitorPickupItems());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorPickupItems() ? "<green>Visitors can now pick up items.</green>" 
                                                       : "<red>Visitors can no longer pick up items.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 23: // Drop items
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanDropItems(!island.canVisitorDropItems());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorDropItems() ? "<green>Visitors can now drop items.</green>" 
                                                     : "<red>Visitors can no longer drop items.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 28: // Interact with entities
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanInteractWithEntities(!island.canVisitorInteractWithEntities());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorInteractWithEntities() ? "<green>Visitors can now interact with entities.</green>" 
                                                               : "<red>Visitors can no longer interact with entities.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 29: // PVP
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanUsePvp(!island.canVisitorUsePvp());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorUsePvp() ? "<green>Visitors can now use PVP.</green>" 
                                                 : "<red>Visitors can no longer use PVP.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 30: // Use redstone
                if (island.isAdventureModeForVisitors()) {
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    island.setVisitorCanUseRedstone(!island.canVisitorUseRedstone());
                    plugin.getIslandManager().saveIsland(island);
                    player.sendMessage(miniMessage.deserialize(
                        island.canVisitorUseRedstone() ? "<green>Visitors can now use redstone.</green>" 
                                                      : "<red>Visitors can no longer use redstone.</red>"));
                    refreshVisitingSettings(player, islandId);
                }
                break;
                
            case 15: // Set home location
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setHomeLocation(player.getLocation());
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Home location set to your current position!</green>"));
                break;
                
            case 37: // Set visit location
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setVisitLocation(player.getLocation());
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Visit location set to your current position!</green>"));
                break;
                
            case 39: // Reset locations
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setHomeLocation(null);
                island.setVisitLocation(null);
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<yellow>Home and visit locations reset to island spawn!</yellow>"));
                break;
                
            case 45: // Back
                player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
                player.closeInventory();
                plugin.getMainSettingsGUI().openSettingsGUI(player, islandId);
                break;
                
            case 53: // Close
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
                player.closeInventory();
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof VisitingSettingsGUI) {
            Player player = (Player) event.getPlayer();
            playerIslands.remove(player.getUniqueId());
        }
    }
}
