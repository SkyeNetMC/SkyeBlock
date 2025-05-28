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

public class DeleteConfirmationGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, DeleteSession> playerSessions;
    
    private static final int INVENTORY_SIZE = 54; // 6 rows
    private static final int REQUIRED_CLICKS = 3;
    
    public DeleteConfirmationGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerSessions = new HashMap<>();
        
        // Register this as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public Inventory getInventory() {
        return null; // Not used in this implementation
    }
    
    public void openDeleteConfirmation(Player player, UUID targetPlayerUUID) {
        // Check permissions
        boolean isAdmin = player.hasPermission("skyeblock.admin");
        boolean isOwner = player.getUniqueId().equals(targetPlayerUUID);
        
        if (!isAdmin && !isOwner) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to delete this island!</red>"));
            return;
        }
        
        Island island = plugin.getIslandManager().getIsland(targetPlayerUUID);
        if (island == null) {
            player.sendMessage(miniMessage.deserialize("<red>Island not found!</red>"));
            return;
        }
        
        // Create new session
        DeleteSession session = new DeleteSession(targetPlayerUUID, isAdmin);
        playerSessions.put(player.getUniqueId(), session);
        
        createConfirmationInventory(player, session);
    }
    
    private void createConfirmationInventory(Player player, DeleteSession session) {
        Inventory inventory = Bukkit.createInventory(this, INVENTORY_SIZE, 
            Component.text("DELETE ISLAND - CLICK " + (REQUIRED_CLICKS - session.clickCount) + " MORE TIMES")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.BOLD, true));
        
        // Fill with glass panes
        ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta grayMeta = grayGlass.getItemMeta();
        grayMeta.displayName(Component.empty());
        grayGlass.setItemMeta(grayMeta);
        
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, grayGlass);
        }
        
        // Generate random slot for confirmation button
        session.generateNewConfirmationSlot();
        
        // Create confirmation button
        ItemStack confirmButton = new ItemStack(Material.TNT);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("CLICK TO DELETE ISLAND")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> confirmLore = new ArrayList<>();
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("⚠ WARNING: THIS CANNOT BE UNDONE! ⚠")
            .color(NamedTextColor.DARK_RED)
            .decoration(TextDecoration.BOLD, true));
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("Clicks remaining: " + (REQUIRED_CLICKS - session.clickCount))
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.BOLD, true));
        confirmLore.add(Component.empty());
        if (session.isAdminAction) {
            confirmLore.add(Component.text("Admin Override: Deleting another player's island")
                .color(NamedTextColor.GOLD));
            confirmLore.add(Component.empty());
        }
        confirmLore.add(Component.text("This will permanently delete:")
            .color(NamedTextColor.RED));
        confirmLore.add(Component.text("• All blocks and builds")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.text("• All items and inventories")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.text("• All island settings")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.text("• All coop members")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("Click " + (REQUIRED_CLICKS - session.clickCount) + " more time(s) to confirm")
            .color(NamedTextColor.YELLOW));
        
        confirmMeta.lore(confirmLore);
        confirmButton.setItemMeta(confirmMeta);
        inventory.setItem(session.confirmationSlot, confirmButton);
        
        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.BARRIER);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("CANCEL")
            .color(NamedTextColor.GREEN)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> cancelLore = new ArrayList<>();
        cancelLore.add(Component.empty());
        cancelLore.add(Component.text("Click to cancel island deletion")
            .color(NamedTextColor.GREEN));
        cancelLore.add(Component.text("and return to safety")
            .color(NamedTextColor.GREEN));
        
        cancelMeta.lore(cancelLore);
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(49, cancelButton); // Bottom right area
        
        player.openInventory(inventory);
        
        // Play warning sound
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
    }
    
    private void refreshConfirmationInventory(Player player, DeleteSession session) {
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        if (currentInventory == null || !(currentInventory.getHolder() instanceof DeleteConfirmationGUI)) {
            return;
        }
        
        // Update the title by creating a new inventory (unfortunately Bukkit doesn't allow title updates)
        // But first clear the old confirmation button
        currentInventory.setItem(session.confirmationSlot, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        
        // Generate new random slot for confirmation button
        session.generateNewConfirmationSlot();
        
        // Create updated confirmation button
        ItemStack confirmButton = new ItemStack(Material.TNT);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        confirmMeta.displayName(Component.text("CLICK TO DELETE ISLAND")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> confirmLore = new ArrayList<>();
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("⚠ WARNING: THIS CANNOT BE UNDONE! ⚠")
            .color(NamedTextColor.DARK_RED)
            .decoration(TextDecoration.BOLD, true));
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("Clicks remaining: " + (REQUIRED_CLICKS - session.clickCount))
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.BOLD, true));
        confirmLore.add(Component.empty());
        if (session.isAdminAction) {
            confirmLore.add(Component.text("Admin Override: Deleting another player's island")
                .color(NamedTextColor.GOLD));
            confirmLore.add(Component.empty());
        }
        confirmLore.add(Component.text("This will permanently delete:")
            .color(NamedTextColor.RED));
        confirmLore.add(Component.text("• All blocks and builds")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.text("• All items and inventories")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.text("• All island settings")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.text("• All coop members")
            .color(NamedTextColor.DARK_RED));
        confirmLore.add(Component.empty());
        confirmLore.add(Component.text("Click " + (REQUIRED_CLICKS - session.clickCount) + " more time(s) to confirm")
            .color(NamedTextColor.YELLOW));
        
        confirmMeta.lore(confirmLore);
        confirmButton.setItemMeta(confirmMeta);
        currentInventory.setItem(session.confirmationSlot, confirmButton);
        
        // Fill the old slot with gray glass if it's not the cancel button slot
        if (session.confirmationSlot != 49) {
            ItemStack grayGlass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta grayMeta = grayGlass.getItemMeta();
            grayMeta.displayName(Component.empty());
            grayGlass.setItemMeta(grayMeta);
            
            // Clear all slots except cancel button (slot 49) and fill with gray glass
            for (int i = 0; i < INVENTORY_SIZE; i++) {
                if (i != 49 && i != session.confirmationSlot) {
                    currentInventory.setItem(i, grayGlass);
                }
            }
        }
        
        // Force inventory update
        player.updateInventory();
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof DeleteConfirmationGUI)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        DeleteSession session = playerSessions.get(player.getUniqueId());
        if (session == null) return;
        
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        if (slot == 49 && clickedItem.getType() == Material.BARRIER) {
            // Cancel button clicked
            player.closeInventory();
            player.sendMessage(miniMessage.deserialize("<green>Island deletion cancelled.</green>"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
            return;
        }
        
        if (slot == session.confirmationSlot && clickedItem.getType() == Material.TNT) {
            // Confirmation button clicked
            session.clickCount++;
            
            if (session.clickCount >= REQUIRED_CLICKS) {
                // Final confirmation reached - delete the island
                Island island = plugin.getIslandManager().getIsland(session.targetPlayerUUID);
                if (island != null) {
                    boolean success = plugin.getIslandManager().deleteIsland(session.targetPlayerUUID);
                    player.closeInventory();
                    
                    if (success) {
                        player.sendMessage(miniMessage.deserialize("<red>Island successfully deleted!</red>"));
                        
                        // Notify the island owner if admin deleted it
                        if (session.isAdminAction) {
                            Player targetPlayer = Bukkit.getPlayer(session.targetPlayerUUID);
                            if (targetPlayer != null) {
                                targetPlayer.sendMessage(miniMessage.deserialize(
                                    "<red>Your island has been deleted by an administrator: " + player.getName() + "</red>"));
                                // Teleport them to hub if they're on their island
                                if (targetPlayer.getWorld().getName().equals(island.getIslandId())) {
                                    plugin.getWorldManager().teleportToHub(targetPlayer);
                                }
                            }
                        }
                        
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 0.8f);
                    } else {
                        player.sendMessage(miniMessage.deserialize("<red>Failed to delete island!</red>"));
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
                    }
                } else {
                    player.closeInventory();
                    player.sendMessage(miniMessage.deserialize("<red>Island not found!</red>"));
                }
            } else {
                // Update GUI with new click count and random position
                refreshConfirmationInventory(player, session);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.5f);
            }
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof DeleteConfirmationGUI) {
            Player player = (Player) event.getPlayer();
            playerSessions.remove(player.getUniqueId());
        }
    }
    
    private static class DeleteSession {
        private final UUID targetPlayerUUID;
        private final boolean isAdminAction;
        private int clickCount;
        private int confirmationSlot;
        private final Random random;
        
        public DeleteSession(UUID targetPlayerUUID, boolean isAdminAction) {
            this.targetPlayerUUID = targetPlayerUUID;
            this.isAdminAction = isAdminAction;
            this.clickCount = 0;
            this.random = new Random();
            generateNewConfirmationSlot();
        }
        
        public void generateNewConfirmationSlot() {
            // Generate random slot, avoiding the cancel button slot (49) and edges
            List<Integer> availableSlots = new ArrayList<>();
            for (int i = 10; i < 44; i++) {
                if (i % 9 != 0 && i % 9 != 8 && i != 49) { // Avoid edges and cancel button
                    availableSlots.add(i);
                }
            }
            
            if (!availableSlots.isEmpty()) {
                this.confirmationSlot = availableSlots.get(random.nextInt(availableSlots.size()));
            } else {
                this.confirmationSlot = 22; // Fallback to center
            }
        }
    }
}
