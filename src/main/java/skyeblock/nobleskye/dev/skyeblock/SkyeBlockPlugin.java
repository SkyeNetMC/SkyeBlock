package skyeblock.nobleskye.dev.skyeblock;

import skyeblock.nobleskye.dev.skyeblock.commands.IslandCommand;
import skyeblock.nobleskye.dev.skyeblock.commands.HubCommand;
import skyeblock.nobleskye.dev.skyeblock.managers.CustomSchematicManager;
import skyeblock.nobleskye.dev.skyeblock.managers.IslandManager;
import skyeblock.nobleskye.dev.skyeblock.managers.SchematicManager;
import skyeblock.nobleskye.dev.skyeblock.managers.WorldManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SkyeBlockPlugin extends JavaPlugin {

    private IslandManager islandManager;
    private SchematicManager schematicManager;
    private CustomSchematicManager customSchematicManager;
    private WorldManager worldManager;
    private MiniMessage miniMessage;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize MiniMessage
        miniMessage = MiniMessage.miniMessage();
        
        // Initialize managers
        this.worldManager = new WorldManager(this);
        this.schematicManager = new SchematicManager(this);
        this.customSchematicManager = new CustomSchematicManager(this);
        this.islandManager = new IslandManager(this);
        
        // Initialize world
        worldManager.initializeWorld();
        
        // Register commands
        registerCommands();
        
        // Log enable message
        getComponentLogger().info(Component.text("SkyeBlock plugin enabled!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        getComponentLogger().info(Component.text("SkyeBlock plugin disabled!", NamedTextColor.RED));
    }

    private void registerCommands() {
        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("hub").setExecutor(new HubCommand(this));
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public SchematicManager getSchematicManager() {
        return schematicManager;
    }

    public CustomSchematicManager getCustomSchematicManager() {
        return customSchematicManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public String getMessage(String key) {
        String prefix = getConfig().getString("messages.prefix", "<dark_gray>[<gold>SkyeBlock</gold>]</dark_gray> ");
        String message = getConfig().getString("messages." + key, key);
        return prefix + message;
    }
    
    public Component getMessageComponent(String key) {
        String message = getMessage(key);
        return miniMessage.deserialize(message);
    }
    
    public void sendMessage(Player player, String key) {
        player.sendMessage(getMessageComponent(key));
    }
}
