package skyeblock.nobleskye.dev.skyeblock.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Listener to modify the server brand displayed to clients
 */
public class ServerBrandListener implements Listener {
    private final SkyeBlockPlugin plugin;
    private final String customBrand;

    public ServerBrandListener(SkyeBlockPlugin plugin, String customBrand) {
        this.plugin = plugin;
        this.customBrand = customBrand;
        
        // Apply server brand modification when the plugin starts
        applyServerBrandModification();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Send the custom brand to the player when they join
        updatePlayerBrand(event.getPlayer());
    }
    
    /**
     * Updates the server brand for a specific player
     */
    public void updatePlayerBrand(Player player) {
        try {
            // Get the player's connection and server info
            Object entityPlayer = getHandle(player);
            Object playerConnection = getField(entityPlayer.getClass(), entityPlayer, "b");
            
            // Create the brand packet
            Object serverBrandPacket = createServerBrandPacket(customBrand);
            
            // Send the packet
            Method sendPacket = playerConnection.getClass().getMethod("a", getNMSClass("network.protocol.Packet"));
            sendPacket.invoke(playerConnection, serverBrandPacket);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update server brand for player " + player.getName(), e);
        }
    }
    
    /**
     * Creates a server brand packet with the custom brand name
     */
    private Object createServerBrandPacket(String brand) throws Exception {
        Object minecraftKey = getNMSClass("resources.MinecraftKey").getConstructor(String.class).newInstance("brand");
        
        // Create a PacketDataSerializer with our brand data
        Object byteBuf = Class.forName("io.netty.buffer.Unpooled").getMethod("buffer").invoke(null);
        Object friendlyByteBuf = getNMSClass("network.FriendlyByteBuf").getConstructor(Class.forName("io.netty.buffer.ByteBuf")).newInstance(byteBuf);
        Object packetDataSerializer = getNMSClass("network.PacketDataSerializer").getConstructor(getNMSClass("network.FriendlyByteBuf")).newInstance(friendlyByteBuf);
        
        // Write the brand
        Method writeUtf = packetDataSerializer.getClass().getMethod("a", String.class);
        writeUtf.invoke(packetDataSerializer, brand);
        
        // Create the packet
        Object packet = getNMSClass("network.protocol.game.PacketPlayOutCustomPayload")
                .getConstructor(getNMSClass("resources.MinecraftKey"), getNMSClass("network.PacketDataSerializer"))
                .newInstance(minecraftKey, packetDataSerializer);
        
        return packet;
    }
    
    /**
     * Modifies the server brand at the root level
     */
    private void applyServerBrandModification() {
        try {
            Object minecraftServer = getMinecraftServer();
            if (minecraftServer != null) {
                // Try to modify the server brand field
                setField(minecraftServer.getClass(), minecraftServer, "serverModName", customBrand);
                
                plugin.getLogger().info("Successfully modified server brand to: " + customBrand);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to modify server brand at root level", e);
        }
    }

    /**
     * Gets the Minecraft server instance
     */
    private Object getMinecraftServer() {
        try {
            Method getServerMethod = Bukkit.getServer().getClass().getMethod("getServer");
            return getServerMethod.invoke(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get MinecraftServer instance", e);
            return null;
        }
    }

    /**
     * Gets the NMS class
     */
    private Class<?> getNMSClass(String name) throws ClassNotFoundException {
        // No need to use version in modern Minecraft - direct net.minecraft paths
        return Class.forName("net.minecraft." + name);
    }
    
    /**
     * Gets the handle of a CraftBukkit entity
     */
    private Object getHandle(Object object) {
        try {
            Method getHandle = object.getClass().getMethod("getHandle");
            return getHandle.invoke(object);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get handle for object: " + object, e);
            return null;
        }
    }
    
    /**
     * Gets the value of a field from an object
     */
    private Object getField(Class<?> clazz, Object object, String fieldName) {
        try {
            // First try directly
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (NoSuchFieldException e) {
                // If not found, search through all fields
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getName().equals(fieldName)) {
                        return field.get(object);
                    }
                }
                
                // If still not found and has superclass, try superclass
                if (clazz.getSuperclass() != null) {
                    return getField(clazz.getSuperclass(), object, fieldName);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get field: " + fieldName, e);
        }
        return null;
    }
    
    /**
     * Sets the value of a field on an object
     */
    private void setField(Class<?> clazz, Object object, String fieldName, Object value) {
        try {
            // Try direct field access first
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(object, value);
                return;
            } catch (NoSuchFieldException e) {
                // If not found, search through all fields
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.getName().equals(fieldName)) {
                        field.set(object, value);
                        return;
                    }
                }
                
                // If still not found and has superclass, try superclass
                if (clazz.getSuperclass() != null) {
                    setField(clazz.getSuperclass(), object, fieldName, value);
                    return;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to set field: " + fieldName, e);
        }
    }
}
