package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IslandCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;

    public IslandCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        // Register tab completer
        plugin.getCommand("island").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreateCommand(player, args);
                break;
            case "tp":
            case "teleport":
            case "home":
                handleTeleportCommand(player);
                break;
            case "delete":
                handleDeleteCommand(player);
                break;
            case "list":
                handleListCommand(player);
                break;
            case "types":
                handleTypesCommand(player);
                break;
            case "help":
                sendHelp(player);
                break;
            default:
                plugin.sendMessage(player, "invalid-command");
                sendHelp(player);
        }

        return true;
    }

    private void handleCreateCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        // Check if player already has an island
        if (plugin.getIslandManager().hasIsland(player.getUniqueId())) {
            plugin.sendMessage(player, "island-already-exists");
            return;
        }

        // Determine island type
        String islandType;
        if (args.length > 1) {
            // Join all arguments after "create" to handle island types with spaces
            StringBuilder typeBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) typeBuilder.append(" ");
                typeBuilder.append(args[i]);
            }
            islandType = typeBuilder.toString();
        } else {
            islandType = "classic"; // Default to classic type
        }

        // Validate island type using custom schematic manager
        String[] availableTypes = plugin.getCustomSchematicManager().getAvailableSchematics();
        boolean validType = false;
        for (String type : availableTypes) {
            if (type.equalsIgnoreCase(islandType)) {
                validType = true;
                islandType = type; // Use the exact case from available types
                break;
            }
        }
        
        if (!validType) {
            player.sendMessage(miniMessage.deserialize("<red>Invalid island type! Available types: <yellow>" + 
                String.join("</yellow>, <yellow>", availableTypes) + "</yellow></red>"));
            return;
        }

        // Create the island
        boolean success = plugin.getIslandManager().createIsland(player, islandType);
        
        if (success) {
            plugin.sendMessage(player, "island-created");
            
            // Teleport player to their new island
            plugin.getIslandManager().teleportToIsland(player);
        } else {
            player.sendMessage(miniMessage.deserialize("<red>Failed to create island. Please contact an administrator.</red>"));
        }
    }

    private void handleTeleportCommand(Player player) {
        if (!player.hasPermission("skyeblock.island")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        boolean success = plugin.getIslandManager().teleportToIsland(player);
        
        if (success) {
            plugin.sendMessage(player, "teleported");
        } else {
            plugin.sendMessage(player, "island-not-found");
        }
    }

    private void handleDeleteCommand(Player player) {
        if (!player.hasPermission("skyeblock.admin")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        boolean success = plugin.getIslandManager().deleteIsland(player);
        
        if (!success) {
            plugin.sendMessage(player, "island-not-found");
        }
    }

    private void handleListCommand(Player player) {
        if (!player.hasPermission("skyeblock.admin")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Map<UUID, Island> islands = plugin.getIslandManager().getPlayerIslands();
        
        if (islands.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<yellow>No islands found.</yellow>"));
            return;
        }

        player.sendMessage(miniMessage.deserialize("<gold>=== Island List ===</gold>"));
        for (Map.Entry<UUID, Island> entry : islands.entrySet()) {
            Island island = entry.getValue();
            String ownerName = plugin.getServer().getOfflinePlayer(entry.getKey()).getName();
            player.sendMessage(miniMessage.deserialize("<aqua>" + ownerName + "</aqua> <white>-</white> " + 
                             "<green>" + island.getIslandType() + "</green> <white>at</white> " +
                             "<yellow>" + island.getLocation().getBlockX() + ", " + 
                             island.getLocation().getBlockY() + ", " + 
                             island.getLocation().getBlockZ() + "</yellow>"));
        }
    }

    private void handleTypesCommand(Player player) {
        String[] availableTypes = plugin.getCustomSchematicManager().getAvailableSchematics();
        
        player.sendMessage(miniMessage.deserialize("<gold>=== Available Island Types ===</gold>"));
        for (String type : availableTypes) {
            var schematic = plugin.getCustomSchematicManager().getSchematic(type);
            if (schematic != null) {
                player.sendMessage(miniMessage.deserialize("<aqua>" + type + "</aqua> <white>-</white> " + 
                                 "<gray>" + schematic.getDescription() + "</gray>"));
            }
        }
        player.sendMessage(miniMessage.deserialize("<yellow>Usage: /island create <type></yellow>"));
    }

    private void sendHelp(Player player) {
        plugin.sendMessage(player, "help-header");
        plugin.sendMessage(player, "help-create");
        player.sendMessage(miniMessage.deserialize("<yellow>/island types</yellow> <gray>-</gray> <gray>Show available island types</gray>"));
        plugin.sendMessage(player, "help-teleport");
        player.sendMessage(miniMessage.deserialize("<yellow>/island help</yellow> <gray>-</gray> <gray>Show this help message</gray>"));
        
        if (player.hasPermission("skyeblock.admin")) {
            player.sendMessage(miniMessage.deserialize("<red>Admin Commands:</red>"));
            plugin.sendMessage(player, "help-delete");
            plugin.sendMessage(player, "help-list");
        }
        
        plugin.sendMessage(player, "help-hub");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = Arrays.asList("create", "tp", "teleport", "home", "types", "help");
            if (sender.hasPermission("skyeblock.admin")) {
                subCommands = new ArrayList<>(subCommands);
                subCommands.addAll(Arrays.asList("delete", "list"));
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("create")) {
            // Arguments for create command - island types (including multi-word types)
            String[] availableTypes = plugin.getCustomSchematicManager().getAvailableSchematics();
            
            // Join current arguments to build partial type name
            StringBuilder currentType = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) currentType.append(" ");
                currentType.append(args[i]);
            }
            String partialType = currentType.toString();
            
            for (String type : availableTypes) {
                // If the type starts with what the user has typed (case-insensitive)
                if (type.toLowerCase().startsWith(partialType.toLowerCase())) {
                    // Add the full type name for completion
                    completions.add(type);
                }
            }
        }

        return completions;
    }
}
