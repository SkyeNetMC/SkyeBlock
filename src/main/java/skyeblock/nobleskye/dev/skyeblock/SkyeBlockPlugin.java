package skyeblock.nobleskye.dev.skyeblock;

import skyeblock.nobleskye.dev.skyeblock.commands.IslandCommand;
import skyeblock.nobleskye.dev.skyeblock.commands.HubCommand;
import skyeblock.nobleskye.dev.skyeblock.commands.SpawnCommand;
import skyeblock.nobleskye.dev.skyeblock.listeners.VisitorProtectionListener;
import skyeblock.nobleskye.dev.skyeblock.listeners.PlayerJoinListener;
import skyeblock.nobleskye.dev.skyeblock.listeners.PlayerLocationListener;
import skyeblock.nobleskye.dev.skyeblock.listeners.VisitorPacketListener;
import skyeblock.nobleskye.dev.skyeblock.managers.CustomSchematicManager;
import skyeblock.nobleskye.dev.skyeblock.managers.IslandManager;
import skyeblock.nobleskye.dev.skyeblock.managers.SchematicManager;
import skyeblock.nobleskye.dev.skyeblock.managers.WorldManager;
import skyeblock.nobleskye.dev.skyeblock.managers.IslandSettingsManager;
import skyeblock.nobleskye.dev.skyeblock.managers.ResourceWorldManager;
import skyeblock.nobleskye.dev.skyeblock.managers.WarpManager;
import skyeblock.nobleskye.dev.skyeblock.managers.PlayerDataManager;
import skyeblock.nobleskye.dev.skyeblock.permissions.IslandPermissionManager;
import skyeblock.nobleskye.dev.skyeblock.gui.IslandSettingsGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.MainSettingsGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.VisitingSettingsGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.IslandVisitGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.DeleteConfirmationGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.IslandCreationGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.PermissionManagementGUI;
import skyeblock.nobleskye.dev.skyeblock.gui.WarpGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;

public class SkyeBlockPlugin extends JavaPlugin {

    private IslandManager islandManager;
    private SchematicManager schematicManager;
    private CustomSchematicManager customSchematicManager;
    private WorldManager worldManager;
    private IslandSettingsManager islandSettingsManager;
    private IslandPermissionManager permissionManager;
    private ResourceWorldManager resourceWorldManager;
    private WarpManager warpManager;
    private PlayerDataManager playerDataManager;
    private IslandSettingsGUI islandSettingsGUI;
    private MainSettingsGUI mainSettingsGUI;
    private VisitingSettingsGUI visitingSettingsGUI;
    private IslandVisitGUI islandVisitGUI;
    private DeleteConfirmationGUI deleteConfirmationGUI;
    private IslandCreationGUI islandCreationGUI;
    private PermissionManagementGUI permissionManagementGUI;
    private WarpGUI warpGUI;
    private VisitorProtectionListener visitorProtectionListener;
    private PlayerJoinListener playerJoinListener;
    private PlayerLocationListener playerLocationListener;
    private MiniMessage miniMessage;
    private FileConfiguration warpConfig;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize warp configuration
        loadWarpConfig();
        
        // Initialize MiniMessage
        miniMessage = MiniMessage.miniMessage();
        
        // Initialize managers
        this.worldManager = new WorldManager(this);
        this.schematicManager = new SchematicManager(this);
        this.customSchematicManager = new CustomSchematicManager(this);
        this.islandManager = new IslandManager(this);
        this.islandSettingsManager = new IslandSettingsManager(this);
        this.permissionManager = new IslandPermissionManager(this);
        this.resourceWorldManager = new ResourceWorldManager(this);
        this.warpManager = new WarpManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        
        // Initialize GUIs
        this.islandSettingsGUI = new IslandSettingsGUI(this);
        this.mainSettingsGUI = new MainSettingsGUI(this);
        this.visitingSettingsGUI = new VisitingSettingsGUI(this);
        this.islandVisitGUI = new IslandVisitGUI(this);
        this.deleteConfirmationGUI = new DeleteConfirmationGUI(this);
        this.islandCreationGUI = new IslandCreationGUI(this);
        this.permissionManagementGUI = new PermissionManagementGUI(this);
        this.warpGUI = new WarpGUI(this);
        
        // Initialize world
        worldManager.initializeWorld();
        
        // Initialize island manager (load existing islands)
        islandManager.initialize();
        
        // Initialize resource worlds and warps
        resourceWorldManager.initializeResourceWorlds();
        warpManager.initializeWarps();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Log enable message
        getComponentLogger().info(Component.text("SkyeBlock plugin enabled!", NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        // Save all island data before shutdown
        if (islandManager != null) {
            islandManager.saveAllIslands();
        }
        
        // Save player data before shutdown
        if (playerDataManager != null) {
            playerDataManager.savePlayerData();
        }
        
        getComponentLogger().info(Component.text("SkyeBlock plugin disabled!", NamedTextColor.RED));
    }

    private void registerCommands() {
        // Register main /sb command with subcommands
        skyeblock.nobleskye.dev.skyeblock.commands.SkyeBlockCommand sbCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.SkyeBlockCommand(this);
        if (getCommand("sb") != null) {
            getCommand("sb").setExecutor(sbCommand);
            getCommand("sb").setTabCompleter(sbCommand);
        } else {
            getLogger().severe("Failed to register 'sb' command - command not found in plugin.yml!");
        }
        
        // Register individual commands (so they work both as /command and /sb command)
        IslandCommand islandCommandExecutor = new IslandCommand(this);
        if (getCommand("island") != null) {
            getCommand("island").setExecutor(islandCommandExecutor);
        } else {
            getLogger().severe("Failed to register 'island' command - command not found in plugin.yml!");
        }
        
        // Also register the /is alias explicitly (in case Bukkit doesn't handle it automatically)
        if (getCommand("is") != null) {
            getCommand("is").setExecutor(islandCommandExecutor);
        } else {
            getLogger().info("'/is' alias not found as separate command - using automatic alias from 'island' command");
        }
        
        if (getCommand("hub") != null) {
            getCommand("hub").setExecutor(new HubCommand(this));
            getCommand("spawn").setExecutor(new SpawnCommand(this));
        } else {
            getLogger().severe("Failed to register 'hub' command - command not found in plugin.yml!");
        }
        
        // Register create island command
        skyeblock.nobleskye.dev.skyeblock.commands.CreateIslandCommand createIslandCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.CreateIslandCommand(this);
        if (getCommand("createisland") != null) {
            getCommand("createisland").setExecutor(createIslandCommand);
            getCommand("createisland").setTabCompleter(createIslandCommand);
        } else {
            getLogger().severe("Failed to register 'createisland' command - command not found in plugin.yml!");
        }
        
        skyeblock.nobleskye.dev.skyeblock.commands.VisitCommand visitCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.VisitCommand(this);
        if (getCommand("visit") != null) {
            getCommand("visit").setExecutor(visitCommand);
            getCommand("visit").setTabCompleter(visitCommand);
        } else {
            getLogger().severe("Failed to register 'visit' command - command not found in plugin.yml!");
        }
        
        skyeblock.nobleskye.dev.skyeblock.commands.DeleteCommand deleteCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.DeleteCommand(this);
        if (getCommand("delete") != null) {
            getCommand("delete").setExecutor(deleteCommand);
            getCommand("delete").setTabCompleter(deleteCommand);
        } else {
            getLogger().severe("Failed to register 'delete' command - command not found in plugin.yml!");
        }
        
        // Register convert islands command
        skyeblock.nobleskye.dev.skyeblock.commands.ConvertIslandsCommand convertCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.ConvertIslandsCommand(this);
        if (getCommand("convertislands") != null) {
            getCommand("convertislands").setExecutor(convertCommand);
            getCommand("convertislands").setTabCompleter(convertCommand);
        } else {
            getLogger().severe("Failed to register 'convertislands' command - command not found in plugin.yml!");
        }

        // Register permission command
        skyeblock.nobleskye.dev.skyeblock.commands.PermissionCommand permissionCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.PermissionCommand(this);
        if (getCommand("islandpermissions") != null) {
            getCommand("islandpermissions").setExecutor(permissionCommand);
        } else {
            getLogger().severe("Failed to register 'islandpermissions' command - command not found in plugin.yml!");
        }
        
        // Register warp commands
        skyeblock.nobleskye.dev.skyeblock.commands.WarpCommand warpCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.WarpCommand(this);
        if (getCommand("warp") != null) {
            getCommand("warp").setExecutor(warpCommand);
            getCommand("warp").setTabCompleter(warpCommand);
        } else {
            getLogger().severe("Failed to register 'warp' command - command not found in plugin.yml!");
        }
        
        skyeblock.nobleskye.dev.skyeblock.commands.WarpAdminCommand warpAdminCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.WarpAdminCommand(this);
        if (getCommand("warpadmin") != null) {
            getCommand("warpadmin").setExecutor(warpAdminCommand);
            getCommand("warpadmin").setTabCompleter(warpAdminCommand);
        } else {
            getLogger().severe("Failed to register 'warpadmin' command - command not found in plugin.yml!");
        }
        
        // Register admin command (/sba)
        skyeblock.nobleskye.dev.skyeblock.commands.SkyeBlockAdminCommand sbaCommand = 
            new skyeblock.nobleskye.dev.skyeblock.commands.SkyeBlockAdminCommand(this);
        if (getCommand("sba") != null) {
            getCommand("sba").setExecutor(sbaCommand);
            getCommand("sba").setTabCompleter(sbaCommand);
        } else {
            getLogger().severe("Failed to register 'sba' command - command not found in plugin.yml!");
        }
    }
    
    private void registerListeners() {
        this.visitorProtectionListener = new VisitorProtectionListener(this);
        getServer().getPluginManager().registerEvents(visitorProtectionListener, this);
        // Register packet-level visitor protection
        VisitorPacketListener.register(this);
        
        // Register player join listener
        this.playerJoinListener = new PlayerJoinListener(this);
        getServer().getPluginManager().registerEvents(playerJoinListener, this);
        
        // Register player location listener to track player locations
        this.playerLocationListener = new PlayerLocationListener(this);
        getServer().getPluginManager().registerEvents(playerLocationListener, this);
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

    public IslandPermissionManager getPermissionManager() {
        return permissionManager;
    }

    public ResourceWorldManager getResourceWorldManager() {
        return resourceWorldManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
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
    
    public IslandCreationGUI getIslandCreationGUI() {
        return islandCreationGUI;
    }
    
    public PermissionManagementGUI getPermissionManagementGUI() {
        return permissionManagementGUI;
    }
    
    public WarpGUI getWarpGUI() {
        return warpGUI;
    }
    
    public PlayerJoinListener getPlayerJoinListener() {
        return playerJoinListener;
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
    
    /**
     * Load warp configuration from warps.yml
     */
    private void loadWarpConfig() {
        File warpFile = new File(getDataFolder(), "warps.yml");
        
        // Save default warps.yml if it doesn't exist
        if (!warpFile.exists()) {
            saveResource("warps.yml", false);
        }
        
        warpConfig = YamlConfiguration.loadConfiguration(warpFile);
        
        // Load defaults from jar file
        InputStream defConfigStream = getResource("warps.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(defConfigStream));
            warpConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * Get the warp configuration
     */
    public FileConfiguration getWarpConfig() {
        if (warpConfig == null) {
            loadWarpConfig();
        }
        return warpConfig;
    }
    
    /**
     * Reload warp configuration
     */
    public void reloadWarpConfig() {
        File warpFile = new File(getDataFolder(), "warps.yml");
        warpConfig = YamlConfiguration.loadConfiguration(warpFile);
        
        InputStream defConfigStream = getResource("warps.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new java.io.InputStreamReader(defConfigStream));
            warpConfig.setDefaults(defConfig);
        }
    }
    
    /**
     * Override reloadConfig to ensure config exists but not overwrite existing config
     */
    @Override
    public void reloadConfig() {
        // Ensure config exists, but don't overwrite if it already exists
        saveDefaultConfig();
        
        // Call parent reloadConfig to load the file
        super.reloadConfig();
        
        getLogger().info("Config reloaded successfully");
    }
    
    /**
     * Force regenerate config file (for admin use when updating config structure)
     */
    public void forceRegenerateConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            // Create backup
            File backupFile = new File(getDataFolder(), "config_backup_" + System.currentTimeMillis() + ".yml");
            try {
                java.nio.file.Files.copy(configFile.toPath(), backupFile.toPath());
                getLogger().info("Backed up existing config to: " + backupFile.getName());
            } catch (Exception e) {
                getLogger().warning("Failed to backup config file: " + e.getMessage());
            }
            
            // Delete old config
            configFile.delete();
        }
        
        // Generate new config
        saveDefaultConfig();
        super.reloadConfig();
        
        getLogger().info("Config regenerated with latest structure");
    }
}
