package skyeblock.nobleskye.dev.skyeblock.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import skyeblock.nobleskye.dev.skyeblock.permissions.IslandPermission;

import java.util.*;

public class Island {
    private final UUID ownerUUID;
    private final String islandType;
    private final Location location;
    private final String islandId;
    
    // New features
    private String displayTitle;
    private String displayDescription;
    private ItemStack displayIcon;
    private boolean locked;
    private boolean adventureModeForVisitors;
    private Location homeLocation;
    private Location visitLocation;
    
    // Coop system
    private final Map<UUID, CoopRole> coopMembers;
    private final Set<UUID> pendingInvites;
    
    // Voting system
    private final Map<UUID, Long> votes; // Player UUID -> vote timestamp
    private long lastOnlineTime;
    
    // Permission system
    private final Map<UUID, Set<IslandPermission>> customPlayerPermissions; // Player UUID -> Set of permissions

    public Island(UUID ownerUUID, String islandType, Location location) {
        this.ownerUUID = ownerUUID;
        this.islandType = islandType;
        this.location = location;
        this.islandId = "island-" + islandType + "-" + ownerUUID.toString();
        
        // Initialize new features with defaults
        this.displayTitle = null; // Will use player name if null
        this.displayDescription = null;
        this.displayIcon = null; // Will use default based on island type
        this.locked = false;
        this.adventureModeForVisitors = true; // Default to adventure mode for visitors
        this.homeLocation = null; // Will use spawn location if null
        this.visitLocation = null; // Will use home location if null
        
        this.coopMembers = new HashMap<>();
        this.pendingInvites = new HashSet<>();
        this.votes = new HashMap<>();
        this.customPlayerPermissions = new HashMap<>();
        this.lastOnlineTime = System.currentTimeMillis();
    }

    // Existing getters
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public String getIslandType() {
        return islandType;
    }

    public Location getLocation() {
        return location;
    }

    public String getIslandId() {
        return islandId;
    }

    public Location getSpawnLocation() {
        // Return a safe spawn location on the island (slightly above the location)
        return location.clone().add(0, 1, 0);
    }

    public World getWorld() {
        return location.getWorld();
    }
    
    // New getters and setters
    public String getDisplayTitle() {
        return displayTitle;
    }
    
    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }
    
    public String getDisplayDescription() {
        return displayDescription;
    }
    
    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }
    
    public ItemStack getDisplayIcon() {
        return displayIcon;
    }
    
    public void setDisplayIcon(ItemStack displayIcon) {
        this.displayIcon = displayIcon;
    }
    
    public boolean isLocked() {
        return locked;
    }
    
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
    
    public boolean isAdventureModeForVisitors() {
        return adventureModeForVisitors;
    }
    
    public void setAdventureModeForVisitors(boolean adventureModeForVisitors) {
        this.adventureModeForVisitors = adventureModeForVisitors;
    }
    
    public Location getHomeLocation() {
        return homeLocation != null ? homeLocation : getSpawnLocation();
    }
    
    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }
    
    public Location getVisitLocation() {
        return visitLocation != null ? visitLocation : getHomeLocation();
    }
    
    public void setVisitLocation(Location visitLocation) {
        this.visitLocation = visitLocation;
    }
    
    // Coop system methods
    public Map<UUID, CoopRole> getCoopMembers() {
        return coopMembers;
    }
    
    public void addCoopMember(UUID playerUUID, CoopRole role) {
        coopMembers.put(playerUUID, role);
        pendingInvites.remove(playerUUID);
    }
    
    public void removeCoopMember(UUID playerUUID) {
        coopMembers.remove(playerUUID);
    }
    
    public CoopRole getCoopRole(UUID playerUUID) {
        if (playerUUID.equals(ownerUUID)) {
            return CoopRole.OWNER;
        }
        return coopMembers.getOrDefault(playerUUID, CoopRole.VISITOR);
    }
    
    public boolean hasCoopAccess(UUID playerUUID) {
        return playerUUID.equals(ownerUUID) || coopMembers.containsKey(playerUUID);
    }
    
    public Set<UUID> getPendingInvites() {
        return pendingInvites;
    }
    
    public void addPendingInvite(UUID playerUUID) {
        pendingInvites.add(playerUUID);
    }
    
    public void removePendingInvite(UUID playerUUID) {
        pendingInvites.remove(playerUUID);
    }
    
    // Voting system methods
    public void addVote(UUID playerUUID) {
        votes.put(playerUUID, System.currentTimeMillis());
    }
    
    public void removeVote(UUID playerUUID) {
        votes.remove(playerUUID);
    }
    
    public boolean hasVoted(UUID playerUUID) {
        return votes.containsKey(playerUUID);
    }
    
    public int getVoteCount() {
        // Clean up expired votes (older than 7 days)
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        votes.entrySet().removeIf(entry -> entry.getValue() < sevenDaysAgo);
        return votes.size();
    }
    
    public boolean canVote(UUID playerUUID) {
        if (!votes.containsKey(playerUUID)) {
            return true;
        }
        
        long lastVote = votes.get(playerUUID);
        long twentyThreeHoursAgo = System.currentTimeMillis() - (23 * 60 * 60 * 1000L);
        return lastVote < twentyThreeHoursAgo;
    }
    
    public long getLastOnlineTime() {
        return lastOnlineTime;
    }
    
    public void updateLastOnlineTime() {
        this.lastOnlineTime = System.currentTimeMillis();
    }
    
    // Helper methods
    public boolean canManageSettings(UUID playerUUID) {
        CoopRole role = getCoopRole(playerUUID);
        return role.getLevel() >= CoopRole.ADMIN.getLevel();
    }
    
    public boolean canManageCoop(UUID playerUUID) {
        CoopRole role = getCoopRole(playerUUID);
        return role.getLevel() >= CoopRole.CO_OWNER.getLevel();
    }
    
    public void setCoopRole(UUID playerUUID, CoopRole role) {
        if (role == CoopRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role through setCoopRole");
        }
        coopMembers.put(playerUUID, role);
    }
    
    public UUID getOwner() {
        return ownerUUID;
    }
    
    public void setTitle(String title) {
        this.displayTitle = title;
    }
    
    public void setDescription(String description) {
        this.displayDescription = description;
    }
    
    public void setIcon(ItemStack icon) {
        this.displayIcon = icon;
    }
    
    public void setCustomHomeLocation(Location location) {
        this.homeLocation = location;
    }
    
    public void setCustomVisitLocation(Location location) {
        this.visitLocation = location;
    }
    
    public Map<UUID, Long> getVotes() {
        return votes;
    }
    
    public long getLastVote(UUID playerUUID) {
        return votes.getOrDefault(playerUUID, 0L);
    }
    
    public ItemStack getDefaultDisplayIcon() {
        switch (islandType.toLowerCase()) {
            case "desert":
            case "desert island":
                return new ItemStack(Material.SAND);
            case "nether":
            case "nether island":
                return new ItemStack(Material.NETHERRACK);
            default:
                return new ItemStack(Material.GRASS_BLOCK);
        }
    }
    
    // Permission system methods
    public Map<UUID, Set<IslandPermission>> getCustomPlayerPermissions() {
        return customPlayerPermissions;
    }
    
    public Set<IslandPermission> getCustomPermissions(UUID playerUUID) {
        return customPlayerPermissions.getOrDefault(playerUUID, new HashSet<>());
    }
    
    public void addCustomPermission(UUID playerUUID, IslandPermission permission) {
        customPlayerPermissions.computeIfAbsent(playerUUID, k -> new HashSet<>()).add(permission);
    }
    
    public void removeCustomPermission(UUID playerUUID, IslandPermission permission) {
        Set<IslandPermission> permissions = customPlayerPermissions.get(playerUUID);
        if (permissions != null) {
            permissions.remove(permission);
            if (permissions.isEmpty()) {
                customPlayerPermissions.remove(playerUUID);
            }
        }
    }
    
    public void clearCustomPermissions(UUID playerUUID) {
        customPlayerPermissions.remove(playerUUID);
    }
    
    public enum CoopRole {
        VISITOR(0, "Visitor"),
        MEMBER(1, "Member"),
        ADMIN(2, "Admin"),
        CO_OWNER(3, "Co-Owner"),
        OWNER(4, "Owner");
        
        private final int level;
        private final String displayName;
        
        CoopRole(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static CoopRole fromLevel(int level) {
            for (CoopRole role : values()) {
                if (role.level == level) {
                    return role;
                }
            }
            return VISITOR;
        }
        
        public static CoopRole fromString(String name) {
            for (CoopRole role : values()) {
                if (role.name().equalsIgnoreCase(name) || role.displayName.equalsIgnoreCase(name)) {
                    return role;
                }
            }
            try {
                int level = Integer.parseInt(name);
                return fromLevel(level);
            } catch (NumberFormatException e) {
                return VISITOR;
            }
        }
        
        public boolean canManage(CoopRole targetRole) {
            return this.level > targetRole.level;
        }
    }
}
