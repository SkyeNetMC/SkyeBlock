package skyeblock.nobleskye.dev.skyeblock;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * Alternative server brand changer that uses Bukkit's plugin messaging system
 */
public class ServerBrandChanger implements PluginMessageListener {
    private final SkyeBlockPlugin plugin;
    private final String customBrand;
    private final String BRAND_CHANNEL = "minecraft:brand";
    
    public ServerBrandChanger(SkyeBlockPlugin plugin, String customBrand) {
        this.plugin = plugin;
        this.customBrand = customBrand;
        
        try {
            // Register the plugin messaging channel
            try {
                Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, BRAND_CHANNEL);
                Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, BRAND_CHANNEL, this);
                plugin.getLogger().info("Successfully registered brand channel: " + BRAND_CHANNEL);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register plugin messaging channels: " + e.getMessage());
            }
            
            // Set server brand in server.properties (modifies Bukkit.getName())
            try {
                java.lang.reflect.Field consoleField = Bukkit.getServer().getClass().getDeclaredField("console");
                consoleField.setAccessible(true);
                Object console = consoleField.get(Bukkit.getServer());
                
                java.lang.reflect.Field brandField = console.getClass().getDeclaredField("serverName");
                brandField.setAccessible(true);
                brandField.set(console, customBrand);
                
                plugin.getLogger().info("Server name successfully set to: " + customBrand);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to set server brand in server properties", e);
            }
            
            // Send brand to all online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerBrand(player);
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize ServerBrandChanger", e);
        }
    }
    
    /**
     * Updates the brand for a specific player
     */
    public void updatePlayerBrand(Player player) {
        try {
            // Create a data output stream
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            byte[] brandBytes = customBrand.getBytes(StandardCharsets.UTF_8);
            
            // Format: length byte + brand bytes
            out.writeByte(brandBytes.length);
            out.write(brandBytes);
            
            // Send the custom brand to the player
            player.sendPluginMessage(plugin, BRAND_CHANNEL, out.toByteArray());
            
            plugin.getLogger().info("Sent brand '" + customBrand + "' to player: " + player.getName());
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to update brand for player: " + player.getName(), e);
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Not needed for outgoing messages
    }
}
