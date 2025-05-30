package skyeblock.nobleskye.dev.skyeblock.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

/**
 * Server brand modification implementation using built-in Spigot methods
 * instead of trying to send custom plugin messages
 */
public class SpigotBrandModifier {
    private final SkyeBlockPlugin plugin;
    private final String customBrand;
    private boolean successful = false;

    public SpigotBrandModifier(SkyeBlockPlugin plugin, String customBrand) {
        this.plugin = plugin;
        this.customBrand = customBrand;
        
        try {
            // Try to directly access and modify the Spigot server brand
            modifyBrandUsingSpigotMethods();
            
            // Schedule additional attempts for when the server is fully loaded
            Bukkit.getScheduler().runTaskLater(plugin, this::modifyBrandUsingSpigotMethods, 100L);
            
            // Schedule periodic updates to ensure the brand stays modified
            Bukkit.getScheduler().runTaskTimer(plugin, this::modifyBrandUsingSpigotMethods, 1200L, 12000L); // Every 10 minutes
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to initialize SpigotBrandModifier", e);
        }
    }
    
    /**
     * Modify the server brand using Spigot-specific methods
     */
    private void modifyBrandUsingSpigotMethods() {
        try {
            // Method 1: Try to use the Spigot server's setBrand method
            Object server = Bukkit.getServer();
            Object craftServer = server;
            
            // Get the CraftServer instance
            if (craftServer != null) {
                // Try to get the console from CraftServer
                Field consoleField = craftServer.getClass().getDeclaredField("console");
                consoleField.setAccessible(true);
                Object minecraftServer = consoleField.get(craftServer);
                
                if (minecraftServer != null) {
                    // Try direct setBrand method
                    try {
                        Method setBrandMethod = minecraftServer.getClass().getMethod("setBrand", String.class);
                        setBrandMethod.setAccessible(true);
                        setBrandMethod.invoke(minecraftServer, customBrand);
                        plugin.getLogger().info("Successfully set server brand using setBrand method: " + customBrand);
                        successful = true;
                    } catch (NoSuchMethodException e) {
                        // Try direct field modification
                        try {
                            Field brandField = findField(minecraftServer.getClass(), "serverModName", "modName", "serverBrand");
                            if (brandField != null) {
                                brandField.setAccessible(true);
                                brandField.set(minecraftServer, customBrand);
                                plugin.getLogger().info("Successfully set server brand using field modification: " + customBrand);
                                successful = true;
                            }
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Failed to set brand via field modification: " + ex.getMessage());
                        }
                    }
                    
                    // If we've made it this far, also try to modify any key server info objects
                    try {
                        Method getServerModNameMethod = minecraftServer.getClass().getMethod("getServerModName");
                        getServerModNameMethod.setAccessible(true);
                        String currentBrand = (String) getServerModNameMethod.invoke(minecraftServer);
                        plugin.getLogger().info("Current server brand: " + currentBrand);
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to modify server brand using Spigot methods", e);
        }
    }
    
    /**
     * Update the brand for a specific player
     */
    public void updatePlayerBrand(Player player) {
        if (!successful) return;
        
        try {
            // Use reflection to get the player's connection
            Object entityPlayer = getHandle(player);
            if (entityPlayer != null) {
                Object playerConnection = getPlayerConnection(entityPlayer);
                if (playerConnection != null) {
                    // Try to find the sendPacket method
                    Method sendPacket = findMethod(playerConnection.getClass(), "sendPacket", "a");
                    if (sendPacket != null) {
                        // Create a brand packet
                        Object packet = createBrandPacket();
                        if (packet != null) {
                            sendPacket.invoke(playerConnection, packet);
                            plugin.getLogger().info("Sent brand packet to player: " + player.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update player brand for " + player.getName(), e);
        }
    }
    
    /**
     * Create a brand packet using reflection
     */
    private Object createBrandPacket() {
        try {
            // Get the MinecraftKey class for the brand channel
            Class<?> minecraftKeyClass = Class.forName("net.minecraft.resources.MinecraftKey");
            Object minecraftKey = minecraftKeyClass.getConstructor(String.class).newInstance("brand");
            
            // Create a buffer to write our brand data
            Class<?> unpooledClass = Class.forName("io.netty.buffer.Unpooled");
            Object byteBuf = unpooledClass.getMethod("buffer").invoke(null);
            
            // Create friendly byte buf and packet serializer
            Class<?> friendlyByteBufClass = Class.forName("net.minecraft.network.FriendlyByteBuf");
            Object friendlyByteBuf = friendlyByteBufClass.getConstructor(Class.forName("io.netty.buffer.ByteBuf")).newInstance(byteBuf);
            
            // Write the custom brand to the buffer
            Method writeUtf = friendlyByteBufClass.getMethod("a", String.class);
            writeUtf.invoke(friendlyByteBuf, customBrand);
            
            // Create the packet
            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.PacketPlayOutCustomPayload");
            Object packet = packetClass.getConstructor(minecraftKeyClass, friendlyByteBufClass)
                .newInstance(minecraftKey, friendlyByteBuf);
            
            return packet;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to create brand packet: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get the handle of a CraftBukkit entity
     */
    private Object getHandle(Object object) {
        try {
            Method getHandle = object.getClass().getMethod("getHandle");
            return getHandle.invoke(object);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get the player connection from an EntityPlayer
     */
    private Object getPlayerConnection(Object entityPlayer) {
        try {
            // Try common field names for player connection
            Field field = findField(entityPlayer.getClass(), "playerConnection", "b", "connection");
            if (field != null) {
                field.setAccessible(true);
                return field.get(entityPlayer);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Find a field by any of its possible names
     */
    private Field findField(Class<?> clazz, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                if (field != null) {
                    return field;
                }
            } catch (NoSuchFieldException e) {
                // Try the next name
            }
        }
        
        // If field not found in this class, check superclasses
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            return findField(superClass, fieldNames);
        }
        
        return null;
    }
    
    /**
     * Find a method by any of its possible names
     */
    private Method findMethod(Class<?> clazz, String... methodNames) {
        for (String methodName : methodNames) {
            try {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName)) {
                        method.setAccessible(true);
                        return method;
                    }
                }
            } catch (Exception e) {
                // Try the next name
            }
        }
        
        // If method not found in this class, check superclasses
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            return findMethod(superClass, methodNames);
        }
        
        return null;
    }
    
    /**
     * Check if the brand modification was successful
     */
    public boolean isSuccessful() {
        return successful;
    }
}
