package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
            case "visit":
                handleVisitCommand(player, args);
                break;
            case "lock":
                handleLockCommand(player);
                break;
            case "unlock":
                handleUnlockCommand(player);
                break;
            case "edit":
                handleEditCommand(player, args);
                break;
            case "coop":
                handleCoopCommand(player, args);
                break;
            case "vote":
                handleVoteCommand(player, args);
                break;
            case "set":
                handleSetCommand(player, args);
                break;
            case "default":
                handleDefaultCommand(player, args);
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
            case "status":
                handleStatusCommand(player);
                break;
            case "settings":
                handleSettingsCommand(player);
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

        // Validate island type using schematic manager
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
            // Create a dynamic message for invalid template
            String availableTypesString = String.join(", ", availableTypes);
            Component errorMessage = miniMessage.deserialize("<red>Invalid island template! Available templates: <yellow>" + 
                availableTypesString.replace(",", "</yellow>,<yellow>") + "</yellow></red>");
            player.sendMessage(errorMessage);
            return;
        }

        // Create the island
        boolean success = plugin.getIslandManager().createIsland(player, islandType);
        
        if (success) {
            plugin.sendMessage(player, "island-created");
            
            // Teleport player to their new island
            plugin.getIslandManager().teleportToIsland(player);
        } else {
            Component errorMessage = miniMessage.deserialize("<red>Failed to create island. Please contact an administrator.</red>");
            player.sendMessage(errorMessage);
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
        if (!player.hasPermission("skyeblock.island.delete")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        // Open confirmation GUI for own island deletion
        plugin.getDeleteConfirmationGUI().openDeleteConfirmation(player, player.getUniqueId());
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
        String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
        
        player.sendMessage(miniMessage.deserialize("<gold>=== Available Island Types ===</gold>"));
        for (String type : availableTypes) {
            // Since .schem files don't have built-in descriptions, show the type name
            player.sendMessage(miniMessage.deserialize("<aqua>" + type + "</aqua> <white>-</white> " + 
                             "<gray>WorldEdit schematic file</gray>"));
        }
        player.sendMessage(miniMessage.deserialize("<yellow>Usage: /island create <type></yellow>"));
    }

    private void handleStatusCommand(Player player) {
        if (!player.hasPermission("skyeblock.admin")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        player.sendMessage(miniMessage.deserialize("<gold>=== SkyeBlock Status ===</gold>"));
        
        // World Manager Status
        boolean slimeEnabled = plugin.getWorldManager().isSlimeWorldEnabled();
        String slimeType = plugin.getWorldManager().getSlimeWorldManagerType();
        
        player.sendMessage(miniMessage.deserialize("<aqua>World Manager:</aqua> " + 
            (slimeEnabled ? "<green>SlimeWorldManager (" + slimeType + ")</green>" : "<red>Standard Bukkit Worlds</red>")));
        
        // Island Statistics
        Map<UUID, Island> islands = plugin.getIslandManager().getPlayerIslands();
        player.sendMessage(miniMessage.deserialize("<aqua>Total Islands:</aqua> <yellow>" + islands.size() + "</yellow>"));
        
        // Available Schematics
        String[] schematics = plugin.getSchematicManager().getAvailableSchematics();
        player.sendMessage(miniMessage.deserialize("<aqua>Available Schematics:</aqua> <yellow>" + schematics.length + "</yellow>"));
        
        player.sendMessage(miniMessage.deserialize("<gray>Use /island types to see all available island types</gray>"));
    }

    private void handleSettingsCommand(Player player) {
        if (!player.hasPermission("skyeblock.island")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        // Check if player has an island
        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        // Open the main settings GUI
        plugin.getMainSettingsGUI().openSettingsGUI(player, island.getIslandId());
    }

    private void handleVisitCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island.visit")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        if (args.length == 1) {
            // Open visit GUI
            plugin.getIslandVisitGUI().openVisitGUI(player);
        } else {
            // Direct visit to player
            String targetPlayerName = args[1];
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
            
            if (targetPlayer.getName() == null) {
                player.sendMessage(miniMessage.deserialize("<red>Player " + targetPlayerName + " not found!</red>"));
                return;
            }

            Island targetIsland = plugin.getIslandManager().getIsland(targetPlayer.getUniqueId());
            if (targetIsland == null) {
                player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " doesn't have an island!</red>"));
                return;
            }

            boolean success = plugin.getIslandManager().teleportToIsland(player, targetPlayer.getUniqueId());
            if (success) {
                player.sendMessage(miniMessage.deserialize("<green>Visiting " + targetPlayerName + "'s island!</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Cannot visit " + targetPlayerName + "'s island. It might be locked.</red>"));
            }
        }
    }

    private void handleLockCommand(Player player) {
        if (!player.hasPermission("skyeblock.island.lock")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        if (!island.canManageSettings(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to manage island settings!</red>"));
            return;
        }

        island.setLocked(true);
        plugin.getIslandManager().saveIsland(island);
        player.sendMessage(miniMessage.deserialize("<green>Island locked! Only coop members can visit now.</green>"));
    }

    private void handleUnlockCommand(Player player) {
        if (!player.hasPermission("skyeblock.island.lock")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        if (!island.canManageSettings(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to manage island settings!</red>"));
            return;
        }

        island.setLocked(false);
        plugin.getIslandManager().saveIsland(island);
        player.sendMessage(miniMessage.deserialize("<green>Island unlocked! Anyone can visit now.</green>"));
    }

    private void handleEditCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island.edit")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        if (!island.canManageSettings(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to edit island settings!</red>"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /is edit <title|desc|icon> [value]</red>"));
            return;
        }

        String editType = args[1].toLowerCase();
        switch (editType) {
            case "title":
                if (args.length < 3) {
                    player.sendMessage(miniMessage.deserialize("<red>Usage: /is edit title <new title></red>"));
                    return;
                }
                String title = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                island.setTitle(title);
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Island title updated to: " + title + "</green>"));
                break;
            case "desc":
            case "description":
                if (args.length < 3) {
                    player.sendMessage(miniMessage.deserialize("<red>Usage: /is edit desc <new description></red>"));
                    return;
                }
                String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                island.setDescription(description);
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Island description updated to: " + description + "</green>"));
                break;
            case "icon":
                ItemStack heldItem = player.getInventory().getItemInMainHand();
                if (heldItem.getType() == Material.AIR) {
                    player.sendMessage(miniMessage.deserialize("<red>Hold an item in your hand to set as island icon!</red>"));
                    return;
                }
                island.setIcon(heldItem.clone());
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Island icon updated to " + heldItem.getType().name() + "!</green>"));
                break;
            default:
                player.sendMessage(miniMessage.deserialize("<red>Usage: /is edit <title|desc|icon> [value]</red>"));
        }
    }

    private void handleCoopCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island.coop")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /is coop <add|remove|role|leave|accept|reject|visit|list> [player] [role]</red>"));
            player.sendMessage(miniMessage.deserialize("<yellow>Use '/is coop visit <player>' to visit an island in survival mode if you have permission.</yellow>"));
            return;
        }

        String action = args[1].toLowerCase();
        switch (action) {
            case "add":
                if (args.length < 3) {
                    player.sendMessage(miniMessage.deserialize("<red>Usage: /is coop add <player></red>"));
                    return;
                }
                handleCoopAdd(player, island, args[2]);
                break;
            case "remove":
                if (args.length < 3) {
                    player.sendMessage(miniMessage.deserialize("<red>Usage: /is coop remove <player></red>"));
                    return;
                }
                handleCoopRemove(player, island, args[2]);
                break;
            case "role":
                if (args.length < 4) {
                    player.sendMessage(miniMessage.deserialize("<red>Usage: /is coop role <player> <role></red>"));
                    return;
                }
                handleCoopRole(player, island, args[2], args[3]);
                break;
            case "leave":
                handleCoopLeave(player, island);
                break;
            case "accept":
                handleCoopAccept(player);
                break;
            case "reject":
                handleCoopReject(player);
                break;
            case "visit":
                if (args.length < 3) {
                    player.sendMessage(miniMessage.deserialize("<red>Usage: /is coop visit <player></red>"));
                    return;
                }
                handleCoopVisit(player, args[2]);
                break;
            case "list":
                handleCoopList(player, island);
                break;
            default:
                player.sendMessage(miniMessage.deserialize("<red>Usage: /is coop <add|remove|role|leave|accept|reject|visit|list> [player] [role]</red>"));
                player.sendMessage(miniMessage.deserialize("<yellow>Use '/is coop visit <player>' to visit an island in survival mode if you have permission.</yellow>"));
        }
    }

    private void handleCoopAdd(Player player, Island island, String targetPlayerName) {
        if (!island.canManageCoop(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to manage coop members!</red>"));
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        // Check if it's a valid player name by checking if UUID is not null and name is not null
        // This is more lenient than hasPlayedBefore() which only works for players who joined THIS server
        if (targetPlayer.getName() == null) {
            player.sendMessage(miniMessage.deserialize("<red>Player " + targetPlayerName + " not found!</red>"));
            return;
        }

        if (island.getCoopMembers().containsKey(targetPlayer.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " is already a coop member!</red>"));
            return;
        }

        island.addCoopMember(targetPlayer.getUniqueId(), Island.CoopRole.MEMBER);
        plugin.getIslandManager().saveIsland(island);
        player.sendMessage(miniMessage.deserialize("<green>Added " + targetPlayerName + " as a coop member!</green>"));

        // Notify target player if online
        Player targetOnline = targetPlayer.getPlayer();
        if (targetOnline != null) {
            targetOnline.sendMessage(miniMessage.deserialize("<green>You've been added as a coop member to " + player.getName() + "'s island!</green>"));
        }
    }

    private void handleCoopRemove(Player player, Island island, String targetPlayerName) {
        if (!island.canManageCoop(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to manage coop members!</red>"));
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        if (!island.getCoopMembers().containsKey(targetPlayer.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " is not a coop member!</red>"));
            return;
        }

        island.removeCoopMember(targetPlayer.getUniqueId());
        plugin.getIslandManager().saveIsland(island);
        player.sendMessage(miniMessage.deserialize("<green>Removed " + targetPlayerName + " from coop!</green>"));

        // Notify target player if online
        Player targetOnline = targetPlayer.getPlayer();
        if (targetOnline != null) {
            targetOnline.sendMessage(miniMessage.deserialize("<yellow>You've been removed from " + player.getName() + "'s island coop.</yellow>"));
        }
    }

    private void handleCoopRole(Player player, Island island, String targetPlayerName, String roleName) {
        if (!island.canManageCoop(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to manage coop roles!</red>"));
            return;
        }

        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        if (!island.getCoopMembers().containsKey(targetPlayer.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " is not a coop member!</red>"));
            return;
        }

        Island.CoopRole role;
        try {
            role = Island.CoopRole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(miniMessage.deserialize("<red>Invalid role! Available roles: VISITOR, MEMBER, ADMIN, CO_OWNER</red>"));
            return;
        }

        if (role == Island.CoopRole.OWNER) {
            player.sendMessage(miniMessage.deserialize("<red>Cannot assign OWNER role!</red>"));
            return;
        }

        island.setCoopRole(targetPlayer.getUniqueId(), role);
        plugin.getIslandManager().saveIsland(island);
        player.sendMessage(miniMessage.deserialize("<green>Set " + targetPlayerName + "'s role to " + role.name() + "!</green>"));

        // Notify target player if online
        Player targetOnline = targetPlayer.getPlayer();
        if (targetOnline != null) {
            targetOnline.sendMessage(miniMessage.deserialize("<green>Your role on " + player.getName() + "'s island has been changed to " + role.name() + "!</green>"));
        }
    }

    private void handleCoopLeave(Player player, Island island) {
        if (island.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You cannot leave your own island! Use /is delete to delete it.</red>"));
            return;
        }

        if (!island.getCoopMembers().containsKey(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You are not a coop member of this island!</red>"));
            return;
        }

        island.removeCoopMember(player.getUniqueId());
        plugin.getIslandManager().saveIsland(island);
        player.sendMessage(miniMessage.deserialize("<yellow>You have left the island coop.</yellow>"));

        // Notify island owner if online
        Player owner = Bukkit.getPlayer(island.getOwner());
        if (owner != null) {
            owner.sendMessage(miniMessage.deserialize("<yellow>" + player.getName() + " has left your island coop.</yellow>"));
        }
    }

    private void handleCoopAccept(Player player) {
        // TODO: Implement coop invitation system
        player.sendMessage(miniMessage.deserialize("<yellow>Coop invitation system coming soon!</yellow>"));
    }

    private void handleCoopReject(Player player) {
        // TODO: Implement coop invitation system
        player.sendMessage(miniMessage.deserialize("<yellow>Coop invitation system coming soon!</yellow>"));
    }

    private void handleCoopVisit(Player player, String targetPlayerName) {
        // Check if player has permission to use coop visit
        if (!player.hasPermission("skyeblock.island.visit")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }
        
        // Find target player and their island
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        if (targetPlayer.getName() == null) {
            player.sendMessage(miniMessage.deserialize("<red>Player " + targetPlayerName + " not found!</red>"));
            return;
        }
        
        Island targetIsland = plugin.getIslandManager().getIsland(targetPlayer.getUniqueId());
        if (targetIsland == null) {
            player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " doesn't have an island!</red>"));
            return;
        }
        
        // Check if player has coop access to the island
        Island.CoopRole role = targetIsland.getCoopRole(player.getUniqueId());
        // Set to survival mode if player has appropriate coop role (MEMBER or higher)
        if (role.getLevel() >= Island.CoopRole.MEMBER.getLevel()) {
            player.setGameMode(GameMode.SURVIVAL);
        }
        
        // Teleport player to the island
        boolean success = plugin.getIslandManager().teleportToIsland(player, targetPlayer.getUniqueId());
        if (success) {
            player.sendMessage(miniMessage.deserialize("<green>Visiting " + targetPlayerName + "'s island as coop member!</green>"));
        } else {
            player.sendMessage(miniMessage.deserialize("<red>Failed to teleport to " + targetPlayerName + "'s island!</red>"));
        }
    }

    private void handleCoopList(Player player, Island island) {
        Map<UUID, Island.CoopRole> coopMembers = island.getCoopMembers();
        if (coopMembers.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("<yellow>No coop members on this island.</yellow>"));
            return;
        }

        player.sendMessage(miniMessage.deserialize("<gold>=== Island Coop Members ===</gold>"));
        player.sendMessage(miniMessage.deserialize("<aqua>Owner:</aqua> <white>" + Bukkit.getOfflinePlayer(island.getOwner()).getName() + "</white>"));
        
        for (Map.Entry<UUID, Island.CoopRole> entry : coopMembers.entrySet()) {
            String playerName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            String roleName = entry.getValue().name();
            player.sendMessage(miniMessage.deserialize("<yellow>" + roleName + ":</yellow> <white>" + playerName + "</white>"));
        }
    }

    private void handleVoteCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island.vote")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /is vote <player></red>"));
            return;
        }

        String targetPlayerName = args[1];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
        
        if (targetPlayer.getName() == null) {
            player.sendMessage(miniMessage.deserialize("<red>Player " + targetPlayerName + " not found!</red>"));
            return;
        }

        Island targetIsland = plugin.getIslandManager().getIsland(targetPlayer.getUniqueId());
        if (targetIsland == null) {
            player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " doesn't have an island!</red>"));
            return;
        }

        if (targetIsland.getOwner().equals(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You cannot vote for your own island!</red>"));
            return;
        }

        // Check if player has already voted
        if (targetIsland.hasVoted(player.getUniqueId())) {
            if (!targetIsland.canVote(player.getUniqueId())) {
                long lastVote = targetIsland.getLastVote(player.getUniqueId());
                long timeSinceVote = System.currentTimeMillis() - lastVote;
                long hoursLeft = 23 - (timeSinceVote / (60 * 60 * 1000));
                player.sendMessage(miniMessage.deserialize("<red>You can vote again in " + hoursLeft + " hours!</red>"));
                return;
            }
        }

        targetIsland.addVote(player.getUniqueId());
        plugin.getIslandManager().saveIsland(targetIsland);
        
        int totalVotes = targetIsland.getVotes().size();
        player.sendMessage(miniMessage.deserialize("<green>Voted for " + targetPlayerName + "'s island! Total votes: " + totalVotes + "</green>"));

        // Notify island owner if online
        Player targetOnline = targetPlayer.getPlayer();
        if (targetOnline != null) {
            targetOnline.sendMessage(miniMessage.deserialize("<green>" + player.getName() + " voted for your island! Total votes: " + totalVotes + "</green>"));
        }
    }

    private void handleSetCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island.set")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        if (!island.canManageSettings(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to set island locations!</red>"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /is set <home|visit></red>"));
            return;
        }

        String locationType = args[1].toLowerCase();
        Location playerLocation = player.getLocation();

        switch (locationType) {
            case "home":
                island.setCustomHomeLocation(playerLocation);
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Home location set!</green>"));
                break;
            case "visit":
                island.setCustomVisitLocation(playerLocation);
                plugin.getIslandManager().saveIsland(island);
                player.sendMessage(miniMessage.deserialize("<green>Visit location set!</green>"));
                break;
            default:
                player.sendMessage(miniMessage.deserialize("<red>Usage: /is set <home|visit></red>"));
        }
    }

    private void handleDefaultCommand(Player player, String[] args) {
        if (!player.hasPermission("skyeblock.island")) {
            plugin.sendMessage(player, "no-permission");
            return;
        }

        Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
        if (island == null) {
            plugin.sendMessage(player, "island-not-found");
            return;
        }

        if (!island.canManageSettings(player.getUniqueId())) {
            player.sendMessage(miniMessage.deserialize("<red>You don't have permission to change default settings!</red>"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /is default <setting> [value]</red>"));
            return;
        }

        // TODO: Implement default command settings
        player.sendMessage(miniMessage.deserialize("<yellow>Default command system coming soon!</yellow>"));
    }

    private void sendHelp(Player player) {
        plugin.sendMessage(player, "help-header");
        plugin.sendMessage(player, "help-create");
        plugin.sendMessage(player, "help-create-nether");
        player.sendMessage(miniMessage.deserialize("<yellow>/island types</yellow> <gray>-</gray> <gray>Show available island types</gray>"));
        plugin.sendMessage(player, "help-teleport");
        player.sendMessage(miniMessage.deserialize("<yellow>/island visit [player]</yellow> <gray>-</gray> <gray>Visit islands or open island browser</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island lock/unlock</yellow> <gray>-</gray> <gray>Lock or unlock your island</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island edit <title|desc|icon> [value]</yellow> <gray>-</gray> <gray>Customize your island</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island coop <add|remove|role|visit|list> [player] [role]</yellow> <gray>-</gray> <gray>Manage coop members</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island vote <player></yellow> <gray>-</gray> <gray>Vote for an island</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island set <home|visit></yellow> <gray>-</gray> <gray>Set custom teleport locations</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island settings</yellow> <gray>-</gray> <gray>Configure island settings</gray>"));
        player.sendMessage(miniMessage.deserialize("<yellow>/island help</yellow> <gray>-</gray> <gray>Show this help message</gray>"));
        
        if (player.hasPermission("skyeblock.admin")) {
            player.sendMessage(miniMessage.deserialize("<red>Admin Commands:</red>"));
            plugin.sendMessage(player, "help-delete");
            plugin.sendMessage(player, "help-list");
            player.sendMessage(miniMessage.deserialize("<yellow>/island status</yellow> <gray>-</gray> <gray>Show server status</gray>"));
        }
        
        plugin.sendMessage(player, "help-hub");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // First argument - subcommands
            List<String> subCommands = new ArrayList<>(Arrays.asList("create", "home", "visit", "lock", "unlock", "edit", 
                    "coop", "vote", "set", "default", "types", "settings", "help"));
            
            // Add delete if player has permission
            if (sender.hasPermission("skyeblock.island.delete")) {
                subCommands.add("delete");
            }
            
            // Add admin commands if player has admin permission
            if (sender.hasPermission("skyeblock.admin")) {
                subCommands.addAll(Arrays.asList("list", "status"));
            }
            
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "create":
                    // Arguments for create command - island types (including multi-word types)
                    String[] availableTypes = plugin.getSchematicManager().getAvailableSchematics();
                    
                    // Join current arguments to build partial type name
                    StringBuilder currentType = new StringBuilder();
                    for (int i = 1; i < args.length; i++) {
                        if (i > 1) currentType.append(" ");
                        currentType.append(args[i]);
                    }
                    String partialType = currentType.toString();
                    
                    for (String type : availableTypes) {
                        if (type.toLowerCase().startsWith(partialType.toLowerCase())) {
                            completions.add(type);
                        }
                    }
                    break;
                    
                case "visit":
                case "vote":
                    if (args.length == 2) {
                        // Online player names
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    }
                    break;
                    
                case "edit":
                    if (args.length == 2) {
                        List<String> editTypes = Arrays.asList("title", "desc", "description", "icon");
                        for (String type : editTypes) {
                            if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(type);
                            }
                        }
                    }
                    break;
                    
                case "coop":
                    if (args.length == 2) {
                        List<String> coopActions = Arrays.asList("add", "remove", "role", "leave", "accept", "reject", "visit", "list");
                        for (String action : coopActions) {
                            if (action.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(action);
                            }
                        }
                    } else if (args.length == 3 && (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("role") || args[1].equalsIgnoreCase("visit"))) {
                        // Player names for coop commands
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                                completions.add(player.getName());
                            }
                        }
                    } else if (args.length == 4 && args[1].equalsIgnoreCase("role")) {
                        // Role names
                        List<String> roles = Arrays.asList("VISITOR", "MEMBER", "ADMIN", "CO_OWNER");
                        for (String role : roles) {
                            if (role.toLowerCase().startsWith(args[3].toLowerCase())) {
                                completions.add(role);
                            }
                        }
                    }
                    break;
                    
                case "set":
                    if (args.length == 2) {
                        List<String> setTypes = Arrays.asList("home", "visit");
                        for (String type : setTypes) {
                            if (type.toLowerCase().startsWith(args[1].toLowerCase())) {
                                completions.add(type);
                            }
                        }
                    }
                    break;
            }
        }

        return completions;
    }
}
