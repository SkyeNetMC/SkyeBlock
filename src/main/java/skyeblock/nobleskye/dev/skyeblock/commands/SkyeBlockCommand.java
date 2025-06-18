package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Main SkyeBlock command handler that routes subcommands to individual command handlers
 */
public class SkyeBlockCommand implements CommandExecutor, TabCompleter {
    
    private final SkyeBlockPlugin plugin;
    private final IslandCommand islandCommand;
    private final VisitCommand visitCommand;
    private final DeleteCommand deleteCommand;
    private final HubCommand hubCommand;
    
    public SkyeBlockCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.islandCommand = new IslandCommand(plugin);
        this.visitCommand = new VisitCommand(plugin);
        this.deleteCommand = new DeleteCommand(plugin);
        this.hubCommand = new HubCommand(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show help when no subcommand is provided
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        // Route to appropriate command handler
        switch (subCommand) {
            case "island":
            case "is":
                return islandCommand.onCommand(sender, command, label, subArgs);
            case "visit":
                return visitCommand.onCommand(sender, command, label, subArgs);
            case "delete":
                return deleteCommand.onCommand(sender, command, label, subArgs);
            case "hub":
                return hubCommand.onCommand(sender, command, label, subArgs);
            case "reload":
                return handleReloadCommand(sender, subArgs);
            case "debug":
                return handleDebugCommand(sender, subArgs);
            case "status":
                // Route status command to island command handler
                String[] statusArgs = new String[]{"status"};
                return islandCommand.onCommand(sender, command, label, statusArgs);
            default:
                sender.sendMessage(Component.text("Unknown subcommand: " + subCommand, NamedTextColor.RED));
                showHelp(sender);
                return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Tab complete subcommands
            List<String> subCommands = Arrays.asList("island", "visit", "delete", "hub", "reload");
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
            return completions;
        } else if (args.length > 1) {
            // Route tab completion to appropriate command handler
            String subCommand = args[0].toLowerCase();
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            
            switch (subCommand) {
                case "island":
                case "is":
                    return islandCommand.onTabComplete(sender, command, alias, subArgs);
                case "visit":
                    return visitCommand.onTabComplete(sender, command, alias, subArgs);
                case "delete":
                    return deleteCommand.onTabComplete(sender, command, alias, subArgs);
                case "hub":
                    // Hub command doesn't need tab completion
                    return new ArrayList<>();
                default:
                    return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== SkyeBlock Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/sb island - Manage your island", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sb visit <player> - Visit another player's island", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sb delete [player] - Delete your island (or another player's if admin)", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sb hub - Return to the hub", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sb reload [force] - Reload plugin configuration (admin only)", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("You can also use these commands directly:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/island, /visit, /delete, /hub", NamedTextColor.GRAY));
    }
    
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.reload")) {
            sender.sendMessage(Component.text("You don't have permission to reload the plugin!", NamedTextColor.RED));
            return true;
        }
        
        boolean forceRegenerate = args.length > 0 && "force".equalsIgnoreCase(args[0]);
        
        try {
            if (forceRegenerate) {
                // Force regenerate config with new structure
                plugin.forceRegenerateConfig();
                plugin.reloadWarpConfig();
                sender.sendMessage(Component.text("SkyeBlock configuration forcefully regenerated with latest structure!", NamedTextColor.GREEN));
                plugin.getLogger().info("Configuration forcefully regenerated by " + sender.getName());
            } else {
                // Normal reload
                plugin.reloadConfig();
                plugin.reloadWarpConfig();
                sender.sendMessage(Component.text("SkyeBlock configuration reloaded successfully!", NamedTextColor.GREEN));
                plugin.getLogger().info("Configuration reloaded by " + sender.getName());
            }
            return true;
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to reload configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Failed to reload configuration: " + e.getMessage());
            return true;
        }
    }
    
    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.debug")) {
            sender.sendMessage(Component.text("You don't have permission to use debug commands!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(Component.text("Debug commands:", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("/sb debug schematics - Check schematic folder and files", NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("/sb debug cooldown [player] - Check island creation cooldown", NamedTextColor.YELLOW));
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "schematics":
                return debugSchematics(sender);
            case "cooldown":
                return debugCooldown(sender, args);
            default:
                sender.sendMessage(Component.text("Unknown debug command: " + args[0], NamedTextColor.RED));
                return true;
        }
    }
    
    private boolean debugSchematics(CommandSender sender) {
        sender.sendMessage(Component.text("=== Schematic Debug Information ===", NamedTextColor.GOLD));
        
        // Schematic folder info
        java.io.File schematicFolder = plugin.getSchematicManager().getSchematicFolder();
        sender.sendMessage(Component.text("Schematic folder: " + schematicFolder.getAbsolutePath(), NamedTextColor.AQUA));
        sender.sendMessage(Component.text("Folder exists: " + schematicFolder.exists(), NamedTextColor.AQUA));
        
        if (schematicFolder.exists()) {
            java.io.File[] files = schematicFolder.listFiles();
            if (files != null && files.length > 0) {
                sender.sendMessage(Component.text("Files in schematic folder (" + files.length + "):", NamedTextColor.GREEN));
                for (java.io.File file : files) {
                    String info = "  " + file.getName() + " (" + file.length() + " bytes)";
                    if (file.getName().endsWith(".schem") || file.getName().endsWith(".schematic")) {
                        sender.sendMessage(Component.text(info, NamedTextColor.GREEN));
                    } else {
                        sender.sendMessage(Component.text(info, NamedTextColor.GRAY));
                    }
                }
            } else {
                sender.sendMessage(Component.text("Schematic folder is empty!", NamedTextColor.RED));
            }
        }
        
        // Available schematics
        String[] availableSchematics = plugin.getSchematicManager().getAvailableSchematics();
        sender.sendMessage(Component.text("Available schematics (" + availableSchematics.length + "): " + 
            String.join(", ", availableSchematics), NamedTextColor.YELLOW));
        
        // Config templates
        sender.sendMessage(Component.text("Configured templates:", NamedTextColor.YELLOW));
        var config = plugin.getConfig();
        if (config.contains("island.templates")) {
            var section = config.getConfigurationSection("island.templates");
            if (section != null) {
                for (String key : section.getKeys(false)) {
                    String value = section.getString(key);
                    sender.sendMessage(Component.text("  " + key + " -> " + value, NamedTextColor.GRAY));
                }
            }
        } else {
            sender.sendMessage(Component.text("  No templates configured", NamedTextColor.RED));
        }
        
        return true;
    }
    
    private boolean debugCooldown(CommandSender sender, String[] args) {
        UUID targetUUID;
        String playerName;
        
        if (args.length > 1) {
            // Check specific player (admin command)
            playerName = args[1];
            org.bukkit.OfflinePlayer offlinePlayer = org.bukkit.Bukkit.getOfflinePlayer(playerName);
            targetUUID = offlinePlayer.getUniqueId();
        } else if (sender instanceof Player) {
            // Check self
            Player player = (Player) sender;
            targetUUID = player.getUniqueId();
            playerName = player.getName();
        } else {
            sender.sendMessage(Component.text("You must specify a player name when using from console", NamedTextColor.RED));
            return true;
        }
        
        sender.sendMessage(Component.text("=== Island Creation Cooldown Debug ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Player: " + playerName, NamedTextColor.AQUA));
        
        // Cooldown info
        long remainingCooldown = plugin.getIslandManager().getRemainingCooldown(targetUUID);
        if (remainingCooldown > 0) {
            long minutes = remainingCooldown / 60;
            long seconds = remainingCooldown % 60;
            String timeMessage = minutes > 0 ? minutes + "m " + seconds + "s" : seconds + "s";
            sender.sendMessage(Component.text("Cooldown: " + timeMessage + " remaining", NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.text("Cooldown: None", NamedTextColor.GREEN));
        }
        
        // Try count info
        int deletionTries = plugin.getIslandManager().getDeletionTries(targetUUID);
        int maxTries = plugin.getIslandManager().getMaxDeletionTries();
        int remainingTries = maxTries - deletionTries;
        
        sender.sendMessage(Component.text("Deletions used: " + deletionTries + "/" + maxTries, 
            remainingTries > 0 ? NamedTextColor.YELLOW : NamedTextColor.RED));
        sender.sendMessage(Component.text("Remaining tries: " + remainingTries, 
            remainingTries > 0 ? NamedTextColor.GREEN : NamedTextColor.RED));
        
        // Config settings
        sender.sendMessage(Component.text("Config settings:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  Delay: " + plugin.getConfig().getLong("island.create-island.delay", 300) + " seconds", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  Max tries: " + maxTries, NamedTextColor.GRAY));
        
        return true;
    }
}
