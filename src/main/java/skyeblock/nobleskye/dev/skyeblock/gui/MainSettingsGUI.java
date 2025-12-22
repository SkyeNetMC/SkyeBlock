package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

public class MainSettingsGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final Map<UUID, String> playerIslands;
    
    private static final int INVENTORY_SIZE = 27; // 3 rows
    
    public MainSettingsGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.playerIslands = new HashMap<>();
        
        // Register this as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public Inventory getInventory() {
        return null; // Not used in this implementation
    }
    
    public void openSettingsGUI(Player player, String islandId) {
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null || !island.getIslandId().equals(islandId)) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }
        
        playerIslands.put(player.getUniqueId(), islandId);
        
        Inventory inventory = Bukkit.createInventory(this, INVENTORY_SIZE, 
            Component.text("Island Settings")
                .color(NamedTextColor.DARK_BLUE)
                .decoration(TextDecoration.BOLD, true));
        
        boolean visitingEnabled = plugin.getConfig().getBoolean("island.visiting.enabled", false);
        
        // Check if player has access to any gamerules
        boolean hasGameruleAccess = !plugin.getIslandSettingsManager().getAvailableGameRules(player).isEmpty();
        
        // Visiting Settings (Ender Eye) - Only show if visiting is enabled
        if (visitingEnabled) {
            ItemStack visitingSettings = new ItemStack(Material.ENDER_EYE);
            ItemMeta visitingMeta = visitingSettings.getItemMeta();
            visitingMeta.displayName(Component.text("Visiting Settings")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
            
            List<Component> visitingLore = new ArrayList<>();
            visitingLore.add(Component.empty());
            visitingLore.add(Component.text("Configure island visiting:")
                .color(NamedTextColor.GRAY));
            visitingLore.add(Component.text("• Lock/unlock island")
                .color(NamedTextColor.DARK_GRAY));
            visitingLore.add(Component.text("• Adventure mode settings")
                .color(NamedTextColor.DARK_GRAY));
            visitingLore.add(Component.text("• Set home/visit locations")
                .color(NamedTextColor.DARK_GRAY));
            visitingLore.add(Component.empty());
            visitingLore.add(Component.text("Click to open visiting settings")
                .color(NamedTextColor.GREEN));
            
            visitingMeta.lore(visitingLore);
            visitingSettings.setItemMeta(visitingMeta);
            inventory.setItem(11, visitingSettings);
        }
        
        // Gamerules Settings (Command Block) - Only show if player has gamerule permissions
        if (hasGameruleAccess) {
            ItemStack gameruleSettings = new ItemStack(Material.COMMAND_BLOCK);
            ItemMeta gameruleMeta = gameruleSettings.getItemMeta();
            gameruleMeta.displayName(Component.text("Gamerule Settings")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
            
            List<Component> gameruleLore = new ArrayList<>();
            gameruleLore.add(Component.empty());
            gameruleLore.add(Component.text("Configure island gamerules:")
                .color(NamedTextColor.GRAY));
            gameruleLore.add(Component.text("• Keep inventory settings")
                .color(NamedTextColor.DARK_GRAY));
            gameruleLore.add(Component.text("• Mob spawning controls")
                .color(NamedTextColor.DARK_GRAY));
            gameruleLore.add(Component.text("• World mechanics")
                .color(NamedTextColor.DARK_GRAY));
            gameruleLore.add(Component.empty());
            gameruleLore.add(Component.text("Click to open gamerule settings")
                .color(NamedTextColor.GREEN));
            
            gameruleMeta.lore(gameruleLore);
            gameruleSettings.setItemMeta(gameruleMeta);
            // Position gamerule settings: center if visiting disabled, right if visiting enabled
            inventory.setItem(visitingEnabled ? 15 : 13, gameruleSettings);
        }
        
        // Delete Island button
        ItemStack deleteIsland = new ItemStack(Material.TNT);
        ItemMeta deleteMeta = deleteIsland.getItemMeta();
        deleteMeta.displayName(Component.text("Delete Island")
            .color(NamedTextColor.DARK_RED)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> deleteLore = new ArrayList<>();
        deleteLore.add(Component.empty());
        deleteLore.add(Component.text("⚠ DANGER ZONE ⚠")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true));
        deleteLore.add(Component.empty());
        deleteLore.add(Component.text("Permanently delete your island")
            .color(NamedTextColor.GRAY));
        deleteLore.add(Component.text("This cannot be undone!")
            .color(NamedTextColor.RED));
        deleteLore.add(Component.empty());
        deleteLore.add(Component.text("Click to delete island")
            .color(NamedTextColor.DARK_RED));
        
        deleteMeta.lore(deleteLore);
        deleteIsland.setItemMeta(deleteMeta);
        // Position delete button: right if visiting disabled, far right if visiting enabled
        inventory.setItem(visitingEnabled ? 17 : 15, deleteIsland);
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(closeMeta);
        inventory.setItem(22, close);
        
        // Info item - Position changes based on visiting enabled
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("Island Settings")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.empty());
        infoLore.add(Component.text("Configure your island settings")
            .color(NamedTextColor.GRAY));
        infoLore.add(Component.text("Choose a category to get started")
            .color(NamedTextColor.GRAY));
        
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        inventory.setItem(visitingEnabled ? 13 : 11, info);
        
        player.openInventory(inventory);
        
        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.2f);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof MainSettingsGUI)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        String islandId = playerIslands.get(player.getUniqueId());
        if (islandId == null) return;
        
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        boolean visitingEnabled = plugin.getConfig().getBoolean("island.visiting.enabled", false);
        boolean hasGameruleAccess = !plugin.getIslandSettingsManager().getAvailableGameRules(player).isEmpty();
        
        switch (slot) {
            case 11: // Visiting Settings (only if visiting enabled)
                if (visitingEnabled && clickedItem.getType() == Material.ENDER_EYE) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
                    player.closeInventory();
                    plugin.getVisitingSettingsGUI().openVisitingSettings(player, islandId);
                }
                break;
                
            case 13: // Info item (when visiting enabled) or Gamerule Settings (when visiting disabled)
                if (visitingEnabled) {
                    // Info item - do nothing
                } else if (hasGameruleAccess && clickedItem.getType() == Material.COMMAND_BLOCK) {
                    // Gamerule settings when visiting disabled
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    player.closeInventory();
                    plugin.getIslandSettingsGUI().openSettingsGUI(player, islandId);
                }
                break;
                
            case 15: // Gamerule Settings (when visiting enabled) or Delete Island (when visiting disabled)
                if (visitingEnabled && hasGameruleAccess && clickedItem.getType() == Material.COMMAND_BLOCK) {
                    // Gamerule settings when visiting enabled
                    player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                    player.closeInventory();
                    plugin.getIslandSettingsGUI().openSettingsGUI(player, islandId);
                } else if (!visitingEnabled && clickedItem.getType() == Material.TNT) {
                    // Delete island when visiting disabled
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    player.closeInventory();
                    plugin.getDeleteConfirmationGUI().openDeleteConfirmation(player, player.getUniqueId());
                }
                break;
                
            case 17: // Delete Island (only when visiting enabled)
                if (visitingEnabled && clickedItem.getType() == Material.TNT) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    player.closeInventory();
                    plugin.getDeleteConfirmationGUI().openDeleteConfirmation(player, player.getUniqueId());
                }
                break;
                
            case 22: // Close
                if (clickedItem.getType() == Material.BARRIER) {
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
                    player.closeInventory();
                }
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof MainSettingsGUI) {
            Player player = (Player) event.getPlayer();
            playerIslands.remove(player.getUniqueId());
        }
    }
}
