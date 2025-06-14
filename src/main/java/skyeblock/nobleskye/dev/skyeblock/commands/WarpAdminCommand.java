package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
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
 * Command handler for /warpadmin
 */
public class WarpAdminCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    
    public WarpAdminCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check admin permission
        if (!sender.hasPermission("skyeblock.admin.warp")) {
            sender.sendMessage(miniMessage.deserialize(
                plugin.getWarpConfig().getString("messages.no-permission", "<red>You don't have permission to use this command!</red>")
            ));
            return true;
        }
        
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "reset" -> handleReset(sender, args);
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender);
            case "info" -> handleInfo(sender, args);
            default -> showHelp(sender);
        }
        
        return true;
    }
    
    private void handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(miniMessage.deserialize("<red>This command can only be used by players!</red>"));
            return;
        }
        
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /warpadmin create <warp_name></red>"));
            return;
        }
        
        String warpName = args[1].toLowerCase();
        Location location = player.getLocation();
        
        // Create warp at player's current location
        if (plugin.getWarpManager().createWarp(warpName, location)) {
            sender.sendMessage(miniMessage.deserialize(
                "<green>Warp '{warp}' created at your current location!</green>"
                    .replace("{warp}", warpName)
            ));
        } else {
            sender.sendMessage(miniMessage.deserialize(
                "<red>Failed to create warp '{warp}'. It may already exist!</red>"
                    .replace("{warp}", warpName)
            ));
        }
    }
    
    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /warpadmin delete <warp_name></red>"));
            return;
        }
        
        String warpName = args[1].toLowerCase();
        
        if (plugin.getWarpManager().deleteWarp(warpName)) {
            sender.sendMessage(miniMessage.deserialize(
                "<green>Warp '{warp}' deleted successfully!</green>"
                    .replace("{warp}", warpName)
            ));
        } else {
            sender.sendMessage(miniMessage.deserialize(
                "<red>Failed to delete warp '{warp}'. It may not exist!</red>"
                    .replace("{warp}", warpName)
            ));
        }
    }
    
    private void handleReset(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /warpadmin reset <nether|end></red>"));
            return;
        }
        
        String worldType = args[1].toLowerCase();
        
        if (!worldType.equals("nether") && !worldType.equals("end")) {
            sender.sendMessage(miniMessage.deserialize("<red>Invalid world type! Use 'nether' or 'end'.</red>"));
            return;
        }
        
        // Force reset resource world
        plugin.getResourceWorldManager().forceResetWorld(worldType);
        
        sender.sendMessage(miniMessage.deserialize(
            "<green>Resource world '{world}' reset initiated!</green>"
                .replace("{world}", worldType)
        ));
    }
    
    private void handleReload(CommandSender sender) {
        try {
            plugin.reloadWarpConfig();
            plugin.getWarpManager().initializeWarps();
            plugin.getResourceWorldManager().initializeResourceWorlds();
            
            sender.sendMessage(miniMessage.deserialize("<green>Warp configuration reloaded successfully!</green>"));
        } catch (Exception e) {
            sender.sendMessage(miniMessage.deserialize("<red>Failed to reload configuration: " + e.getMessage() + "</red>"));
            plugin.getLogger().severe("Failed to reload warp configuration: " + e.getMessage());
        }
    }
    
    private void handleList(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold>Available Warps:</gold>"));
        
        plugin.getWarpManager().getAllWarps().forEach(warp -> {
            String status = warp.isEnabled() ? "<green>Enabled</green>" : "<red>Disabled</red>";
            sender.sendMessage(miniMessage.deserialize(
                "<gray>- <yellow>{name}</yellow> ({status}) - Slot {slot}</gray>"
                    .replace("{name}", warp.getName())
                    .replace("{status}", status)
                    .replace("{slot}", String.valueOf(warp.getSlot()))
            ));
        });
    }
    
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /warpadmin info <warp_name></red>"));
            return;
        }
        
        String warpName = args[1].toLowerCase();
        var warp = plugin.getWarpManager().getWarp(warpName);
        
        if (warp == null) {
            sender.sendMessage(miniMessage.deserialize(
                "<red>Warp '{warp}' not found!</red>".replace("{warp}", warpName)
            ));
            return;
        }
        
        sender.sendMessage(miniMessage.deserialize("<gold>Warp Information:</gold>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Name: <yellow>" + warp.getName() + "</yellow></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Display Name: <yellow>" + warp.getDisplayName() + "</yellow></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Permission: <yellow>" + warp.getPermission() + "</yellow></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Material: <yellow>" + warp.getMaterial() + "</yellow></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Slot: <yellow>" + warp.getSlot() + "</yellow></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Cost: <yellow>" + warp.getCost() + "</yellow></gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Enabled: <yellow>" + warp.isEnabled() + "</yellow></gray>"));
        
        if (warp.getLocation() != null) {
            Location loc = warp.getLocation();
            sender.sendMessage(miniMessage.deserialize(
                "<gray>Location: <yellow>" + loc.getWorld().getName() + " " + 
                loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "</yellow></gray>"
            ));
        }
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold>Warp Admin Commands:</gold>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/warpadmin create <name> - Create warp at current location</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/warpadmin delete <name> - Delete a warp</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/warpadmin reset <nether|end> - Force reset resource world</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/warpadmin reload - Reload warp configuration</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/warpadmin list - List all warps</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>/warpadmin info <name> - Show warp information</gray>"));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.warp")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "reset", "reload", "list", "info")
                    .stream()
                    .filter(cmd -> cmd.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "delete", "info" -> {
                    return plugin.getWarpManager().getAllWarps()
                            .stream()
                            .map(warp -> warp.getName())
                            .filter(name -> name.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "reset" -> {
                    return Arrays.asList("nether", "end")
                            .stream()
                            .filter(world -> world.startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }
        
        return new ArrayList<>();
    }
}
