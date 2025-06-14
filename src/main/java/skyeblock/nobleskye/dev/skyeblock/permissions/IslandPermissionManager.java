package skyeblock.nobleskye.dev.skyeblock.permissions;

import org.bukkit.entity.Player;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;

import java.util.*;

/**
 * Permission manager for island-specific permissions
 * Integrates with the existing role-based system and provides Flan-style granular control
 */
public class IslandPermissionManager {
    
    private final SkyeBlockPlugin plugin;
    private final Map<Island.CoopRole, Set<IslandPermission>> defaultRolePermissions;
    
    public IslandPermissionManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.defaultRolePermissions = new HashMap<>();
        
        initializeDefaultPermissions();
    }
    
    /**
     * Initialize default permissions for each role
     */
    private void initializeDefaultPermissions() {
        // VISITOR - Very limited access (similar to current adventure mode restrictions)
        Set<IslandPermission> visitorPerms = EnumSet.of(
            IslandPermission.BUTTON_USE,
            IslandPermission.DOOR_USE,
            IslandPermission.CRAFTING_TABLE_USE,
            IslandPermission.ENCHANTING_TABLE_USE
        );
        defaultRolePermissions.put(Island.CoopRole.VISITOR, visitorPerms);
        
        // MEMBER - Container access and basic interactions
        Set<IslandPermission> memberPerms = EnumSet.of(
            IslandPermission.CONTAINER_ACCESS,
            IslandPermission.CHEST_ACCESS,
            IslandPermission.FURNACE_ACCESS,
            IslandPermission.CRAFTING_TABLE_USE,
            IslandPermission.ENCHANTING_TABLE_USE,
            IslandPermission.BUTTON_USE,
            IslandPermission.DOOR_USE,
            IslandPermission.ITEM_PICKUP,
            IslandPermission.ITEM_DROP,
            IslandPermission.ENTITY_INTERACT,
            IslandPermission.ANIMAL_INTERACT,
            IslandPermission.VILLAGER_TRADE
        );
        defaultRolePermissions.put(Island.CoopRole.MEMBER, memberPerms);
        
        // ADMIN - Full access except some admin functions
        Set<IslandPermission> adminPerms = EnumSet.allOf(IslandPermission.class);
        adminPerms.remove(IslandPermission.BYPASS_PROTECTION);
        defaultRolePermissions.put(Island.CoopRole.ADMIN, adminPerms);
        
        // CO_OWNER - Full access
        Set<IslandPermission> coOwnerPerms = EnumSet.allOf(IslandPermission.class);
        defaultRolePermissions.put(Island.CoopRole.CO_OWNER, coOwnerPerms);
        
        // OWNER - Full access
        Set<IslandPermission> ownerPerms = EnumSet.allOf(IslandPermission.class);
        defaultRolePermissions.put(Island.CoopRole.OWNER, ownerPerms);
    }
    
    /**
     * Check if a player has a specific permission on an island
     */
    public boolean hasPermission(Player player, String islandId, IslandPermission permission) {
        // Check for server-wide permission bypass
        if (player.hasPermission("skyeblock.admin.bypass")) {
            return true;
        }
        
        // Check for specific permission bypass
        if (player.hasPermission(permission.getFullPermissionNode() + ".bypass")) {
            return true;
        }
        
        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island == null) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        Island.CoopRole role = island.getCoopRole(playerUUID);
        
        // Check for custom player permissions first
        Set<IslandPermission> customPerms = island.getCustomPermissions(playerUUID);
        if (!customPerms.isEmpty() && customPerms.contains(permission)) {
            return true;
        }
        
        // Check role-based permissions
        Set<IslandPermission> rolePermissions = defaultRolePermissions.get(role);
        if (rolePermissions != null && rolePermissions.contains(permission)) {
            return true;
        }
        
        // Check for specific server permission
        if (player.hasPermission(permission.getFullPermissionNode())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if a player has any container access permission
     */
    public boolean hasContainerAccess(Player player, String islandId) {
        return hasPermission(player, islandId, IslandPermission.CONTAINER_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.CHEST_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.FURNACE_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.HOPPER_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.DISPENSER_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.BARREL_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.SHULKER_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.BREWING_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.BEACON_ACCESS) ||
               hasPermission(player, islandId, IslandPermission.ENDER_CHEST_ACCESS);
    }
    
    /**
     * Grant a specific permission to a player on an island
     */
    public void grantPermission(String islandId, UUID playerUUID, IslandPermission permission) {
        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island != null) {
            island.addCustomPermission(playerUUID, permission);
        }
    }
    
    /**
     * Revoke a specific permission from a player on an island
     */
    public void revokePermission(String islandId, UUID playerUUID, IslandPermission permission) {
        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island != null) {
            island.removeCustomPermission(playerUUID, permission);
        }
    }
    
    /**
     * Get all permissions for a player on an island (role + custom)
     */
    public Set<IslandPermission> getPlayerPermissions(Player player, String islandId) {
        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island == null) {
            return EnumSet.noneOf(IslandPermission.class);
        }
        
        UUID playerUUID = player.getUniqueId();
        Island.CoopRole role = island.getCoopRole(playerUUID);
        
        Set<IslandPermission> permissions = EnumSet.noneOf(IslandPermission.class);
        
        // Add role-based permissions
        Set<IslandPermission> rolePermissions = defaultRolePermissions.get(role);
        if (rolePermissions != null) {
            permissions.addAll(rolePermissions);
        }
        
        // Add custom player permissions
        Set<IslandPermission> customPerms = island.getCustomPermissions(playerUUID);
        permissions.addAll(customPerms);
        
        // Add server-wide permissions
        for (IslandPermission permission : IslandPermission.values()) {
            if (player.hasPermission(permission.getFullPermissionNode())) {
                permissions.add(permission);
            }
        }
        
        return permissions;
    }
    
    /**
     * Get default permissions for a specific role
     */
    public Set<IslandPermission> getRolePermissions(Island.CoopRole role) {
        return new HashSet<>(defaultRolePermissions.getOrDefault(role, EnumSet.noneOf(IslandPermission.class)));
    }
    
    /**
     * Update default permissions for a role
     */
    public void setRolePermissions(Island.CoopRole role, Set<IslandPermission> permissions) {
        defaultRolePermissions.put(role, new HashSet<>(permissions));
    }
    
    /**
     * Check if a player is restricted (VISITOR role)
     */
    public boolean isPlayerRestricted(Player player, String islandId) {
        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island == null) {
            return false;
        }
        
        UUID playerUUID = player.getUniqueId();
        Island.CoopRole role = island.getCoopRole(playerUUID);
        
        // Check for admin bypass
        if (player.hasPermission("skyeblock.admin.bypass")) {
            return false;
        }
        
        // Only VISITOR role is restricted by default
        return role == Island.CoopRole.VISITOR;
    }
    
    /**
     * Get permission manager configuration data for saving
     * Note: Player permissions are now stored in Island objects and saved via IslandDataManager
     */
    public Map<String, Object> getConfigurationData() {
        Map<String, Object> data = new HashMap<>();
        // Custom permissions are now stored in Island objects
        // No additional data needs to be saved here
        return data;
    }
    
    /**
     * Load permission manager configuration data
     * Note: Player permissions are now loaded from Island objects via IslandDataManager
     */
    public void loadConfigurationData(Map<String, Object> data) {
        // Custom permissions are now loaded from Island objects
        // No additional data needs to be loaded here
    }
    
    /**
     * Clean up permissions for removed islands
     * Note: Permissions are automatically cleaned up when Island objects are removed
     */
    public void cleanupIslandPermissions(String islandId) {
        // Permissions are now stored in Island objects and automatically cleaned up
    }
}
