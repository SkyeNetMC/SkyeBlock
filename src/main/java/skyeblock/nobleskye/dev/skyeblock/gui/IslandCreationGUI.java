package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandCreationGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, String> playerSelections;
    
    private static final int INVENTORY_SIZE = 27; // 3 rows
    
    public IslandCreationGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerSelections = new HashMap<>();
        
        // Register this as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public Inventory getInventory() {
        return null; // Not used in this implementation
    }
    
    public void openCreationGUI(Player player) {
        // Check if player already has an island
        if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You already have an island! Use /island to teleport to it.</red>"));
            return;
        }
        
        playerSelections.remove(player.getUniqueId()); // Clear any previous selection
        
        Inventory inventory = Bukkit.createInventory(this, INVENTORY_SIZE, 
            Component.text("Create Your Island")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
        
        // Get available island types
        String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
        
        // Add island type items
        addIslandTypeItems(inventory, availableTypes);
        
        // Add navigation/control items
        addControlItems(inventory);
        
        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        player.openInventory(inventory);
    }
    
    private void addIslandTypeItems(Inventory inventory, String[] availableTypes) {
        // Define materials and descriptions for each island type
        Map<String, Material> typeMaterials = new HashMap<>();
        Map<String, String> typeDescriptions = new HashMap<>();
        
        // Default mappings
        typeMaterials.put("classic", Material.GRASS_BLOCK);
        typeMaterials.put("desert", Material.SAND);
        typeMaterials.put("nether", Material.NETHERRACK);
        typeMaterials.put("cherry", Material.CHERRY_LOG);
        typeMaterials.put("spruce", Material.SPRUCE_LOG);
        typeMaterials.put("normal", Material.OAK_LOG);
        
        typeDescriptions.put("classic", "The original SkyBlock experience with grass platform");
        typeDescriptions.put("desert", "Sandy island with desert theme");
        typeDescriptions.put("nether", "Challenging nether-themed island");
        typeDescriptions.put("cherry", "Beautiful cherry wood themed island");
        typeDescriptions.put("spruce", "Northern forest themed island");
        typeDescriptions.put("normal", "Standard oak wood themed island");
        
        // Add items for each available type
        int slot = 10; // Start from slot 10 (second row, second column)
        for (String type : availableTypes) {
            if (slot >= 17) break; // Don't exceed the row
            
            Material material = typeMaterials.getOrDefault(type, Material.STONE);
            String description = typeDescriptions.getOrDefault(type, "Custom island type");
            
            ItemStack item = createIslandTypeItem(type, material, description);
            inventory.setItem(slot, item);
            slot++;
        }
    }
    
    private ItemStack createIslandTypeItem(String type, Material material, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Set display name
        String displayName = type.substring(0, 1).toUpperCase() + type.substring(1) + " Island";
        meta.displayName(Component.text(displayName)
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false)
            .decoration(TextDecoration.BOLD, true));
        
        // Set lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text(description)
            .color(NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("Click to select this island type")
            .color(NamedTextColor.GREEN));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void addControlItems(Inventory inventory) {
        // Create button (initially disabled)
        ItemStack createButton = new ItemStack(Material.BARRIER);
        ItemMeta createMeta = createButton.getItemMeta();
        createMeta.displayName(Component.text("Select an Island Type")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        createMeta.lore(List.of(
            Component.empty(),
            Component.text("Choose an island type above")
                .color(NamedTextColor.GRAY),
            Component.text("to enable island creation")
                .color(NamedTextColor.GRAY)
        ));
        createButton.setItemMeta(createMeta);
        inventory.setItem(22, createButton); // Bottom row, center
        
        // Cancel button
        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        cancelMeta.displayName(Component.text("Cancel")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        cancelMeta.lore(List.of(
            Component.empty(),
            Component.text("Close this menu")
                .color(NamedTextColor.GRAY)
        ));
        cancelButton.setItemMeta(cancelMeta);
        inventory.setItem(18, cancelButton); // Bottom row, left
        
        // Info item
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.displayName(Component.text("Island Creation")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        infoMeta.lore(List.of(
            Component.empty(),
            Component.text("Choose your island template")
                .color(NamedTextColor.GRAY),
            Component.text("This cannot be changed later!")
                .color(NamedTextColor.YELLOW),
            Component.empty(),
            Component.text("Each island type has unique")
                .color(NamedTextColor.GRAY),
            Component.text("structures and starting items")
                .color(NamedTextColor.GRAY)
        ));
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(4, infoItem); // Top row, center
    }
    
    private void updateCreateButton(Inventory inventory, String selectedType) {
        ItemStack createButton;
        ItemMeta createMeta;
        
        if (selectedType != null) {
            createButton = new ItemStack(Material.EMERALD);
            createMeta = createButton.getItemMeta();
            createMeta.displayName(Component.text("Create " + selectedType.substring(0, 1).toUpperCase() + selectedType.substring(1) + " Island")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
            createMeta.lore(List.of(
                Component.empty(),
                Component.text("Click to create your island!")
                    .color(NamedTextColor.GREEN),
                Component.empty(),
                Component.text("⚠ This cannot be undone!")
                    .color(NamedTextColor.YELLOW)
            ));
        } else {
            createButton = new ItemStack(Material.BARRIER);
            createMeta = createButton.getItemMeta();
            createMeta.displayName(Component.text("Select an Island Type")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
            createMeta.lore(List.of(
                Component.empty(),
                Component.text("Choose an island type above")
                    .color(NamedTextColor.GRAY),
                Component.text("to enable island creation")
                    .color(NamedTextColor.GRAY)
            ));
        }
        
        createButton.setItemMeta(createMeta);
        inventory.setItem(22, createButton);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof IslandCreationGUI)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        // Handle island type selection (slots 10-16)
        if (slot >= 10 && slot <= 16) {
            handleIslandTypeSelection(player, event.getInventory(), slot);
            return;
        }
        
        // Handle create button (slot 22)
        if (slot == 22) {
            handleCreateButton(player, clickedItem.getType());
            return;
        }
        
        // Handle cancel button (slot 18)
        if (slot == 18) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            player.closeInventory();
            player.sendMessage(miniMessage.deserialize("<yellow>Island creation cancelled.</yellow>"));
            return;
        }
        
        // Info item (slot 4) - do nothing
    }
    
    private void handleIslandTypeSelection(Player player, Inventory inventory, int slot) {
        String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
        int typeIndex = slot - 10;
        
        if (typeIndex >= 0 && typeIndex < availableTypes.length) {
            String selectedType = availableTypes[typeIndex];
            
            // Update player selection
            playerSelections.put(player.getUniqueId(), selectedType);
            
            // Play selection sound
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            
            // Update visual feedback
            updateSelectionVisuals(inventory, slot);
            updateCreateButton(inventory, selectedType);
            
            // Send feedback message
            String displayName = selectedType.substring(0, 1).toUpperCase() + selectedType.substring(1);
            player.sendMessage(miniMessage.deserialize("<green>Selected: " + displayName + " Island</green>"));
        }
    }
    
    private void updateSelectionVisuals(Inventory inventory, int selectedSlot) {
        // Reset all island type items to normal appearance
        for (int slot = 10; slot <= 16; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Remove enchantment glow effect
                    meta.setEnchantmentGlintOverride(false);
                    item.setItemMeta(meta);
                }
            }
        }
        
        // Add glow effect to selected item
        ItemStack selectedItem = inventory.getItem(selectedSlot);
        if (selectedItem != null) {
            ItemMeta meta = selectedItem.getItemMeta();
            if (meta != null) {
                meta.setEnchantmentGlintOverride(true);
                selectedItem.setItemMeta(meta);
            }
        }
    }
    
    private void handleCreateButton(Player player, Material buttonType) {
        if (buttonType == Material.BARRIER) {
            // Button is disabled
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage(miniMessage.deserialize("<red>Please select an island type first!</red>"));
            return;
        }
        
        String selectedType = playerSelections.get(player.getUniqueId());
        if (selectedType == null) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
            player.sendMessage(miniMessage.deserialize("<red>No island type selected!</red>"));
            return;
        }
        
        // Close GUI
        player.closeInventory();
        
        // Play creation sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        // Create the island
        player.sendMessage(miniMessage.deserialize("<yellow>Creating your " + selectedType + " island...</yellow>"));
        
        boolean success = plugin.getIslandManager().createIsland(player, selectedType);
        
        if (success) {
            player.sendMessage(miniMessage.deserialize("<green>✓ Island created successfully!</green>"));
            
            // Teleport player to their new island after a short delay
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getIslandManager().teleportToIsland(player);
                player.sendMessage(miniMessage.deserialize("<aqua>Welcome to your new island!</aqua>"));
            }, 20L); // 1 second delay
        } else {
            player.sendMessage(miniMessage.deserialize("<red>✗ Failed to create island. Please contact an administrator.</red>"));
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof IslandCreationGUI) {
            Player player = (Player) event.getPlayer();
            playerSelections.remove(player.getUniqueId());
        }
    }
}
