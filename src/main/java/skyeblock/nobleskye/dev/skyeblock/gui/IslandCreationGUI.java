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
    
    private static final int INVENTORY_SIZE = 54; // 6 rows for navigation
    
    // Island types organized by difficulty: Easy (7) → Medium (5) → Hard (7) = 19 total
    private static final IslandType[] ISLAND_TYPES = {
        // Easy Islands (Green) - 7 total
        new IslandType("vanilla", "The classic Skyblock experience", Material.GRASS_BLOCK, Difficulty.EASY),
        new IslandType("beginner", "Perfect starting island for new players", Material.OAK_LEAVES, Difficulty.EASY),
        new IslandType("Mossy Cavern", "Underground cave with moss and nature", Material.MOSS_BLOCK, Difficulty.EASY),
        new IslandType("Farmers Dream", "Agricultural paradise for farming", Material.FARMLAND, Difficulty.EASY),
        new IslandType("Mineshaft", "Old mining operation with resources", Material.RAIL, Difficulty.EASY),
        new IslandType("Cozy Grove", "Peaceful forest retreat", Material.OAK_LOG, Difficulty.EASY),
        new IslandType("Bare Bones", "Minimal starting resources", Material.BONE, Difficulty.EASY),

        // Medium Islands (Yellow) - 5 total
        new IslandType("Campsite", "Outdoor adventure base camp", Material.CAMPFIRE, Difficulty.MEDIUM),
        new IslandType("Fishermans Paradise", "Perfect for aquatic adventures", Material.FISHING_ROD, Difficulty.MEDIUM),
        new IslandType("Inverted", "Upside-down challenge island", Material.CRYING_OBSIDIAN, Difficulty.MEDIUM),
        new IslandType("Grid Map", "Organized grid-based layout", Material.MAP, Difficulty.MEDIUM),
        new IslandType("2010", "Retro minecraft experience", Material.COBBLESTONE, Difficulty.MEDIUM),
        
        // Hard Islands (Red) - 7 total
        new IslandType("Advanced", "Expert-level skyblock challenge", Material.DIAMOND_BLOCK, Difficulty.HARD),
        new IslandType("Igloo", "Frozen wasteland survival", Material.ICE, Difficulty.HARD),
        new IslandType("Nether Jail", "Trapped in the nether dimension", Material.NETHER_BRICKS, Difficulty.HARD),
        new IslandType("Desert", "Harsh desert survival challenge", Material.SAND, Difficulty.HARD),
        new IslandType("Wilson", "Stranded survival experience", Material.PUMPKIN, Difficulty.HARD),
        new IslandType("Olympus", "Divine mountain peak challenge", Material.QUARTZ_BLOCK, Difficulty.HARD),
        new IslandType("Sandy Isle", "Desert island with limited resources", Material.RED_SAND, Difficulty.HARD)
    };
    
    // Difficulty enum for organization
    private enum Difficulty {
        EASY, MEDIUM, HARD
    }
    
    // Island type data class
    private static class IslandType {
        final String name;
        final String description;
        final Material material;
        final Difficulty difficulty;
        
        IslandType(String name, String description, Material material, Difficulty difficulty) {
            this.name = name;
            this.description = description;
            this.material = material;
            this.difficulty = difficulty;
        }
    }
    
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

        // Add island type items using our predefined types
        addIslandTypeItems(inventory);
        
        // Add navigation/control items
        addControlItems(inventory);
        
        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        player.openInventory(inventory);
    }
    
    private void addIslandTypeItems(Inventory inventory) {
        // Define slot positions for 19 islands: 7 Easy + 5 Medium + 7 Hard
        int[] slots = {
            // Row 1: Easy islands (5 total) - slots 11-15
            11, 12, 13, 14, 15, 
            // Row 2: Easy + Medium islands (5 total) - slots 20-24 (2 easy + 3 medium)
            20, 21, 22, 23, 24,
            // Row 3: Medium + Hard islands (5 total) - slots 29-33 (2 medium + 3 hard)
            29, 30, 31, 32, 33,
            // Row 4: Hard islands (4 total) - slots 38-41
            38, 39, 40, 41,
        };
        
        // Create items for each island type
        for (int i = 0; i < Math.min(ISLAND_TYPES.length, slots.length); i++) {
            IslandType islandType = ISLAND_TYPES[i];
            ItemStack item = createIslandTypeItem(islandType);
            inventory.setItem(slots[i], item);
        }
    }
    
    private ItemStack createIslandTypeItem(IslandType islandType) {
        ItemStack item = new ItemStack(islandType.material);
        ItemMeta meta = item.getItemMeta();
        
        // Determine color based on difficulty
        String nameColor = switch (islandType.difficulty) {
            case EASY -> "<green>";
            case MEDIUM -> "<yellow>";
            case HARD -> "<red>";
        };
        
        String difficultyText = switch (islandType.difficulty) {
            case EASY -> "<green>Easy";
            case MEDIUM -> "<yellow>Medium";
            case HARD -> "<red>Hard";
        };
        
        // Set display name with appropriate color
        Component displayName = miniMessage.deserialize(nameColor + islandType.name + " Island");
        meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
        
        // Set lore with description and difficulty info
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(miniMessage.deserialize("<gray>" + islandType.description));
        lore.add(Component.empty());
        lore.add(miniMessage.deserialize("<white>Difficulty: " + difficultyText));
        lore.add(Component.empty());
        lore.add(Component.text("Click to select this island type")
            .color(NamedTextColor.GREEN));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void addControlItems(Inventory inventory) {
        // Add dark gray stained glass border
        addGlassBorder(inventory);
        
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
        inventory.setItem(49, createButton); // Bottom row, center

        // Cancel button - bottom row left
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
        inventory.setItem(45, cancelButton); // Bottom row, left
        
        // Book and quill item
        ItemStack bookItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta bookMeta = bookItem.getItemMeta();
        bookMeta.displayName(Component.text("Island Creation")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        bookMeta.lore(List.of(
            Component.empty(),
            Component.text("Choose your island template")
                .color(NamedTextColor.GRAY),
            Component.text("This cannot be changed later without restarting!")
                .color(NamedTextColor.YELLOW),
            Component.empty(),
            Component.text("Each island type has unique")
                .color(NamedTextColor.GRAY),
            Component.text("structures and starting items")
                .color(NamedTextColor.GRAY)
        ));
        bookItem.setItemMeta(bookMeta);
        inventory.setItem(4, bookItem); // Top row, center
    }
    
    private void addGlassBorder(Inventory inventory) {
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.displayName(Component.text(" "));
        glass.setItemMeta(glassMeta);
        
        // Top row border (1 layer)
        for (int i = 0; i < 9; i++) {
            if (i != 4) { // Skip center where book goes
                inventory.setItem(i, glass);
            }
        }
        
        // Left and right side borders (2 layers on each side)
        for (int row = 1; row < 5; row++) { // Rows 2-5
            inventory.setItem(row * 9, glass);     // Left border column 1
            inventory.setItem(row * 9 + 1, glass); // Left border column 2
            inventory.setItem(row * 9 + 7, glass); // Right border column 1
            inventory.setItem(row * 9 + 8, glass); // Right border column 2
        }
        
        // Bottom row border (1 layer)
        for (int i = 45; i < 54; i++) {
            if (i != 49) { // Skip center where create button goes
                inventory.setItem(i, glass);
            }
        }
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
        inventory.setItem(49, createButton);
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
        
        // Define the slots for island type selection (matching addIslandTypeItems layout)
        int[] validSlots = {
            // Row 1: Easy islands (5 total) - slots 11-15
            11, 12, 13, 14, 15, 
            // Row 2: Easy + Medium islands (5 total) - slots 20-24
            20, 21, 22, 23, 24,
            // Row 3: Medium + Hard islands (5 total) - slots 29-33
            29, 30, 31, 32, 33,
            // Row 4: Hard islands (4 total) - slots 38-41
            38, 39, 40, 41
        };
        
        // Check if clicked slot is one of the island type slots
        boolean isIslandSlot = false;
        int slotIndex = -1;
        for (int i = 0; i < validSlots.length; i++) {
            if (validSlots[i] == slot) {
                isIslandSlot = true;
                slotIndex = i;
                break;
            }
        }
        
        if (isIslandSlot) {
            handleIslandTypeSelection(player, event.getInventory(), slot, slotIndex);
            return;
        }
        
        // Handle create button (slot 49)
        if (slot == 49) {
            handleCreateButton(player, clickedItem.getType());
            return;
        }
        
        // Handle cancel button (slot 45)
        if (slot == 45) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            player.closeInventory();
            player.sendMessage(miniMessage.deserialize("<yellow>Island creation cancelled.</yellow>"));
            return;
        }
        
        // Info item (slot 4) - do nothing
    }
    
    private void handleIslandTypeSelection(Player player, Inventory inventory, int slot, int slotIndex) {
        if (slotIndex >= 0 && slotIndex < ISLAND_TYPES.length) {
            IslandType selectedIslandType = ISLAND_TYPES[slotIndex];
            String selectedType = selectedIslandType.name.toLowerCase();
            
            // Update player selection
            playerSelections.put(player.getUniqueId(), selectedType);
            
            // Play selection sound
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            
            // Update visual feedback
            updateSelectionVisuals(inventory, slot);
            updateCreateButton(inventory, selectedType);
            
            // Send feedback message with difficulty info
            String difficultyColor = switch (selectedIslandType.difficulty) {
                case EASY -> "<green>";
                case MEDIUM -> "<yellow>";
                case HARD -> "<red>";
            };
            
            player.sendMessage(miniMessage.deserialize("<green>Selected: " + difficultyColor + 
                selectedIslandType.name + " Island <gray>(" + selectedIslandType.difficulty.toString() + " difficulty)"));
        }
    }
    
    private void updateSelectionVisuals(Inventory inventory, int selectedSlot) {
        // Define all slots (matching the layout in addIslandTypeItems)
        int[] allSlots = {
            // Row 1: Easy islands (5 total) - slots 11-15
            11, 12, 13, 14, 15, 
            // Row 2: Easy + Medium islands (5 total) - slots 20-24
            20, 21, 22, 23, 24,
            // Row 3: Medium + Hard islands (5 total) - slots 29-33
            29, 30, 31, 32, 33,
            // Row 4: Hard islands (4 total) - slots 38-41
            38, 39, 40, 41
        };
        
        // Reset all island type items to normal appearance
        for (int slot : allSlots) {
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
