package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class IslandVisitGUI implements InventoryHolder, Listener {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    private final Map<UUID, Integer> playerPages;
    
    private static final int ITEMS_PER_PAGE = 45; // 9*5 rows for items
    private static final int INVENTORY_SIZE = 54; // 6 rows
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm");
    
    public IslandVisitGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.playerPages = new HashMap<>();
        
        // Register this as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
    
    @Override
    public Inventory getInventory() {
        return null; // Not used in this implementation
    }
    
    public void openVisitGUI(Player player) {
        // Check if visiting is enabled
        if (!plugin.getConfig().getBoolean("island.visiting.enabled", false)) {
            plugin.sendMessage(player, "visiting-disabled");
            return;
        }
        
        playerPages.put(player.getUniqueId(), 0);
        openPage(player, 0);
    }
    
    private void openPage(Player player, int page) {
        List<Island> visitableIslands = getVisitableIslands(player);
        
        if (visitableIslands.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<yellow>No islands available to visit!</yellow>"));
            return;
        }
        
        int totalPages = (int) Math.ceil((double) visitableIslands.size() / ITEMS_PER_PAGE);
        page = Math.max(0, Math.min(page, totalPages - 1));
        
        playerPages.put(player.getUniqueId(), page);
        
        Inventory inventory = Bukkit.createInventory(this, INVENTORY_SIZE, 
            Component.text("Visit Islands - Page " + (page + 1) + "/" + totalPages)
                .color(NamedTextColor.BLUE)
                .decoration(TextDecoration.BOLD, true));
        
        // Add island items
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, visitableIslands.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Island island = visitableIslands.get(i);
            ItemStack item = createIslandItem(island, player);
            inventory.setItem(i - startIndex, item);
        }
        
        // Add navigation items
        addNavigationItems(inventory, page, totalPages);
        
        // Play opening sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
        
        player.openInventory(inventory);
    }
    
    private List<Island> getVisitableIslands(Player player) {
        List<Island> allIslands = plugin.getIslandManager().getAllIslands();
        List<Island> onlineIslands = new ArrayList<>();
        List<Island> offlineIslands = new ArrayList<>();
        
        for (Island island : allIslands) {
            // Skip own island and locked islands (unless player has coop access)
            if (island.getOwnerUUID().equals(player.getUniqueId())) {
                continue;
            }
            
            if (island.isLocked() && !island.hasCoopAccess(player.getUniqueId())) {
                continue;
            }
            
            // Check if owner is online
            OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwnerUUID());
            if (owner.isOnline()) {
                onlineIslands.add(island);
                island.updateLastOnlineTime(); // Update last online time
            } else {
                offlineIslands.add(island);
            }
        }
        
        // Sort online islands by vote count (descending)
        onlineIslands.sort((a, b) -> Integer.compare(b.getVoteCount(), a.getVoteCount()));
        
        // Sort offline islands by last online time (most recent first)
        offlineIslands.sort((a, b) -> Long.compare(b.getLastOnlineTime(), a.getLastOnlineTime()));
        
        // Combine lists: online first, then offline
        List<Island> result = new ArrayList<>();
        result.addAll(onlineIslands);
        result.addAll(offlineIslands);
        
        return result;
    }
    
    private ItemStack createIslandItem(Island island, Player viewer) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(island.getOwnerUUID());
        
        // Create player head
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        
        // Set the skull owner
        meta.setOwningPlayer(owner);
        
        // Set display name
        String displayTitle = island.getDisplayTitle();
        if (displayTitle == null || displayTitle.trim().isEmpty()) {
            displayTitle = owner.getName() + "'s Island";
        }
        
        Component nameComponent = Component.text(displayTitle)
            .color(owner.isOnline() ? NamedTextColor.GREEN : NamedTextColor.GRAY)
            .decoration(TextDecoration.ITALIC, false);
        
        if (owner.isOnline()) {
            nameComponent = nameComponent.append(Component.text(" ●").color(NamedTextColor.GREEN));
        }
        
        meta.displayName(nameComponent);
        
        // Create lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        
        // Island type
        lore.add(Component.text("Type: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text(island.getIslandType())
                .color(NamedTextColor.WHITE)));
        
        // Description (if set)
        String description = island.getDisplayDescription();
        if (description != null && !description.trim().isEmpty()) {
            lore.add(Component.text("Description: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(description)
                    .color(NamedTextColor.WHITE)));
        }
        
        lore.add(Component.empty());
        
        // Owner status
        if (owner.isOnline()) {
            lore.add(Component.text("Owner: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(owner.getName() + " (Online)")
                    .color(NamedTextColor.GREEN)));
        } else {
            long lastSeen = island.getLastOnlineTime();
            String lastSeenStr = DATE_FORMAT.format(new Date(lastSeen));
            lore.add(Component.text("Owner: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(owner.getName() + " (Offline)")
                    .color(NamedTextColor.RED)));
            lore.add(Component.text("Last seen: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(lastSeenStr)
                    .color(NamedTextColor.DARK_GRAY)));
        }
        
        // Vote count
        int votes = island.getVoteCount();
        lore.add(Component.text("Votes: ")
            .color(NamedTextColor.GRAY)
            .append(Component.text("" + votes)
                .color(votes > 0 ? NamedTextColor.GOLD : NamedTextColor.WHITE)));
        
        // Voting status
        if (island.hasVoted(viewer.getUniqueId())) {
            if (island.canVote(viewer.getUniqueId())) {
                lore.add(Component.text("✓ You can vote again")
                    .color(NamedTextColor.YELLOW));
            } else {
                lore.add(Component.text("✓ You voted recently")
                    .color(NamedTextColor.GRAY));
            }
        } else {
            lore.add(Component.text("Vote for this island!")
                .color(NamedTextColor.GOLD));
        }
        
        lore.add(Component.empty());
        
        // Coop status
        Island.CoopRole role = island.getCoopRole(viewer.getUniqueId());
        if (role != Island.CoopRole.VISITOR) {
            lore.add(Component.text("Role: ")
                .color(NamedTextColor.GRAY)
                .append(Component.text(role.getDisplayName())
                    .color(NamedTextColor.BLUE)));
        }
        
        // Actions
        lore.add(Component.text("Left click: Visit island")
            .color(NamedTextColor.GREEN));
        
        if (!island.hasVoted(viewer.getUniqueId()) || island.canVote(viewer.getUniqueId())) {
            lore.add(Component.text("Right click: Vote for island")
                .color(NamedTextColor.GOLD));
        }
        
        meta.lore(lore);
        
        // Set custom icon if configured
        if (island.getDisplayIcon() != null) {
            // Note: We'll keep the player head but could add custom model data or other modifications
            // For now, we'll stick with player heads as requested
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    private void addNavigationItems(Inventory inventory, int currentPage, int totalPages) {
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
        ItemStack info = new ItemStack(Material.COMPASS);
        ItemMeta meta = info.getItemMeta();
        meta.displayName(Component.text("Island Visits")
            .color(NamedTextColor.GOLD)
            .decoration(TextDecoration.BOLD, true)
            .decoration(TextDecoration.ITALIC, false));
        
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("Visit other players' islands")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("Online players shown first")
            .color(NamedTextColor.GRAY));
        lore.add(Component.text("Sorted by votes and activity")
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
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!(event.getInventory().getHolder() instanceof IslandVisitGUI)) return;
        
        event.setCancelled(true);
        
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        
        // Handle navigation
        if (slot == 45 && clickedItem.getType() == Material.ARROW) { // Previous page
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
            int currentPage = playerPages.get(player.getUniqueId());
            openPage(player, currentPage - 1);
            return;
        }
        
        if (slot == 53 && clickedItem.getType() == Material.ARROW) { // Next page
            player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1.0f, 1.0f);
            int currentPage = playerPages.get(player.getUniqueId());
            openPage(player, currentPage + 1);
            return;
        }
        
        if (slot == 48 && clickedItem.getType() == Material.BARRIER) { // Close
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
            player.closeInventory();
            return;
        }
        
        if (slot == 49) return; // Info item, do nothing
        
        // Handle island interaction
        if (slot < ITEMS_PER_PAGE && clickedItem.getType() == Material.PLAYER_HEAD) {
            handleIslandClick(player, slot, event.isRightClick());
        }
    }
    
    private void handleIslandClick(Player player, int slot, boolean isRightClick) {
        List<Island> visitableIslands = getVisitableIslands(player);
        int currentPage = playerPages.get(player.getUniqueId());
        int actualIndex = currentPage * ITEMS_PER_PAGE + slot;
        
        if (actualIndex >= visitableIslands.size()) return;
        
        Island island = visitableIslands.get(actualIndex);
        
        if (isRightClick) {
            // Vote for island
            if (island.canVote(player.getUniqueId())) {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                island.addVote(player.getUniqueId());
                player.sendMessage(miniMessage.deserialize(
                    "<gold>✓ You voted for " + Bukkit.getOfflinePlayer(island.getOwnerUUID()).getName() + "'s island!</gold>"));
                openPage(player, currentPage); // Refresh to show updated vote count
            } else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                player.sendMessage(miniMessage.deserialize(
                    "<yellow>You can only vote once every 23 hours!</yellow>"));
            }
        } else {
            // Visit island
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            player.closeInventory();
            plugin.getIslandManager().teleportToIsland(player, island);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof IslandVisitGUI) {
            Player player = (Player) event.getPlayer();
            playerPages.remove(player.getUniqueId());
        }
    }
}
