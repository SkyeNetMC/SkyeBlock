package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Command to manage island permissions
 * Usage: /islandpermissions <player> [target_player]
 */
public class PermissionCommand implements CommandExecutor {
    private final SkyeBlockPlugin plugin;

    public PermissionCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;

        // Check if player is on an island
        String islandId = getPlayerIslandId(player);
        if (islandId == null) {
            player.sendMessage(Component.text("You must be on an island to use this command!", NamedTextColor.RED));
            return true;
        }

        Island island = plugin.getIslandManager().getIslandById(islandId);
        if (island == null) {
            player.sendMessage(Component.text("Invalid island!", NamedTextColor.RED));
            return true;
        }

        // Check permissions
        if (!island.getOwnerUUID().equals(player.getUniqueId()) && 
            !player.hasPermission("skyeblock.admin.permissions")) {
            player.sendMessage(Component.text("You don't have permission to manage island permissions!", NamedTextColor.RED));
            return true;
        }

        UUID targetPlayerUUID;
        
        if (args.length == 0) {
            // Manage permissions for yourself
            targetPlayerUUID = player.getUniqueId();
        } else if (args.length == 1) {
            // Manage permissions for specified player
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                // Try to get offline player UUID
                try {
                    targetPlayerUUID = UUID.fromString(args[0]);
                } catch (IllegalArgumentException e) {
                    player.sendMessage(Component.text("Player not found: " + args[0], NamedTextColor.RED));
                    return true;
                }
            } else {
                targetPlayerUUID = targetPlayer.getUniqueId();
            }
        } else {
            player.sendMessage(Component.text("Usage: /islandpermissions [player]", NamedTextColor.YELLOW));
            return true;
        }

        // Open the permission management GUI
        plugin.getPermissionManagementGUI().openPermissionGUI(player, islandId, targetPlayerUUID);
        
        return true;
    }

    /**
     * Get the island ID from the player's current world
     */
    private String getPlayerIslandId(Player player) {
        String worldName = player.getWorld().getName();
        
        // Check if player is in an island world (starts with "island-")
        if (!worldName.startsWith("island-")) {
            return null;
        }
        
        // Return the world name as the island ID
        return worldName;
    }
}
