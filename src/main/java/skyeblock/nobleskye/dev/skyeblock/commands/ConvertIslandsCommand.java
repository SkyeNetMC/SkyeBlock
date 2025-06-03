package skyeblock.nobleskye.dev.skyeblock.commands;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Command to convert old island format (island-<username>) to new UUID-based format
 */
public class ConvertIslandsCommand implements CommandExecutor, TabCompleter {
    private final SkyeBlockPlugin plugin;
    private final MiniMessage miniMessage;
    
    public ConvertIslandsCommand(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.convert")) {
            sender.sendMessage(miniMessage.deserialize("<red>You don't have permission to use this command!</red>"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(miniMessage.deserialize("<yellow>Usage: /convertislands <scan|convert> [username]</yellow>"));
            sender.sendMessage(miniMessage.deserialize("<gray>  scan - Scan for old format islands</gray>"));
            sender.sendMessage(miniMessage.deserialize("<gray>  convert [username] - Convert specific island or all islands</gray>"));
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "scan":
                scanOldIslands(sender);
                break;
            case "convert":
                if (args.length > 1) {
                    convertSpecificIsland(sender, args[1]);
                } else {
                    convertAllIslands(sender);
                }
                break;
            default:
                sender.sendMessage(miniMessage.deserialize("<red>Unknown action: " + action + "</red>"));
                return true;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("skyeblock.admin.convert")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("scan", "convert");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("convert")) {
            // Return list of potential usernames from old islands
            return getOldIslandUsernames();
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Scan for old format islands and report findings
     */
    private void scanOldIslands(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<yellow>🔍 Scanning for old format islands...</yellow>"));
        
        File serverRoot = Bukkit.getWorldContainer();
        Map<String, File> oldIslands = findOldIslands(serverRoot);
        
        if (oldIslands.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<green>✓ No old format islands found!</green>"));
            return;
        }
        
        sender.sendMessage(miniMessage.deserialize("<yellow>📋 Found " + oldIslands.size() + " old format islands:</yellow>"));
        
        for (Map.Entry<String, File> entry : oldIslands.entrySet()) {
            String username = entry.getKey();
            File worldFolder = entry.getValue();
            
            // Determine if this is a nether island
            boolean isNetherIsland = worldFolder.getName().endsWith("_nether");
            String islandTypeDisplay = isNetherIsland ? " (Nether)" : " (Overworld)";
            
            // Try to get UUID for this username
            OfflinePlayer player = Bukkit.getOfflinePlayer(username);
            UUID uuid = player.getUniqueId();
            
            sender.sendMessage(miniMessage.deserialize("<gray>  • " + username + islandTypeDisplay + " (" + uuid + ") -> " + worldFolder.getName() + "</gray>"));
        }
        
        sender.sendMessage(miniMessage.deserialize("<yellow>💡 Use '/convertislands convert' to convert all islands</yellow>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>💡 Use '/convertislands convert <username>' to convert specific island</yellow>"));
    }
    
    /**
     * Convert all old format islands
     */
    private void convertAllIslands(CommandSender sender) {
        sender.sendMessage(miniMessage.deserialize("<yellow>🔄 Starting conversion of all old format islands...</yellow>"));
        
        File serverRoot = Bukkit.getWorldContainer();
        Map<String, File> oldIslands = findOldIslands(serverRoot);
        
        if (oldIslands.isEmpty()) {
            sender.sendMessage(miniMessage.deserialize("<green>✓ No old format islands found to convert!</green>"));
            return;
        }
        
        int converted = 0;
        int failed = 0;
        
        for (Map.Entry<String, File> entry : oldIslands.entrySet()) {
            String username = entry.getKey();
            File worldFolder = entry.getValue();
            
            try {
                if (convertIsland(username, worldFolder, sender)) {
                    converted++;
                } else {
                    failed++;
                }
            } catch (Exception e) {
                sender.sendMessage(miniMessage.deserialize("<red>❌ Failed to convert island for " + username + ": " + e.getMessage() + "</red>"));
                plugin.getLogger().warning("Failed to convert island for " + username + ": " + e.getMessage());
                failed++;
            }
        }
        
        sender.sendMessage(miniMessage.deserialize("<green>✅ Conversion complete!</green>"));
        sender.sendMessage(miniMessage.deserialize("<yellow>  Converted: " + converted + "</yellow>"));
        if (failed > 0) {
            sender.sendMessage(miniMessage.deserialize("<red>  Failed: " + failed + "</red>"));
        }
    }
    
    /**
     * Convert a specific island by username
     */
    private void convertSpecificIsland(CommandSender sender, String username) {
        sender.sendMessage(miniMessage.deserialize("<yellow>🔄 Converting island for " + username + "...</yellow>"));
        
        File serverRoot = Bukkit.getWorldContainer();
        Map<String, File> oldIslands = findOldIslands(serverRoot);
        
        File worldFolder = oldIslands.get(username);
        if (worldFolder == null) {
            sender.sendMessage(miniMessage.deserialize("<red>❌ No old format island found for " + username + "</red>"));
            return;
        }
        
        try {
            if (convertIsland(username, worldFolder, sender)) {
                sender.sendMessage(miniMessage.deserialize("<green>✅ Successfully converted island for " + username + "!</green>"));
            } else {
                sender.sendMessage(miniMessage.deserialize("<red>❌ Failed to convert island for " + username + "</red>"));
            }
        } catch (Exception e) {
            sender.sendMessage(miniMessage.deserialize("<red>❌ Error converting island for " + username + ": " + e.getMessage() + "</red>"));
            plugin.getLogger().warning("Failed to convert island for " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Find all old format islands in the server root
     */
    private Map<String, File> findOldIslands(File serverRoot) {
        Map<String, File> oldIslands = new HashMap<>();
        
        // Look for patterns like "island-<username>" and "island-<username>_nether" in the root directory
        File[] files = serverRoot.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && file.getName().startsWith("island-")) {
                    String name = file.getName();
                    String username;
                    
                    // Handle nether islands (island-username_nether)
                    if (name.endsWith("_nether")) {
                        // Extract username (everything between "island-" and "_nether")
                        username = name.substring(7, name.length() - 7); // "island-".length() = 7, "_nether".length() = 7
                    } else {
                        // Extract username (everything after "island-")
                        username = name.substring(7); // "island-".length() = 7
                    }
                    
                    // Skip if it looks like it's already in new format (contains UUIDs)
                    if (username.contains("-") && username.length() > 20) {
                        continue; // Likely already converted or new format
                    }
                    
                    // Verify it's a world folder (has level.dat)
                    File levelDat = new File(file, "level.dat");
                    if (levelDat.exists()) {
                        oldIslands.put(username, file);
                    }
                }
            }
        }
        
        return oldIslands;
    }
    
    /**
     * Convert a single island from old format to new format
     */
    private boolean convertIsland(String username, File oldWorldFolder, CommandSender sender) throws Exception {
        // Get player UUID
        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        UUID uuid = player.getUniqueId();
        
        if (uuid == null) {
            sender.sendMessage(miniMessage.deserialize("<red>❌ Could not find UUID for player: " + username + "</red>"));
            return false;
        }
        
        // Check if player already has an island in the new system
        if (plugin.getIslandManager().hasIsland(uuid)) {
            sender.sendMessage(miniMessage.deserialize("<yellow>⚠ Player " + username + " already has an island in the new system, skipping...</yellow>"));
            return false;
        }
        
        // Determine if this is a nether island
        boolean isNetherIsland = oldWorldFolder.getName().endsWith("_nether");
        String islandType = isNetherIsland ? "nether" : determineIslandType(oldWorldFolder);
        
        // Create new island ID
        String newIslandId = "island-" + islandType + "-" + uuid.toString();
        
        // Create destination directories
        File skyBlockDir = new File(Bukkit.getWorldContainer(), "skyeblock");
        File destinationDir;
        if (isNetherIsland) {
            destinationDir = new File(skyBlockDir, "nether");
        } else {
            destinationDir = new File(skyBlockDir, "islands");
        }
        File newWorldFolder = new File(destinationDir, newIslandId);
        
        // Create directories if they don't exist
        destinationDir.mkdirs();
        
        String destinationType = isNetherIsland ? "nether" : "overworld";
        sender.sendMessage(miniMessage.deserialize("<gray>  📂 Moving " + destinationType + " world folder: " + oldWorldFolder.getName() + " -> " + newWorldFolder.getName() + "</gray>"));
        
        // Move the world folder
        moveDirectory(oldWorldFolder.toPath(), newWorldFolder.toPath());
        
        // Create island object and add to manager
        World world = plugin.getWorldManager().getIslandWorld(newIslandId);
        if (world == null) {
            // Try to load the world manually
            world = Bukkit.createWorld(new org.bukkit.WorldCreator(newWorldFolder.getName()));
        }
        
        if (world != null) {
            // Create island location (center of world)
            Location islandLocation = new Location(world, 0, 100, 0);
            
            // Create new island object
            Island island = new Island(uuid, islandType, islandLocation);
            island.setDisplayTitle(username + "'s " + (isNetherIsland ? "Nether " : "") + "Island (Converted)");
            island.setDisplayDescription("Converted from old format");
            
            // Add to island manager
            plugin.getIslandManager().getPlayerIslands().put(uuid, island);
            plugin.getIslandManager().saveIsland(island);
            
            // Create default settings
            plugin.getIslandSettingsManager().createDefaultSettings(newIslandId);
            
            sender.sendMessage(miniMessage.deserialize("<green>  ✅ Converted " + username + "'s island to UUID format</green>"));
            return true;
        } else {
            sender.sendMessage(miniMessage.deserialize("<red>  ❌ Failed to load world for " + username + "'s island</red>"));
            return false;
        }
    }
    
    /**
     * Try to determine island type from world folder contents
     */
    private String determineIslandType(File worldFolder) {
        // Look for signs of different island types
        File netherFolder = new File(worldFolder, "DIM-1");
        if (netherFolder.exists()) {
            return "nether";
        }
        
        // Default to classic
        return "classic";
    }
    
    /**
     * Move a directory from source to destination
     */
    private void moveDirectory(Path source, Path destination) throws Exception {
        if (Files.exists(destination)) {
            throw new Exception("Destination directory already exists: " + destination);
        }
        
        Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * Get list of old island usernames for tab completion
     */
    private List<String> getOldIslandUsernames() {
        File serverRoot = Bukkit.getWorldContainer();
        Map<String, File> oldIslands = findOldIslands(serverRoot);
        return new ArrayList<>(oldIslands.keySet());
    }
}
