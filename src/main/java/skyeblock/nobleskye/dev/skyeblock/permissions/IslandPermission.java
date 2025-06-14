package skyeblock.nobleskye.dev.skyeblock.permissions;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;

/**
 * Comprehensive permission system for island interactions
 * Similar to Flan plugin but integrated with the existing role-based system
 */
public enum IslandPermission {
    
    // Container Access Permissions
    CONTAINER_ACCESS("container.access", "Access containers (chests, furnaces, etc.)", PermissionCategory.CONTAINER),
    CHEST_ACCESS("container.chest", "Access chests and trapped chests", PermissionCategory.CONTAINER),
    FURNACE_ACCESS("container.furnace", "Access furnaces, blast furnaces, and smokers", PermissionCategory.CONTAINER),
    HOPPER_ACCESS("container.hopper", "Access hoppers", PermissionCategory.CONTAINER),
    DISPENSER_ACCESS("container.dispenser", "Access dispensers and droppers", PermissionCategory.CONTAINER),
    BARREL_ACCESS("container.barrel", "Access barrels", PermissionCategory.CONTAINER),
    SHULKER_ACCESS("container.shulker", "Access shulker boxes", PermissionCategory.CONTAINER),
    BREWING_ACCESS("container.brewing", "Access brewing stands", PermissionCategory.CONTAINER),
    BEACON_ACCESS("container.beacon", "Access beacons", PermissionCategory.CONTAINER),
    ENDER_CHEST_ACCESS("container.enderchest", "Access ender chests", PermissionCategory.CONTAINER),
    
    // Block Interaction Permissions
    BLOCK_BREAK("block.break", "Break blocks", PermissionCategory.BLOCK),
    BLOCK_PLACE("block.place", "Place blocks", PermissionCategory.BLOCK),
    BUTTON_USE("block.button", "Use buttons and pressure plates", PermissionCategory.BLOCK),
    LEVER_USE("block.lever", "Use levers", PermissionCategory.BLOCK),
    DOOR_USE("block.door", "Use doors and trapdoors", PermissionCategory.BLOCK),
    CRAFTING_TABLE_USE("block.crafting", "Use crafting tables", PermissionCategory.BLOCK),
    ENCHANTING_TABLE_USE("block.enchanting", "Use enchanting tables", PermissionCategory.BLOCK),
    ANVIL_USE("block.anvil", "Use anvils", PermissionCategory.BLOCK),
    BED_USE("block.bed", "Use beds", PermissionCategory.BLOCK),
    
    // Redstone Permissions
    REDSTONE_INTERACT("redstone.interact", "Interact with redstone devices", PermissionCategory.REDSTONE),
    REDSTONE_BUILD("redstone.build", "Build redstone contraptions", PermissionCategory.REDSTONE),
    COMPARATOR_USE("redstone.comparator", "Use comparators", PermissionCategory.REDSTONE),
    REPEATER_USE("redstone.repeater", "Use repeaters", PermissionCategory.REDSTONE),
    PISTON_USE("redstone.piston", "Use pistons", PermissionCategory.REDSTONE),
    
    // Entity Interaction Permissions
    ENTITY_INTERACT("entity.interact", "Interact with entities", PermissionCategory.ENTITY),
    ANIMAL_INTERACT("entity.animal", "Interact with animals", PermissionCategory.ENTITY),
    VILLAGER_TRADE("entity.villager", "Trade with villagers", PermissionCategory.ENTITY),
    ITEM_FRAME_USE("entity.itemframe", "Use item frames", PermissionCategory.ENTITY),
    ARMOR_STAND_USE("entity.armorstand", "Use armor stands", PermissionCategory.ENTITY),
    VEHICLE_USE("entity.vehicle", "Use vehicles (boats, minecarts)", PermissionCategory.ENTITY),
    
    // Item Management Permissions
    ITEM_PICKUP("item.pickup", "Pick up items", PermissionCategory.ITEM),
    ITEM_DROP("item.drop", "Drop items", PermissionCategory.ITEM),
    INVENTORY_MANAGEMENT("item.inventory", "Manage inventory", PermissionCategory.ITEM),
    
    // Administrative Permissions
    GAMEMODE_CHANGE("admin.gamemode", "Change game mode", PermissionCategory.ADMIN),
    COMMAND_USE("admin.commands", "Use commands", PermissionCategory.ADMIN),
    BYPASS_PROTECTION("admin.bypass", "Bypass all protection", PermissionCategory.ADMIN);
    
    private final String node;
    private final String description;
    private final PermissionCategory category;
    
    IslandPermission(String node, String description, PermissionCategory category) {
        this.node = node;
        this.description = description;
        this.category = category;
    }
    
    public String getNode() {
        return node;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDisplayName() {
        // Convert the description to a display name (first letter uppercase, rest as is)
        return description;
    }
    
    public PermissionCategory getCategory() {
        return category;
    }
    
    public String getFullPermissionNode() {
        return "skyeblock.island.permission." + node;
    }
    
    /**
     * Get permission for a specific material container type
     */
    public static IslandPermission getContainerPermission(Material material) {
        switch (material) {
            case CHEST:
            case TRAPPED_CHEST:
                return CHEST_ACCESS;
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
                return FURNACE_ACCESS;
            case HOPPER:
                return HOPPER_ACCESS;
            case DISPENSER:
            case DROPPER:
                return DISPENSER_ACCESS;
            case BARREL:
                return BARREL_ACCESS;
            case SHULKER_BOX:
            case WHITE_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case RED_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
                return SHULKER_ACCESS;
            case BREWING_STAND:
                return BREWING_ACCESS;
            case BEACON:
                return BEACON_ACCESS;
            case ENDER_CHEST:
                return ENDER_CHEST_ACCESS;
            default:
                return CONTAINER_ACCESS;
        }
    }
    
    /**
     * Get permission for a specific inventory type
     */
    public static IslandPermission getInventoryPermission(InventoryType type) {
        switch (type) {
            case CHEST:
            case ENDER_CHEST:
                return CHEST_ACCESS;
            case FURNACE:
            case BLAST_FURNACE:
            case SMOKER:
                return FURNACE_ACCESS;
            case HOPPER:
                return HOPPER_ACCESS;
            case DISPENSER:
            case DROPPER:
                return DISPENSER_ACCESS;
            case BARREL:
                return BARREL_ACCESS;
            case SHULKER_BOX:
                return SHULKER_ACCESS;
            case BREWING:
                return BREWING_ACCESS;
            case BEACON:
                return BEACON_ACCESS;
            default:
                return CONTAINER_ACCESS;
        }
    }
    
    public enum PermissionCategory {
        CONTAINER("Container Access", "Permissions for accessing various containers"),
        BLOCK("Block Interaction", "Permissions for interacting with blocks"),
        REDSTONE("Redstone", "Permissions for redstone devices"),
        ENTITY("Entity Interaction", "Permissions for interacting with entities"),
        ITEM("Item Management", "Permissions for item handling"),
        ADMIN("Administrative", "Administrative permissions");
        
        private final String displayName;
        private final String description;
        
        PermissionCategory(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
