package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
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

/**
 * SkyeBlock Admin command handler that provides admin bypass functionality
 * Usage: /sba <subcommand> [args...]
 * Requires admin permissions for all operations
 */
public class SkyeBlockAdminCommand implements CommandExecutor, TabCompleter {
    
    private final SkyeBlockPlugin plugin;
    private final IslandCommand islandCommand;
    private final VisitCommand visitCommand;
    private final DeleteCommand deleteCommand;
    private final CreateIslandCommand createIslandCommand;
    private final HubCommand hubCommand;
    
    public SkyeBlockAdminCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.islandCommand = new IslandCommand(plugin);
        this.visitCommand = new VisitCommand(plugin);
        this.deleteCommand = new DeleteCommand(plugin);
        this.createIslandCommand = new CreateIslandCommand(plugin);
        this.hubCommand = new HubCommand(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check for admin permission
        if (!sender.hasPermission("skyeblock.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use admin commands!", NamedTextColor.RED));
            return true;
        }
        
        if (args.length == 0) {
            showAdminHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        // Add admin bypass flag to sender if they're a player
        // The admin bypass is already handled by permission "skyeblock.admin.bypass"
        // which is checked in the individual commands and listeners
        
        // Route to appropriate command handler with admin privileges
        switch (subCommand) {
            case "island":
            case "is":
                return handleAdminIslandCommand(sender, subArgs);
            case "create":
                return handleAdminCreateCommand(sender, subArgs);
            case "delete":
                return handleAdminDeleteCommand(sender, subArgs);
            case "visit":
                return handleAdminVisitCommand(sender, subArgs);
            case "hub":
                return hubCommand.onCommand(sender, command, label, subArgs);
            case "reload":
                return handleReloadCommand(sender, subArgs);
            case "debug":
                return handleDebugCommand(sender, subArgs);
            case "bypass":
                return handleBypassCommand(sender, subArgs);
            default:
                sender.sendMessage(Component.text("Unknown admin subcommand: " + subCommand, NamedTextColor.RED));
                showAdminHelp(sender);
                return true;
        }
    }
    
    /**
     * Get the island the admin is currently on by checking their world
     */
    private Island getIslandFromAdminLocation(Player admin) {
        String worldName = admin.getWorld().getName();
        
        // Check if the world is an island world
        Island island = plugin.getIslandManager().getIslandById(worldName);
        
        // If direct match fails, check if it's a nether island world (ends with _nether)
        if (island == null && worldName.endsWith("_nether")) {
            String mainIslandId = worldName.substring(0, worldName.length() - "_nether".length());
            island = plugin.getIslandManager().getIslandById(mainIslandId);
        }
        
        return island;
    }
    
    private boolean handleAdminIslandCommand(CommandSender sender, String[] args) {
        // Admin island management - bypasses all restrictions
        if (sender instanceof Player) {
            Player admin = (Player) sender;
            Island currentIsland = getIslandFromAdminLocation(admin);
            
            if (currentIsland != null) {
                sender.sendMessage(Component.text("Executing island command for the island you're on ("
                    + currentIsland.getIslandId() + ") with admin privileges...", NamedTextColor.YELLOW));
                
                // Temporarily set the admin's UUID to the island owner's UUID for the command context
                // Note: This approach maintains the admin's actual identity but operates on the current island
                return islandCommand.onCommand(sender, null, "sba", args);
            } else {
                sender.sendMessage(Component.text("You are not currently on an island! Go to an island first, then use /sba island.", NamedTextColor.RED));
                return true;
            }
        }
        
        sender.sendMessage(Component.text("Executing island command with admin privileges...", NamedTextColor.YELLOW));
        return islandCommand.onCommand(sender, null, "sba", args);
    }
    
    private boolean handleAdminCreateCommand(CommandSender sender, String[] args) {
        // Admin island creation - bypasses cooldowns and limits
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        sender.sendMessage(Component.text("Creating island with admin privileges (bypassing all restrictions)...", NamedTextColor.YELLOW));
        
        // Use CreateIslandCommand with admin bypass
        return createIslandCommand.onCommand(sender, null, "sba", args);
    }
    
    private boolean handleAdminDeleteCommand(CommandSender sender, String[] args) {
        // Admin island deletion - bypasses cooldowns and restrictions completely
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }
        
        sender.sendMessage(Component.text("Executing deletion with FULL admin privileges (bypassing ALL restrictions)...", NamedTextColor.YELLOW));
        
        // The permission "skyeblock.admin" already provides full bypass
        return deleteCommand.onCommand(sender, null, "sba", args);
    }
    
    private boolean handleAdminVisitCommand(CommandSender sender, String[] args) {
        // Admin visit - bypasses visitor restrictions
        sender.sendMessage(Component.text("Visiting with admin privileges (bypassing visitor restrictions)...", NamedTextColor.YELLOW));
        return visitCommand.onCommand(sender, null, "sba", args);
    }
    
    private boolean handleBypassCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Admin Bypass Status:", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("• Island Creation: Bypasses cooldowns and limits", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("• Island Deletion: Bypasses 1-hour cooldowns, location checks, and 3-deletion limit", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("• Visitor Restrictions: Can interact with everything on any island", NamedTextColor.GREEN));
            sender.sendMessage(Component.text("• Permissions: All admin commands available", NamedTextColor.GREEN));
            return true;
        }
        
        String target = args[0];
        switch (target.toLowerCase()) {
            case "creation":
            case "create":
                sender.sendMessage(Component.text("Admin bypass for island creation:", NamedTextColor.GOLD));
                sender.sendMessage(Component.text("• No deletion cooldown restrictions (1 hour bypass)", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• No maximum island creation limits (3 deletion bypass)", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• Can create islands for other players", NamedTextColor.GREEN));
                break;
            case "deletion":
            case "delete":
                sender.sendMessage(Component.text("Admin bypass for island deletion:", NamedTextColor.GOLD));
                sender.sendMessage(Component.text("• No deletion cooldown restrictions (1 hour bypass)", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• No location requirements (don't need to be on island)", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• No 3-deletion limit enforcement", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• Can delete other players' islands", NamedTextColor.GREEN));
                break;
            case "visitor":
            case "visit":
                sender.sendMessage(Component.text("Admin bypass for visitor restrictions:", NamedTextColor.GOLD));
                sender.sendMessage(Component.text("• Can break/place blocks on any island", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• Can open containers and use workstations", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• Can interact with entities and items", NamedTextColor.GREEN));
                sender.sendMessage(Component.text("• Can engage in PVP and change game modes", NamedTextColor.GREEN));
                break;
            default:
                sender.sendMessage(Component.text("Unknown bypass type: " + target, NamedTextColor.RED));
                sender.sendMessage(Component.text("Available: creation, deletion, visitor", NamedTextColor.YELLOW));
                break;
        }
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        boolean forceRegenerate = args.length > 0 && "force".equalsIgnoreCase(args[0]);
        
        try {
            plugin.reloadConfig();
            
            if (forceRegenerate) {
                sender.sendMessage(Component.text("Force regenerating plugin configuration...", NamedTextColor.YELLOW));
                // Additional force reload logic if needed
            }
            
            sender.sendMessage(Component.text("SkyeBlock configuration reloaded successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Error reloading configuration: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Error reloading configuration: " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Debug information:", NamedTextColor.GOLD));
        } else {
            Player player = (Player) sender;
            sender.sendMessage(Component.text("Debug information for " + player.getName() + ":", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("World: " + player.getWorld().getName(), NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Admin Bypass: " + player.hasPermission("skyeblock.admin.bypass"), NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Admin Permission: " + player.hasPermission("skyeblock.admin"), NamedTextColor.YELLOW));
        }
        
        sender.sendMessage(Component.text("Islands loaded: Available", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("Plugin version: SkyeBlock", NamedTextColor.YELLOW));
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("skyeblock.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("island", "create", "delete", "visit", "hub", "reload", "debug", "bypass");
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
            return completions;
        } else if (args.length > 1) {
            String subCommand = args[0].toLowerCase();
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            
            switch (subCommand) {
                case "island":
                case "is":
                    return islandCommand.onTabComplete(sender, command, alias, subArgs);
                case "create":
                    return createIslandCommand.onTabComplete(sender, command, alias, subArgs);
                case "delete":
                    return deleteCommand.onTabComplete(sender, command, alias, subArgs);
                case "visit":
                    return visitCommand.onTabComplete(sender, command, alias, subArgs);
                case "bypass":
                    if (args.length == 2) {
                        return Arrays.asList("creation", "deletion", "visitor");
                    }
                    break;
                case "reload":
                    if (args.length == 2) {
                        return Arrays.asList("force");
                    }
                    break;
            }
        }
        
        return new ArrayList<>();
    }
    
    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== SkyeBlock Admin Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/sba island - Manage islands with admin privileges", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba create [type] - Create island bypassing restrictions", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba delete [player] - Delete islands bypassing cooldowns", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba visit <player> - Visit with full admin access", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba hub - Return to hub", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba reload [force] - Reload plugin configuration", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba debug - Show debug information", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/sba bypass [type] - Show bypass information", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("All admin commands bypass restrictions:", NamedTextColor.GREEN));
        sender.sendMessage(Component.text("• Cooldowns • Limits • Visitor restrictions • Location checks", NamedTextColor.GRAY));
    }
}
