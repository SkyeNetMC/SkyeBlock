package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CreateIslandCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;

    public CreateIslandCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("skyeblock.island.create")) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to create islands!</red>"));
            return true;
        }

        // Check if player already has an island
        if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You already have an island! Use /island to manage it.</red>"));
            return true;
        }

        if (args.length == 0) {
            // Open GUI
            plugin.getIslandCreationGUI().openCreationGUI(player);
        } else if (args.length == 1) {
            // Direct creation with specified type
            String islandType = args[0].toLowerCase();
            
            // Validate island type
            String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
            boolean validType = false;
            for (String type : availableTypes) {
                if (type.equalsIgnoreCase(islandType)) {
                    validType = true;
                    islandType = type; // Use the exact case from available types
                    break;
                }
            }
            
            if (!validType) {
                String availableTypesString = String.join(", ", availableTypes);
                player.sendMessage(miniMessage.deserialize("<red>Invalid island type! Available types: <yellow>" + 
                    availableTypesString + "</yellow></red>"));
                return true;
            }
            
            // Create the island directly
            player.sendMessage(miniMessage.deserialize("<yellow>Creating your " + islandType + " island...</yellow>"));
            
            boolean success = plugin.getIslandManager().createIsland(player, islandType);
            
            if (success) {
                player.sendMessage(miniMessage.deserialize("<green>✓ Island created successfully!</green>"));
                
                // Teleport player to their new island
                plugin.getIslandManager().teleportToIsland(player);
                player.sendMessage(miniMessage.deserialize("<aqua>Welcome to your new island!</aqua>"));
            }
            // Note: Error messages are now handled by IslandManager.canCreateIsland() and createIsland() methods
        } else {
            // Too many arguments
            sendUsage(player);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("skyeblock.island.create")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Return available island types
            String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
            List<String> completions = new ArrayList<>();
            
            for (String type : availableTypes) {
                if (type.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(type);
                }
            }
            
            return completions;
        }

        return new ArrayList<>();
    }
    
    private void sendUsage(Player player) {
        player.sendMessage(miniMessage.deserialize("<gold>=== Create Island Help ===</gold>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/createisland</yellow> <gray>-</gray> <gray>Open island creation GUI</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/createisland <type></yellow> <gray>-</gray> <gray>Create island directly</gray>"));
        
        String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
        if (availableTypes.length > 0) {
            String typesString = String.join(", ", availableTypes);
            player.sendMessage(miniMessage.deserialize("<gray>Available types: <aqua>" + typesString + "</aqua></gray>"));
        }
    }
}
