package skyeblock.nobleskye.dev.skyeblock.models;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

/**
 * Represents a warp point with GUI configuration
 */
public class Warp {
    private final String name;
    private final String displayName;
    private final Location location;
    private final String permission;
    private final Material material;
    private final int slot;
    private final List<String> lore;
    private final double cost;
    private final boolean enabled;
    
    public Warp(String name, String displayName, Location location, String permission, 
                Material material, int slot, List<String> lore, double cost, boolean enabled) {
        this.name = name;
        this.displayName = displayName;
        this.location = location;
        this.permission = permission;
        this.material = material;
        this.slot = slot;
        this.lore = lore;
        this.cost = cost;
        this.enabled = enabled;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public int getSlot() {
        return slot;
    }
    
    public List<String> getLore() {
        return lore;
    }
    
    public double getCost() {
        return cost;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public String toString() {
        return "Warp{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", location=" + location +
                ", permission='" + permission + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}
