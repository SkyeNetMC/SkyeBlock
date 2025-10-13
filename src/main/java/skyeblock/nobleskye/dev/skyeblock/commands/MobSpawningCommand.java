package skyeblock.nobleskye.dev.skyeblock.commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;

public class MobSpawningCommand implements CommandExecutor {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;

    public MobSpawningCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.mobspawning")) {
            sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to use this command!</red>"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
            case "update":
                handleReload(sender);
                break;
            case "status":
                handleStatus(sender);
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<yellow>Reloading mob spawning settings for all island worlds...</yellow>"));
        
        // Reload the config first
        plugin.reloadConfig();
        
        // Update all island worlds with new settings
        plugin.getWorldManager().updateMobSpawningForAllIslands();
        
        sender.sendMessage(miniMessage.deserialize("<green>✓ Mob spawning settings reloaded and applied to all island worlds!</green>"));
    }

    private void handleStatus(CommandSender sender) {
        boolean allowMonsters = plugin.getConfig().getBoolean("world.spawning.allow-monsters", true);
        boolean allowAnimals = plugin.getConfig().getBoolean("world.spawning.allow-animals", true);
        String difficulty = plugin.getConfig().getString("world.spawning.difficulty", "normal");

        sender.sendMessage(miniMessage.deserialize("<gold>=== Mob Spawning Settings ===</gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>Allow Monsters:</yellow> " + (allowMonsters ? "<green>Enabled" : "<red>Disabled")));
        sender.sendMessage(miniMessage.deserialize("<yellow>Allow Animals:</yellow> " + (allowAnimals ? "<green>Enabled" : "<red>Disabled")));
        sender.sendMessage(miniMessage.deserialize("<yellow>Difficulty:</yellow> <aqua>" + difficulty.toUpperCase()));
        sender.sendMessage(miniMessage.deserialize("<gray>Use <yellow>/mobspawning reload</yellow> to apply changes after modifying config.yml</gray>"));
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<gold>=== Mob Spawning Commands ===</gold>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/mobspawning status</yellow> <gray>-</gray> <gray>Show current mob spawning settings</gray>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>/mobspawning reload</yellow> <gray>-</gray> <gray>Reload config and apply to all islands</gray>"));
        sender.sendMessage(miniMessage.deserialize("<gray>Configure settings in config.yml under world.spawning section</gray>"));
    }
}
