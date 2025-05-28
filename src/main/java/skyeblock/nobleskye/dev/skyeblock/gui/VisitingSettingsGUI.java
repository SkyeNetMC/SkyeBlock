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
    
    private static final int INVENTORY_SIZE = 45; // 5 rows
    
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
        
        // Adventure Mode Toggle
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
            adventureLore.add(Component.text("Visitors cannot:")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("• Break/place blocks")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.text("• Open chests/inventories")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.text("• Pick up/drop items")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.empty());
            adventureLore.add(Component.text("Click to disable adventure mode")
                .color(NamedTextColor.YELLOW));
        } else {
            adventureLore.add(Component.text("Visitors have full access")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("Click to enable adventure mode protection")
                .color(NamedTextColor.GREEN));
        }
        
        adventureMeta.lore(adventureLore);
        adventureToggle.setItemMeta(adventureMeta);
        inventory.setItem(13, adventureToggle);
        
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
        inventory.setItem(29, setVisit);
        
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
        inventory.setItem(31, resetLocations);
        
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.displayName(Component.text("Back to Settings")
            .color(NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(backMeta);
        inventory.setItem(36, back);
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(closeMeta);
        inventory.setItem(44, close);
        
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
            adventureLore.add(Component.text("Visitors cannot:")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("• Break/place blocks")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.text("• Open chests/inventories")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.text("• Pick up/drop items")
                .color(NamedTextColor.DARK_GRAY));
            adventureLore.add(Component.empty());
            adventureLore.add(Component.text("Click to disable adventure mode")
                .color(NamedTextColor.YELLOW));
        } else {
            adventureLore.add(Component.text("Visitors have full access")
                .color(NamedTextColor.GRAY));
            adventureLore.add(Component.text("Click to enable adventure mode protection")
                .color(NamedTextColor.GREEN));
        }
        
        adventureMeta.lore(adventureLore);
        adventureToggle.setItemMeta(adventureMeta);
        currentInventory.setItem(13, adventureToggle);
        
        // Update the player's view
        player.updateInventory();
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
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize(
                    island.isAdventureModeForVisitors() ? "<green>Adventure mode enabled for visitors.</green>" 
                                                       : "<yellow>Adventure mode disabled for visitors.</yellow>"));
                // Schedule refresh for next tick to avoid inventory conflicts
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() instanceof VisitingSettingsGUI) {
                        refreshVisitingSettings(player, islandId);
                    }
                }, 1L);
                break;
                
            case 15: // Set home location
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setHomeLocation(player.getLocation());
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Home location set to your current position!</green>"));
                break;
                
            case 29: // Set visit location
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setVisitLocation(player.getLocation());
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Visit location set to your current position!</green>"));
                break;
                
            case 31: // Reset locations
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                island.setHomeLocation(null);
                island.setVisitLocation(null);
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<yellow>Home and visit locations reset to island spawn!</yellow>"));
                break;
                
            case 36: // Back
                player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
                player.closeInventory();
                plugin.getMainSettingsGUI().openSettingsGUI(player, islandId);
                break;
                
            case 44: // Close
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
