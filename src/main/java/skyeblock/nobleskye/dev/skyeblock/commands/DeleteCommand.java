package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DeleteCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    
    public DeleteCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
            return true;
        }
        
        if (args.length == 0) {
            // Delete own island
            if (!player.hasPermission("skyeblock.island.delete")) {
                plugin.sendMessage(player, "no-permission");
                return true;
            }
            
            Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
            if (island == null) {
                player.sendMessage(miniMessage.deserialize("<red>You don't have an island to delete!</red>"));
                return true;
            }
            
            // Open confirmation GUI for own island
            plugin.getDeleteConfirmationGUI().openDeleteConfirmation(player, player.getUniqueId());
            
        } else if (args.length == 1) {
            // Admin delete another player's island
            if (!player.hasPermission("skyeblock.admin")) {
                player.sendMessage(miniMessage.deserialize("<red>You don't have permission to delete other players' islands!</red>"));
                return true;
            }
            
            String targetPlayerName = args[0];
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetPlayerName);
            
            if (targetPlayer.getName() == null) {
                player.sendMessage(miniMessage.deserialize("<red>Player " + targetPlayerName + " not found!</red>"));
                return true;
            }
            
            Island targetIsland = plugin.getIslandManager().getIsland(targetPlayer.getUniqueId());
            if (targetIsland == null) {
                player.sendMessage(miniMessage.deserialize("<red>" + targetPlayerName + " doesn't have an island!</red>"));
                return true;
            }
            
            // Open confirmation GUI for target player's island
            plugin.getDeleteConfirmationGUI().openDeleteConfirmation(player, targetPlayer.getUniqueId());
            
        } else {
            player.sendMessage(miniMessage.deserialize("<red>Usage: /delete [player]</red>"));
            player.sendMessage(miniMessage.deserialize("<gray>Use /delete to delete your own island</gray>"));
            player.sendMessage(miniMessage.deserialize("<gray>Use /delete <player> to delete another player's island (admin only)</gray>"));
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1 && sender.hasPermission("skyeblock.admin")) {
            // Tab complete player names for admins
            String partial = args[0].toLowerCase();
            
            // Add online players
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    Island island = plugin.getIslandManager().getIsland(player.getUniqueId());
                    if (island != null) { // Only show players who have islands
                        completions.add(player.getName());
                    }
                }
            }
            
            // Add offline players who have islands (limit to prevent lag)
            if (completions.size() < 10) {
                for (Island island : plugin.getIslandManager().getAllIslands()) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(island.getOwnerUUID());
                    if (player.getName() != null && player.getName().toLowerCase().startsWith(partial)) {
                        if (!completions.contains(player.getName())) {
                            completions.add(player.getName());
                            if (completions.size() >= 10) break; // Limit results
                        }
                    }
                }
            }
        }
        
        return completions;
    }
}
