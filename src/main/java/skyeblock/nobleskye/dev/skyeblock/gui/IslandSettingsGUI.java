package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.managers.IslandSettingsManager;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
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

public class IslandSettingsGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final IslandSettingsManager settingsManager;
    private final MiniMessage miniMessage;
    private final Map<UUID, Integer> playerPages;
    private final Map<UUID, String> playerIslands;
    private final Map<UUID, Boolean> pendingResets;
    
    private static final int ITEMS_PER_PAGE = 45; // 9*5 rows for items
    private static final int INVENTORY_SIZE = 54; // 6 rows
    
    public IslandSettingsGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.settingsManager = plugin.getIslandSettingsManager();
        this.miniMessage = MiniMessage.miniMessage();
        this.playerPages = new HashMap<>();
        this.playerIslands = new HashMap<>();
        this.pendingResets = new HashMap<>();
        
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
        playerPages.put(player.getUniqueId(), 0);
        
        openPage(player, 0);
    }
    
    private void openPage(Player player, int page) {
        List<GameRule<?>> availableRules = settingsManager.getAvailableGameRules(player);
        
        if (availableRules.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to modify any gamerules!</red>"));
            return;
        }
        
        int totalPages = (int) Math.ceil((double) availableRules.size() / ITEMS_PER_PAGE);
        page = Math.max(0, Math.min(page, totalPages - 1));
        
        playerPages.put(player.getUniqueId(), page);
        
        Inventory inventory = Bukkit.createInventory(this, INVENTORY_SIZE, 
            Component.text("Island Settings - Page " + (page + 1) + "/" + totalPages)
                .color(NamedTextColor.DARK_BLUE)
                .decoration(TextDecoration.BOLD, true));
        
        String islandId = playerIslands.get(player.getUniqueId());
        
        // Add gamerule items
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, availableRules.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            GameRule<?> gameRule = availableRules.get(i);
            ItemStack item = createGameRuleItem(islandId, gameRule, player);
            inventory.setItem(i - startIndex, item);
        }
        
        // Add navigation items
        addNavigationItems(inventory, page, totalPages, player);
        
        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        player.openInventory(inventory);
    }
    
    private ItemStack createGameRuleItem(String islandId, GameRule<?> gameRule, Player player) {
        Object currentValue = settingsManager.getGameRule(islandId, gameRule);
        
        Material material;
        if (gameRule.getType() == Boolean.class) {
            material = (Boolean) currentValue ? Material.LIME_DYE : Material.RED_DYE;
        } else {
            material = Material.COMPARATOR;
        }
        
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Title
        String displayName = formatGameRuleName(gameRule.getName());
        meta.displayName(Component.text(displayName)
            .color(NamedTextColor.YELLOW)
            .decoration(TextDecoration.ITALIC, false));
        
        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Current value: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(String.valueOf(currentValue))
                .color(NamedTextColor.WHITE)));
        
        lore.add(Component.empty());
        lore.add(Component.text("Description:")
            .color(NamedTextColor.GRAY));
        
        // Add description based on gamerule
        String description = getGameRuleDescription(gameRule);
        for (String line : description.split("\n")) {
            lore.add(Component.text("  " + line)
                .color(NamedTextColor.DARK_GRAY));
        }
        
        lore.add(Component.empty());
        if (gameRule.getType() == Boolean.class) {
            lore.add(Component.text("Click: Toggle value")
                .color(NamedTextColor.GREEN));
        } else {
            lore.add(Component.text("Left click: Increase by 1")
                .color(NamedTextColor.GREEN));
            lore.add(Component.text("Shift + Left: Increase by 10")
                .color(NamedTextColor.AQUA));
            lore.add(Component.text("Shift + Right: Decrease by 1")
                .color(NamedTextColor.YELLOW));
            lore.add(Component.text("Right click: Decrease by 10")
                .color(NamedTextColor.GOLD));
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        
        return item;
    }
    
    private void addNavigationItems(Inventory inventory, int currentPage, int totalPages, Player player) {
        // Previous page
        if (currentPage > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta meta = prevPage.getItemMeta();
            meta.displayName(Component.text("Previous Page")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("Go to page " + currentPage)
                .color(NamedTextColor.GRAY)));
            prevPage.setItemMeta(meta);
            inventory.setItem(45, prevPage);
        }
        
        // Next page
        if (currentPage < totalPages - 1) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            meta.displayName(Component.text("Next Page")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("Go to page " + (currentPage + 2))
                .color(NamedTextColor.GRAY)));
            nextPage.setItemMeta(meta);
            inventory.setItem(53, nextPage);
        }
        
        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Island Settings")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Configure your island's gamerules")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("These settings only affect your island")
            .color(NamedTextColor.GRAY));
        lore.add(Component.empty());
        lore.add(Component.text("Page " + (currentPage + 1) + " of " + totalPages)
            .color(NamedTextColor.YELLOW));
        
        meta.lore(lore);
        info.setItemMeta(meta);
        inventory.setItem(49, info);
        
        // Close button
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        closeMeta.displayName(Component.text("Close")
            .color(NamedTextColor.RED)
            .decoration(TextDecoration.ITALIC, false));
        close.setItemMeta(closeMeta);
        inventory.setItem(48, close);
        
        // Reset to defaults button
        Boolean hasPendingReset = pendingResets.get(player.getUniqueId());
        ItemStack reset = new ItemStack(hasPendingReset != null && hasPendingReset ? Material.REDSTONE_BLOCK : Material.TNT);
        ItemMeta resetMeta = reset.getItemMeta();
        
        if (hasPendingReset != null && hasPendingReset) {
            resetMeta.displayName(Component.text("⚠ CONFIRM RESET ⚠")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, true)
                .decoration(TextDecoration.ITALIC, false));
            resetMeta.lore(List.of(
                Component.empty(),
                Component.text("Click again to CONFIRM")
                    .color(NamedTextColor.RED)
                    .decoration(TextDecoration.BOLD, true),
                Component.text("resetting ALL gamerules")
                    .color(NamedTextColor.RED),
                Component.text("to their default values!")
                    .color(NamedTextColor.RED),
                Component.empty(),
                Component.text("This action cannot be undone!")
                    .color(NamedTextColor.DARK_RED)
                    .decoration(TextDecoration.BOLD, true)
            ));
        } else {
            resetMeta.displayName(Component.text("Reset to Defaults")
                .color(NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
            resetMeta.lore(List.of(
                Component.text("Click to reset all gamerules")
                    .color(NamedTextColor.GRAY),
                Component.text("to their default values")
                    .color(NamedTextColor.GRAY),
                Component.empty(),
                Component.text("Requires confirmation")
                    .color(NamedTextColor.YELLOW)
            ));
        }
        
        reset.setItemMeta(resetMeta);
        inventory.setItem(50, reset);
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof IslandSettingsGUI)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        String islandId = playerIslands.get(player.getUniqueId());
        if (islandId == null) return;
        
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        // Handle navigation
        if (slot == 45 && clickedItem.getType() == Material.ARROW) { // Previous page
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
            // Clear pending reset when navigating
            pendingResets.remove(player.getUniqueId());
            int currentPage = playerPages.get(player.getUniqueId());
            openPage(player, currentPage - 1);
            return;
        }
        
        if (slot == 53 && clickedItem.getType() == Material.ARROW) { // Next page
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
            // Clear pending reset when navigating
            pendingResets.remove(player.getUniqueId());
            int currentPage = playerPages.get(player.getUniqueId());
            openPage(player, currentPage + 1);
            return;
        }
        
        if (slot == 48 && clickedItem.getType() == Material.BARRIER) { // Close
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }
        
        if (slot == 50 && (clickedItem.getType() == Material.TNT || clickedItem.getType() == Material.REDSTONE_BLOCK)) { // Reset to defaults
            UUID playerId = player.getUniqueId();
            Boolean hasPendingReset = pendingResets.get(playerId);
            
            if (hasPendingReset != null && hasPendingReset) {
                // Confirm and execute reset
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
                executeResetToDefaults(player, islandId);
                pendingResets.remove(playerId);
                refreshCurrentPage(player); // This will return the button to normal state
            } else {
                // First click - show confirmation
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                pendingResets.put(playerId, true);
                player.sendMessage(miniMessage.deserialize(
                    "<yellow>⚠ Click the Reset button again to confirm resetting ALL gamerules to defaults!</yellow>"));
                refreshCurrentPage(player);
            }
            return;
        }
        
        if (slot == 49) return; // Info item, do nothing
        
        // Handle gamerule modification
        if (slot < ITEMS_PER_PAGE) {
            handleGameRuleClick(player, islandId, slot, event.isShiftClick(), event.isRightClick());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleGameRuleClick(Player player, String islandId, int slot, boolean isShift, boolean isRightClick) {
        List<GameRule<?>> availableRules = settingsManager.getAvailableGameRules(player);
        int currentPage = playerPages.get(player.getUniqueId());
        int actualIndex = currentPage * ITEMS_PER_PAGE + slot;
        
        if (actualIndex >= availableRules.size()) return;
        
        GameRule<?> gameRule = availableRules.get(actualIndex);
        
        // Apply changes immediately
        player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_DISPENSE, 1.0f, 1.2f);
        Object newValue;
        
        if (gameRule.getType() == Boolean.class) {
            // For boolean, toggle the current value
            Boolean currentValue = settingsManager.getGameRule(islandId, (GameRule<Boolean>) gameRule);
            newValue = !currentValue;
            settingsManager.setGameRule(islandId, (GameRule<Boolean>) gameRule, (Boolean) newValue);
        } else if (gameRule.getType() == Integer.class) {
            Integer currentValue = settingsManager.getGameRule(islandId, (GameRule<Integer>) gameRule);
            int change;
            
            if (isRightClick) {
                if (isShift) {
                    // Shift + Right click: Decrease by 1
                    change = -1;
                } else {
                    // Right click: Decrease by 10
                    change = -10;
                }
            } else {
                if (isShift) {
                    // Shift + Left click: Increase by 10
                    change = 10;
                } else {
                    // Left click: Increase by 1
                    change = 1;
                }
            }
            
            newValue = Math.max(0, currentValue + change);
            settingsManager.setGameRule(islandId, (GameRule<Integer>) gameRule, (Integer) newValue);
        } else {
            return; // Unknown type
        }
        
        player.sendMessage(miniMessage.deserialize(
            "<green>✓ Set " + formatGameRuleName(gameRule.getName()) + " = " + newValue + "</green>"));
        
        // Refresh the inventory to show the change
        refreshCurrentPage(player);
    }
    
    @SuppressWarnings("unchecked")
    private void executeResetToDefaults(Player player, String islandId) {
        List<GameRule<?>> availableRules = settingsManager.getAvailableGameRules(player);
        Map<GameRule<?>, Object> defaults = settingsManager.getDefaultGameRules();
        
        for (GameRule<?> gameRule : availableRules) {
            Object defaultValue = defaults.get(gameRule);
            if (defaultValue != null) {
                if (gameRule.getType() == Boolean.class) {
                    settingsManager.setGameRule(islandId, (GameRule<Boolean>) gameRule, (Boolean) defaultValue);
                } else if (gameRule.getType() == Integer.class) {
                    settingsManager.setGameRule(islandId, (GameRule<Integer>) gameRule, (Integer) defaultValue);
                }
            }
        }
        
        player.sendMessage(miniMessage.deserialize("<green>✓ Reset all gamerules to their default values!</green>"));
        
        // Refresh the current page inventory
        refreshCurrentPage(player);
    }
    
    private void refreshCurrentPage(Player player) {
        Inventory currentInventory = player.getOpenInventory().getTopInventory();
        if (currentInventory == null || !(currentInventory.getHolder() instanceof IslandSettingsGUI)) {
            return;
        }
        
        String islandId = playerIslands.get(player.getUniqueId());
        int currentPage = playerPages.get(player.getUniqueId());
        List<GameRule<?>> availableRules = settingsManager.getAvailableGameRules(player);
        
        // Clear only the gamerule items (slots 0-44), keep navigation items
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            currentInventory.setItem(i, null);
        }
        
        // Add updated gamerule items
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, availableRules.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            GameRule<?> gameRule = availableRules.get(i);
            ItemStack item = createGameRuleItem(islandId, gameRule, player);
            currentInventory.setItem(i - startIndex, item);
        }
        
        // Refresh navigation items to update reset button state
        int totalPages = (int) Math.ceil((double) availableRules.size() / ITEMS_PER_PAGE);
        addNavigationItems(currentInventory, currentPage, totalPages, player);
        
        // Update the player's view
        player.updateInventory();
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof IslandSettingsGUI) {
            Player player = (Player) event.getPlayer();
            UUID playerId = player.getUniqueId();
            
            // Clear player data
            playerPages.remove(playerId);
            playerIslands.remove(playerId);
            
            // Clear any pending reset confirmation
            Boolean pendingReset = pendingResets.remove(playerId);
            if (pendingReset != null && pendingReset) {
                player.sendMessage(miniMessage.deserialize(
                    "<yellow>Cancelled pending reset to defaults</yellow>"));
            }
        }
    }
    
    private String formatGameRuleName(String ruleName) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = ruleName.split("(?=[A-Z])|_");
        
        for (String part : parts) {
            if (part.length() > 0) {
                if (formatted.length() > 0) {
                    formatted.append(" ");
                }
                formatted.append(part.substring(0, 1).toUpperCase())
                         .append(part.substring(1).toLowerCase());
            }
        }
        
        return formatted.toString();
    }
    
    private String getGameRuleDescription(GameRule<?> gameRule) {
        switch (gameRule.getName()) {
            case "doDaylightCycle":
                return "Whether the daylight cycle advances";
            case "doWeatherCycle":
                return "Whether weather changes naturally";
            case "keepInventory":
                return "Whether players keep items after death";
            case "mobGriefing":
                return "Whether mobs can change blocks";
            case "doMobSpawning":
                return "Whether mobs spawn naturally";
            case "doFireTick":
                return "Whether fire spreads";
            case "fallDamage":
                return "Whether players take fall damage";
            case "fireDamage":
                return "Whether players take fire damage";
            case "drowningDamage":
                return "Whether players take drowning damage";
            case "doInsomnia":
                return "Whether phantoms spawn when players\ndon't sleep";
            case "doImmediateRespawn":
                return "Whether players respawn immediately\nwithout death screen";
            case "announceAdvancements":
                return "Whether advancement messages\nare shown in chat";
            case "randomTickSpeed":
                return "How fast random events occur\n(plant growth, etc.)";
            case "spawnRadius":
                return "Radius around spawn where players\ncan spawn";
            case "maxEntityCramming":
                return "Maximum entities that can be\ncrammed in one block";
            case "naturalRegeneration":
                return "Whether players regenerate health\nnaturally";
            case "showDeathMessages":
                return "Whether death messages are shown";
            case "doEntityDrops":
                return "Whether entities drop items when killed";
            case "doTileDrops":
                return "Whether blocks drop items when broken";
            case "doMobLoot":
                return "Whether mobs drop loot when killed";
            case "playersSleepingPercentage":
                return "Percentage of players that need to\nsleep to skip night";
            default:
                return "A gamerule that affects gameplay";
        }
    }
}
