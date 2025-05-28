package skyeblock.nobleskye.dev.skyeblock;

import skyeblock.nobleskye.dev.skyeblock.commands.IslandCommand;
import skyeblock.nobleskye.dev.skyeblock.commands.HubCommand;
import skyeblock.nobleskye.dev.skyeblock.listeners.VisitorProtectionListener;
import skyeblock.nobleskye.dev.skyeblock.managers.CustomSchematicManager;
import skyeblock.nobleskye.dev.skyeblock.managers.IslandManager;
import skyeblock.nobleskye.dev.skyeblock.managers.SchematicManager;
import skyeblock.nobleskye.dev.skyeblock.managers.WorldManager;
import skyeblock.nobleskye.dev.skyeblock.managers.IslandSettingsManager;
import skyeblock.nobleskye.dev.skyeblock.gui.IslandSettingsGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.MainSettingsGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.VisitingSettingsGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.IslandVisitGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.DeleteConfirmationGUI;
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
    private IslandSettingsManager islandSettingsManager;
    private IslandSettingsGUI islandSettingsGUI;
    private MainSettingsGUI mainSettingsGUI;
    private VisitingSettingsGUI visitingSettingsGUI;
    private IslandVisitGUI islandVisitGUI;
    private DeleteConfirmationGUI deleteConfirmationGUI;
    private VisitorProtectionListener visitorProtectionListener;
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
        this.islandSettingsManager = new IslandSettingsManager(this);
        
        // Initialize GUIs
        this.islandSettingsGUI = new IslandSettingsGUI(this);
        this.mainSettingsGUI = new MainSettingsGUI(this);
        this.visitingSettingsGUI = new VisitingSettingsGUI(this);
        this.islandVisitGUI = new IslandVisitGUI(this);
        this.deleteConfirmationGUI = new DeleteConfirmationGUI(this);
        
        // Initialize world
        worldManager.initializeWorld();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
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
        
        skyeblock.nobleskye.dev.skyeblock.commands.VisitCommand visitCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.VisitCommand(this);
        getCommand("visit").setExecutor(visitCommand);
        getCommand("visit").setTabCompleter(visitCommand);
        
        skyeblock.nobleskye.dev.skyeblock.commands.DeleteCommand deleteCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.DeleteCommand(this);
        getCommand("delete").setExecutor(deleteCommand);
        getCommand("delete").setTabCompleter(deleteCommand);
    }
    
    private void registerListeners() {
        this.visitorProtectionListener = new VisitorProtectionListener(this);
        getServer().getPluginManager().registerEvents(visitorProtectionListener, this);
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

    public IslandSettingsManager getIslandSettingsManager() {
        return islandSettingsManager;
    }

    public IslandSettingsGUI getIslandSettingsGUI() {
        return islandSettingsGUI;
    }

    public MainSettingsGUI getMainSettingsGUI() {
        return mainSettingsGUI;
    }

    public VisitingSettingsGUI getVisitingSettingsGUI() {
        return visitingSettingsGUI;
    }

    public IslandVisitGUI getIslandVisitGUI() {
        return islandVisitGUI;
    }

    public DeleteConfirmationGUI getDeleteConfirmationGUI() {
        return deleteConfirmationGUI;
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
