package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command handler for /warp
 */
public class WarpCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    
    public WarpCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize(
                plugin.getWarpConfig().getString("messages.player-only", "<red>This command can only be used by players!</red>")
            ));
            return true;
        }
        
        // Check basic permission
        if (!player.hasPermission("skyeblock.warp")) {
            player.sendMessage(miniMessage.deserialize(
                plugin.getWarpConfig().getString("messages.no-permission", "<red>You don't have permission to use warps!</red>")
            ));
            return true;
        }
        
        // If no arguments, open GUI
        if (args.length == 0) {
            plugin.getWarpGUI().openWarpGUI(player);
            return true;
        }
        
        // If argument provided, try to warp directly
        String warpName = args[0].toLowerCase();
        
        // Check if warp exists and player has access
        if (!plugin.getWarpManager().hasWarp(warpName)) {
            player.sendMessage(miniMessage.deserialize(
                plugin.getWarpConfig().getString("messages.warp-not-found", "<red>Warp '{warp}' not found!</red>")
                    .replace("{warp}", warpName)
            ));
            return true;
        }
        
        // Check specific warp permission
        if (!plugin.getWarpManager().hasPermission(player, warpName)) {
            player.sendMessage(miniMessage.deserialize(
                plugin.getWarpConfig().getString("messages.warp-no-permission", "<red>You don't have permission to use warp '{warp}'!</red>")
                    .replace("{warp}", warpName)
            ));
            return true;
        }
        
        // Attempt to warp
        plugin.getWarpManager().warpPlayer(player, warpName);
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            // Return available warps for the player
            return plugin.getWarpManager().getAvailableWarps(player)
                    .stream()
                    .map(warp -> warp.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
}
