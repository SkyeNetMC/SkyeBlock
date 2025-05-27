package skyeblock.nobleskye.dev.skyeblock.models;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class Island {
    private final UUID ownerUUID;
    private final String islandType;
    private final Location location;
    private final String islandId;

    public Island(UUID ownerUUID, String islandType, Location location) {
        this.ownerUUID = ownerUUID;
        this.islandType = islandType;
        this.location = location;
        this.islandId = "island-" + islandType + "-" + ownerUUID.toString();
    }

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
}
