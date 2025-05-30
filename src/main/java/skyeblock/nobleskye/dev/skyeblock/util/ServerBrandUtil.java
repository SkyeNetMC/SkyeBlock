package skyeblock.nobleskye.dev.skyeblock.util;

import org.bukkit.Bukkit;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Utility class for modifying the server brand
 */
public class ServerBrandUtil {
    
    /**
     * Attempt to modify the server brand using multiple approaches
     * 
     * @param plugin The main plugin instance
     * @param customBrand The desired custom brand
     */
    public static void modifyServerBrand(SkyeBlockPlugin plugin, String customBrand) {
        try {
            // Try to modify the server.properties file
            modifyServerProperties(plugin, customBrand);
            
            // Try to modify the server brand field directly
            modifyServerBrandField(plugin, customBrand);
            
            // Try to set server name via console command
            setServerNameViaConsole(plugin, customBrand);
            
            plugin.getLogger().info("Applied all available methods to set server brand to: " + customBrand);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to modify server brand: " + e.getMessage(), e);
        }
    }
    
    /**
     * Modify the server.properties file to include the custom brand
     */
    private static void modifyServerProperties(SkyeBlockPlugin plugin, String customBrand) {
        try {
            File serverPropertiesFile = new File("server.properties");
            if (!serverPropertiesFile.exists()) {
                plugin.getLogger().warning("server.properties file not found, skipping property modification");
                return;
            }
            
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(serverPropertiesFile)) {
                properties.load(fis);
            }
            
            // Set or update the brand property (uses a custom property)
            properties.setProperty("server-brand", customBrand);
            properties.setProperty("motd", "A " + customBrand + " Server");
            
            // Save the properties file
            try (FileOutputStream fos = new FileOutputStream(serverPropertiesFile)) {
                properties.store(fos, "Modified by SkyeBlock plugin to set custom server brand");
            }
            
            plugin.getLogger().info("Updated server.properties file with custom brand: " + customBrand);
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update server.properties file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Try to modify the server brand field directly using reflection
     */
    private static void modifyServerBrandField(SkyeBlockPlugin plugin, String customBrand) {
        try {
            // Try to get the server instance
            Method getServerMethod = Bukkit.getServer().getClass().getMethod("getServer");
            Object minecraftServer = getServerMethod.invoke(Bukkit.getServer());
            
            // Try multiple common field names
            String[] possibleFieldNames = {
                "serverModName", "modName", "name", "serverName", "brand", "serverBrand", "serverModNameFull"
            };
            
            boolean success = false;
            
            // First try Spigot's easy method
            try {
                Method setBrandMethod = minecraftServer.getClass().getMethod("setServerBrand", String.class);
                setBrandMethod.invoke(minecraftServer, customBrand);
                success = true;
                plugin.getLogger().info("Used Spigot's setServerBrand method to set brand: " + customBrand);
            } catch (NoSuchMethodException e) {
                // Continue to try other methods
            }
            
            // Try multiple field names if the direct method didn't work
            if (!success) {
                for (String fieldName : possibleFieldNames) {
                    try {
                        Field field = findField(minecraftServer.getClass(), fieldName);
                        if (field != null) {
                            field.setAccessible(true);
                            field.set(minecraftServer, customBrand);
                            plugin.getLogger().info("Modified server brand via field '" + fieldName + "': " + customBrand);
                            success = true;
                            break;
                        }
                    } catch (Exception e) {
                        // Continue trying other fields
                    }
                }
            }
            
            if (!success) {
                plugin.getLogger().warning("Could not find a suitable field to modify server brand");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to modify server brand field: " + e.getMessage(), e);
        }
    }
    
    /**
     * Try to set the server name using console commands
     */
    private static void setServerNameViaConsole(SkyeBlockPlugin plugin, String customBrand) {
        try {
            // Try to run the command to set server name if available
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "setservername " + customBrand);
            plugin.getLogger().info("Executed console command to set server name: " + customBrand);
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to dispatch console command: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find a field by name in a class or its superclasses
     */
    private static Field findField(Class<?> clazz, String fieldName) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return null;
    }
}
