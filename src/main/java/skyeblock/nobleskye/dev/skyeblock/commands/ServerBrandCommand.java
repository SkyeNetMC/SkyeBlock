package skyeblock.nobleskye.dev.skyeblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.util.ServerBrandUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command to manage server brand settings
 */
public class ServerBrandCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;

    public ServerBrandCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.serverbrand")) {
            plugin.sendMessage(sender instanceof Player ? (Player) sender : null, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendBrandInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                reloadBrand(sender);
                break;
            case "update":
                updateAllPlayers(sender);
                break;
            case "set":
                if (args.length < 2) {
                    sender.sendMessage("§c/serverbrand set <brand name>");
                    return true;
                }
                setBrandName(sender, args[1]);
                break;
            case "toggle":
                toggleBrand(sender);
                break;
            default:
                sendBrandHelp(sender);
                break;
        }

        return true;
    }

    private void sendBrandInfo(CommandSender sender) {
        boolean enabled = plugin.getConfig().getBoolean("server-brand.enabled", true);
        String brandName = plugin.getConfig().getString("server-brand.name", "LegitiSkyeSlimePaper");
        boolean periodicUpdates = plugin.getConfig().getBoolean("server-brand.periodic-updates", true);
        
        sender.sendMessage("§6§l===== Server Brand Settings =====");
        sender.sendMessage("§eEnabled: " + (enabled ? "§aYes" : "§cNo"));
        sender.sendMessage("§eBrand Name: §f" + brandName);
        sender.sendMessage("§ePeriodic Updates: " + (periodicUpdates ? "§aYes" : "§cNo"));
        sender.sendMessage("§eUpdate Interval: §f" + plugin.getConfig().getInt("server-brand.update-interval-minutes", 10) + " minutes");
        sender.sendMessage("§6§l==============================");
    }

    private void reloadBrand(CommandSender sender) {
        plugin.reloadConfig();
        
        boolean enabled = plugin.getConfig().getBoolean("server-brand.enabled", true);
        String brandName = plugin.getConfig().getString("server-brand.name", "LegitiSkyeSlimePaper");
        
        if (enabled) {
            // Apply server brand modification
            ServerBrandUtil.modifyServerBrand(plugin, brandName);
            sender.sendMessage("§aServer brand configuration reloaded. Brand name: §f" + brandName);
        } else {
            sender.sendMessage("§cServer brand feature is disabled in config.");
        }
    }

    private void updateAllPlayers(CommandSender sender) {
        boolean enabled = plugin.getConfig().getBoolean("server-brand.enabled", true);
        if (!enabled) {
            sender.sendMessage("§cServer brand feature is disabled in config.");
            return;
        }
        
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            try {
                // Update player brand using all available methods
                if (plugin.getServerBrandChanger() != null) {
                    plugin.getServerBrandChanger().updatePlayerBrand(player);
                }
                
                if (plugin.getServerBrandListener() != null) {
                    plugin.getServerBrandListener().updatePlayerBrand(player);
                }
                
                if (plugin.getSpigotBrandModifier() != null) {
                    plugin.getSpigotBrandModifier().updatePlayerBrand(player);
                }
                
                if (plugin.getPlayerJoinListener() != null) {
                    plugin.getPlayerJoinListener().updatePlayerBrand(player);
                }
                
                count++;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to update brand for player " + player.getName() + ": " + e.getMessage());
            }
        }
        
        sender.sendMessage("§aUpdated server brand for " + count + " player(s).");
    }

    private void setBrandName(CommandSender sender, String newBrandName) {
        if (newBrandName.isEmpty()) {
            sender.sendMessage("§cBrand name cannot be empty.");
            return;
        }
        
        // Update the configuration
        plugin.getConfig().set("server-brand.name", newBrandName);
        plugin.saveConfig();
        
        // Apply the new brand
        ServerBrandUtil.modifyServerBrand(plugin, newBrandName);
        
        sender.sendMessage("§aServer brand name set to: §f" + newBrandName);
        sender.sendMessage("§aPlayers will see the new brand on their next join or when you use §f/serverbrand update§a.");
    }

    private void toggleBrand(CommandSender sender) {
        boolean currentValue = plugin.getConfig().getBoolean("server-brand.enabled", true);
        plugin.getConfig().set("server-brand.enabled", !currentValue);
        plugin.saveConfig();
        
        if (!currentValue) {
            // If we're enabling the feature, apply the brand
            String brandName = plugin.getConfig().getString("server-brand.name", "LegitiSkyeSlimePaper");
            ServerBrandUtil.modifyServerBrand(plugin, brandName);
            sender.sendMessage("§aServer brand feature has been §f§lENABLED§a. Brand name: §f" + brandName);
        } else {
            sender.sendMessage("§cServer brand feature has been §f§lDISABLED§c. Players will see the default server brand.");
        }
    }

    private void sendBrandHelp(CommandSender sender) {
        sender.sendMessage("§6§l===== Server Brand Commands =====");
        sender.sendMessage("§e/serverbrand §7- View current settings");
        sender.sendMessage("§e/serverbrand reload §7- Reload configuration");
        sender.sendMessage("§e/serverbrand update §7- Update brand for all online players");
        sender.sendMessage("§e/serverbrand set <name> §7- Set custom brand name");
        sender.sendMessage("§e/serverbrand toggle §7- Enable/disable the feature");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.serverbrand")) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "update", "set", "toggle");
            String input = args[0].toLowerCase();
            return subcommands.stream()
                    .filter(s -> s.startsWith(input))
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
}
