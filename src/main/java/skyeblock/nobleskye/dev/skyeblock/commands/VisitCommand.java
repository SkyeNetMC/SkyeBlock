package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class VisitCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;

    public VisitCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("skyeblock.island.visit")) {
            plugin.sendMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            // Open visit GUI
            plugin.getIslandVisitGUI().openVisitGUI(player);
        } else {
            // Direct visit to player
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

            boolean success = plugin.getIslandManager().teleportToIsland(player, targetPlayer.getUniqueId());
            if (success) {
                player.sendMessage(miniMessage.deserialize("<green>Visiting " + targetPlayerName + "'s island!</green>"));
            } else {
                player.sendMessage(miniMessage.deserialize("<red>Cannot visit " + targetPlayerName + "'s island. It might be locked.</red>"));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Tab complete player names
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}
