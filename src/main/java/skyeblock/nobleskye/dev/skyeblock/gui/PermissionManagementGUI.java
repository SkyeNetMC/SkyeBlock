package skyeblock.nobleskye.dev.skyeblock.gui;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import skyeblock.nobleskye.dev.skyeblock.permissions.IslandPermission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * GUI for managing island permissions for players
 */
public class PermissionManagementGUI implements Listener {
    private final SkyeBlockPlugin plugin;
    private final Map<UUID, String> openGUIs = new HashMap<>(); // Player UUID -> Island ID
    private final Map<UUID, UUID> targetPlayers = new HashMap<>(); // Viewer UUID -> Target Player UUID

    public PermissionManagementGUI(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Open permission management GUI for a player on an island
     */
    public void openPermissionGUI(Player viewer, String islandId, UUID targetPlayerUUID) {
        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island == null) {
            viewer.sendMessage(Component.text("Island not found!", NamedTextColor.RED));
            return;
        }

        // Check if viewer has permission to manage permissions
        if (!island.getOwnerUUID().equals(viewer.getUniqueId()) && 
            !viewer.hasPermission("skyeblock.admin.permissions")) {
            viewer.sendMessage(Component.text("You don't have permission to manage island permissions!", NamedTextColor.RED));
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetPlayerUUID);
        String targetName = targetPlayer != null ? targetPlayer.getName() : "Unknown Player";

        Inventory gui = Bukkit.createInventory(null, 54, 
            Component.text("Permissions: " + targetName, NamedTextColor.DARK_GREEN, TextDecoration.BOLD));

        // Store GUI context
        openGUIs.put(viewer.getUniqueId(), islandId);
        targetPlayers.put(viewer.getUniqueId(), targetPlayerUUID);

        // Get current role and permissions
        Island.CoopRole currentRole = island.getCoopRole(targetPlayerUUID);
        Set<IslandPermission> rolePermissions = plugin.getPermissionManager().getRolePermissions(currentRole);
        Set<IslandPermission> customPermissions = island.getCustomPermissions(targetPlayerUUID);

        // Add role info
        ItemStack roleItem = new ItemStack(Material.NAME_TAG);
        ItemMeta roleMeta = roleItem.getItemMeta();
        roleMeta.displayName(Component.text("Current Role: " + currentRole.getDisplayName(), NamedTextColor.GOLD));
        List<Component> roleLore = new ArrayList<>();
        roleLore.add(Component.text("Default permissions for this role:", NamedTextColor.GRAY));
        for (IslandPermission perm : rolePermissions) {
            roleLore.add(Component.text("  • " + perm.getDescription(), NamedTextColor.GREEN));
        }
        roleMeta.lore(roleLore);
        roleItem.setItemMeta(roleMeta);
        gui.setItem(4, roleItem);

        // Add permission items
        int slot = 9;
        for (IslandPermission permission : IslandPermission.values()) {
            if (slot >= 45) break; // Don't go beyond row 5

            boolean hasFromRole = rolePermissions.contains(permission);
            boolean hasCustom = customPermissions.contains(permission);
            
            Material material;
            NamedTextColor color;
            String status;
            
            if (hasCustom) {
                material = Material.EMERALD_BLOCK;
                color = NamedTextColor.GREEN;
                status = "Custom Permission (Click to remove)";
            } else if (hasFromRole) {
                material = Material.GOLD_BLOCK;
                color = NamedTextColor.YELLOW;
                status = "From Role (Click to override)";
            } else {
                material = Material.REDSTONE_BLOCK;
                color = NamedTextColor.RED;
                status = "Not Granted (Click to grant)";
            }

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(permission.getDescription(), color, TextDecoration.BOLD));
            
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(permission.getDescription(), NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("Category: " + permission.getCategory().name(), NamedTextColor.BLUE));
            lore.add(Component.text("Status: " + status, NamedTextColor.WHITE));
            
            meta.lore(lore);
            item.setItemMeta(meta);
            gui.setItem(slot, item);
            
            slot++;
        }

        // Add close button
        ItemStack closeItem = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeItem.getItemMeta();
        closeMeta.displayName(Component.text("Close", NamedTextColor.RED, TextDecoration.BOLD));
        closeItem.setItemMeta(closeMeta);
        gui.setItem(53, closeItem);

        viewer.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().title().toString();
        
        if (!title.contains("Permissions:") || !openGUIs.containsKey(player.getUniqueId())) {
            return;
        }

        event.setCancelled(true);

        String islandId = openGUIs.get(player.getUniqueId());
        UUID targetPlayerUUID = targetPlayers.get(player.getUniqueId());
        
        if (islandId == null || targetPlayerUUID == null) {
            return;
        }

        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island == null) {
            player.closeInventory();
            openGUIs.remove(player.getUniqueId());
            targetPlayers.remove(player.getUniqueId());
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Handle close button
        if (clicked.getType() == Material.BARRIER) {
            player.closeInventory();
            openGUIs.remove(player.getUniqueId());
            targetPlayers.remove(player.getUniqueId());
            return;
        }

        // Handle permission toggle
        if (clicked.getType() == Material.EMERALD_BLOCK || 
            clicked.getType() == Material.GOLD_BLOCK || 
            clicked.getType() == Material.REDSTONE_BLOCK) {
            
            Component displayName = clicked.getItemMeta().displayName();
            if (displayName == null) return;
            
            String permissionName = ((net.kyori.adventure.text.TextComponent) displayName).content();
            
            // Find the permission by display name
            IslandPermission targetPermission = null;
            for (IslandPermission perm : IslandPermission.values()) {
                if (perm.getDescription().equals(permissionName)) {
                    targetPermission = perm;
                    break;
                }
            }
            
            if (targetPermission == null) return;

            Set<IslandPermission> customPermissions = island.getCustomPermissions(targetPlayerUUID);
            
            if (customPermissions.contains(targetPermission)) {
                // Remove custom permission
                island.removeCustomPermission(targetPlayerUUID, targetPermission);
                player.sendMessage(Component.text("Removed custom permission: " + targetPermission.getDescription(), NamedTextColor.YELLOW));
            } else {
                // Add custom permission
                island.addCustomPermission(targetPlayerUUID, targetPermission);
                player.sendMessage(Component.text("Granted custom permission: " + targetPermission.getDescription(), NamedTextColor.GREEN));
            }
            
            // Refresh the GUI
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                openPermissionGUI(player, islandId, targetPlayerUUID);
            }, 1L);
        }
    }

    /**
     * Clean up when player closes inventory
     */
    public void cleanupPlayer(UUID playerUUID) {
        openGUIs.remove(playerUUID);
        targetPlayers.remove(playerUUID);
    }
}
