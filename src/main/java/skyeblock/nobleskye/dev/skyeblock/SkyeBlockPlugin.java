package skyeblock.nobleskye.dev.skyeblock;

import skyeblock.nobleskye.dev.skyeblock.commands.IslandCommand;
import skyeblock.nobleskye.dev.skyeblock.commands.HubCommand;
import skyeblock.nobleskye.dev.skyeblock.listeners.VisitorProtectionListener;
import skyeblock.nobleskye.dev.skyeblock.listeners.ServerBrandListener;
import skyeblock.nobleskye.dev.skyeblock.listeners.PlayerJoinListener;
import skyeblock.nobleskye.dev.skyeblock.util.ServerBrandUtil;
import skyeblock.nobleskye.dev.skyeblock.util.SpigotBrandModifier;
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
    private ServerBrandListener serverBrandListener;
    private ServerBrandChanger serverBrandChanger;
    private PlayerJoinListener playerJoinListener;
    private SpigotBrandModifier spigotBrandModifier;
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
        // Register main /sb command with subcommands
        skyeblock.nobleskye.dev.skyeblock.commands.SkyeBlockCommand sbCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.SkyeBlockCommand(this);
        getCommand("sb").setExecutor(sbCommand);
        getCommand("sb").setTabCompleter(sbCommand);
        
        // Register individual commands (so they work both as /command and /sb command)
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
        
        // Register server brand command
        skyeblock.nobleskye.dev.skyeblock.commands.ServerBrandCommand serverBrandCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.ServerBrandCommand(this);
        getCommand("serverbrand").setExecutor(serverBrandCommand);
        getCommand("serverbrand").setTabCompleter(serverBrandCommand);
    }
    
    private void registerListeners() {
        this.visitorProtectionListener = new VisitorProtectionListener(this);
        getServer().getPluginManager().registerEvents(visitorProtectionListener, this);
        
        // Server brand configuration from config.yml
        boolean brandEnabled = getConfig().getBoolean("server-brand.enabled", true);
        if (!brandEnabled) {
            getLogger().info("Custom server brand feature is disabled in config.yml");
            return;
        }
        
        // Get the custom brand name from config
        String customBrand = getConfig().getString("server-brand.name", "LegitiSkyeSlimePaper");
        
        // Try all methods to modify the server brand for maximum compatibility
        
        // Method 1: Use our utility class with multiple approaches
        ServerBrandUtil.modifyServerBrand(this, customBrand);
        
        // Method 2: Try to set the server brand using reflection-based listener
        try {
            this.serverBrandListener = new ServerBrandListener(this, customBrand);
            getServer().getPluginManager().registerEvents(serverBrandListener, this);
        } catch (Exception e) {
            getLogger().warning("Failed to initialize reflection-based brand changer: " + e.getMessage());
        }
        
        // Method 3: Try the plugin messaging approach
        try {
            this.serverBrandChanger = new ServerBrandChanger(this, customBrand);
        } catch (Exception e) {
            getLogger().warning("Failed to initialize plugin messaging brand changer: " + e.getMessage());
        }
        
        // Method 4: Try the Spigot-specific brand modifier
        try {
            this.spigotBrandModifier = new SpigotBrandModifier(this, customBrand);
        } catch (Exception e) {
            getLogger().warning("Failed to initialize Spigot brand modifier: " + e.getMessage());
        }
        
        // Register player join listener to update brand for joining players
        this.playerJoinListener = new PlayerJoinListener(this);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        
        getLogger().info("Server brand set to: " + customBrand);
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
    
    public ServerBrandListener getServerBrandListener() {
        return serverBrandListener;
    }
    
    public ServerBrandChanger getServerBrandChanger() {
        return serverBrandChanger;
    }
    
    public PlayerJoinListener getPlayerJoinListener() {
        return playerJoinListener;
    }
    
    public SpigotBrandModifier getSpigotBrandModifier() {
        return spigotBrandModifier;
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
