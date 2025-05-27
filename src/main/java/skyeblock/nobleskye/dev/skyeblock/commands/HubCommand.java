package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HubCommand implements CommandExecutor {
    private final SkyeBlockPlugin plugin;

    public HubCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("skyeblock.hub")) {
            plugin.sendMessage(player, "no-permission");
            return true;
        }

        if (!plugin.getConfig().getBoolean("hub.enabled", true)) {
            plugin.sendMessage(player, "hub-not-configured");
            return true;
        }

        String hubWorldName = plugin.getConfig().getString("hub.world", "world");
        World hubWorld = Bukkit.getWorld(hubWorldName);

        if (hubWorld == null) {
            plugin.sendMessage(player, "hub-not-configured");
            return true;
        }

        double x = plugin.getConfig().getDouble("hub.spawn.x", 0);
        double y = plugin.getConfig().getDouble("hub.spawn.y", 100);
        double z = plugin.getConfig().getDouble("hub.spawn.z", 0);

        Location hubLocation = new Location(hubWorld, x, y, z);
        player.teleport(hubLocation);
        plugin.sendMessage(player, "teleported-to-hub");

        return true;
    }
}
