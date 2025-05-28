package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main SkyeBlock command handler that routes subcommands to individual command handlers
 */
public class SkyeBlockCommand implements CommandExecutor, TabCompleter {
    
    private final IslandCommand islandCommand;
    private final VisitCommand visitCommand;
    private final DeleteCommand deleteCommand;
    private final HubCommand hubCommand;
    
    public SkyeBlockCommand(SkyeBlockPlugin plugin) {
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
            List<String> subCommands = Arrays.asList("island", "visit", "delete", "hub");
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
        sender.sendMessage(Component.text("", NamedTextColor.WHITE));
        sender.sendMessage(Component.text("You can also use these commands directly:", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/island, /visit, /delete, /hub", NamedTextColor.GRAY));
    }
}
