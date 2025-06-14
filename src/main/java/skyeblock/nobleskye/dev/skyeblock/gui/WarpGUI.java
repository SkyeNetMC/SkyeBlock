package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Warp;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI for warp selection and teleportation
 */
public class WarpGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private Inventory inventory;
    
    public WarpGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        
        // Register this as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    /**
     * Open the warp GUI for a player
     */
    public void openWarpGUI(Player player) {
        // Get GUI configuration
        String title = plugin.getWarpConfig().getString("gui.title", "Warps");
        int size = plugin.getWarpConfig().getInt("gui.size", 27);
        
        // Create inventory
        inventory = Bukkit.createInventory(this, size, miniMessage.deserialize(title));
        
        // Fill with warps that player has access to
        List<Warp> availableWarps = plugin.getWarpManager().getAvailableWarps(player);
        
        for (Warp warp : availableWarps) {
            if (warp.isEnabled() && warp.getSlot() < size) {
                ItemStack item = createWarpItem(warp, player);
                inventory.setItem(warp.getSlot(), item);
            }
        }
        
        // Add filler items if configured
        fillEmptySlots();
        
        player.openInventory(inventory);
    }
    
    /**
     * Create an item stack for a warp
     */
    private ItemStack createWarpItem(Warp warp, Player player) {
        ItemStack item = new ItemStack(warp.getMaterial());
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set display name
            meta.displayName(miniMessage.deserialize(warp.getDisplayName()));
            
            // Set lore with placeholder processing
            List<String> processedLore = new ArrayList<>();
            for (String loreLine : warp.getLore()) {
                String processed = processLorePlaceholders(loreLine, warp, player);
                processedLore.add(processed);
            }
            
            List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
            for (String line : processedLore) {
                loreComponents.add(miniMessage.deserialize(line));
            }
            meta.lore(loreComponents);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Process placeholders in lore text
     */
    private String processLorePlaceholders(String text, Warp warp, Player player) {
        text = text.replace("{player}", player.getName());
        text = text.replace("{warp_name}", warp.getName());
        text = text.replace("{cost}", String.valueOf(warp.getCost()));
        
        // Process resource world reset times if applicable
        if (warp.getName().equals("nether") || warp.getName().equals("end")) {
            String resetTime = plugin.getResourceWorldManager().getNextResetTime(warp.getName());
            text = text.replace("{reset_time}", resetTime != null ? resetTime : "Unknown");
        }
        
        return text;
    }
    
    /**
     * Fill empty slots with filler items if configured
     */
    private void fillEmptySlots() {
        if (!plugin.getWarpConfig().getBoolean("gui.fill-empty-slots", false)) {
            return;
        }
        
        Material fillerMaterial = Material.valueOf(
            plugin.getWarpConfig().getString("gui.filler-material", "GRAY_STAINED_GLASS_PANE").toUpperCase()
        );
        String fillerName = plugin.getWarpConfig().getString("gui.filler-name", " ");
        
        ItemStack filler = new ItemStack(fillerMaterial);
        ItemMeta meta = filler.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(fillerName));
            filler.setItemMeta(meta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        if (!event.getInventory().equals(inventory)) {
            return;
        }
        
        event.setCancelled(true);
        
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Find the warp by slot
        int slot = event.getSlot();
        Warp clickedWarp = null;
        
        for (Warp warp : plugin.getWarpManager().getAvailableWarps(player)) {
            if (warp.getSlot() == slot) {
                clickedWarp = warp;
                break;
            }
        }
        
        if (clickedWarp == null) {
            return;
        }
        
        // Close inventory and teleport
        player.closeInventory();
        plugin.getWarpManager().warpPlayer(player, clickedWarp.getName());
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
