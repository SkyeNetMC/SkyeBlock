package skyeblock.nobleskye.dev.skyeblock.managers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import org.bukkit.Location;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;

public class SchematicManager {
    private final SkyeBlockPlugin plugin;
    private final File schematicFolder;

    public SchematicManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        
        // Get schematic path from config, default to plugin data folder
        String schematicPath = plugin.getConfig().getString("schematics.template-path", "schematics");
        
        // Handle both absolute and relative paths
        if (schematicPath.startsWith("/") || schematicPath.contains(":")) {
            // Absolute path
            this.schematicFolder = new File(schematicPath);
        } else {
            // Relative path - relative to plugin data folder
            this.schematicFolder = new File(plugin.getDataFolder(), schematicPath);
        }
        
        // Create schematics folder if it doesn't exist
        if (!schematicFolder.exists()) {
            schematicFolder.mkdirs();
        }
        
        // Create default schematic files if they don't exist
        createDefaultSchematics();
    }

    public boolean pasteSchematic(String schematicName, Location location) {
        plugin.getLogger().info("Attempting to paste schematic: " + schematicName + " at " + location);
        
        File schematicFile = new File(schematicFolder, schematicName + ".schem");
        if (!schematicFile.exists()) {
            // Try .schematic extension as fallback
            schematicFile = new File(schematicFolder, schematicName + ".schematic");
            if (!schematicFile.exists()) {
                plugin.getLogger().severe("SCHEMATIC ERROR: File not found for '" + schematicName + "'");
                plugin.getLogger().severe("  Searched for: " + new File(schematicFolder, schematicName + ".schem").getAbsolutePath());
                plugin.getLogger().severe("  Also searched: " + schematicFile.getAbsolutePath());
                plugin.getLogger().severe("  Schematic folder: " + schematicFolder.getAbsolutePath());
                plugin.getLogger().severe("  Folder exists: " + schematicFolder.exists());
                if (schematicFolder.exists()) {
                    File[] files = schematicFolder.listFiles();
                    if (files != null && files.length > 0) {
                        plugin.getLogger().severe("  Files in schematic folder:");
                        for (File file : files) {
                            plugin.getLogger().severe("    - " + file.getName());
                        }
                    } else {
                        plugin.getLogger().severe("  Schematic folder is empty!");
                    }
                }
                return false;
            }
        }

        try {
            plugin.getLogger().info("Loading schematic file: " + schematicFile.getAbsolutePath());
            
            // Load the schematic
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().severe("SCHEMATIC ERROR: Unknown format for file: " + schematicFile.getName());
                plugin.getLogger().severe("  Supported formats: .schem, .schematic");
                return false;
            }

            plugin.getLogger().info("Schematic format detected: " + format.getName());

            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
                plugin.getLogger().info("Schematic loaded successfully. Dimensions: " + 
                    clipboard.getDimensions().x() + "x" + 
                    clipboard.getDimensions().y() + "x" + 
                    clipboard.getDimensions().z());
            }

            // Paste the schematic
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                
                Operations.complete(operation);
                plugin.getLogger().info("Schematic '" + schematicName + "' pasted successfully at " + 
                    location.getX() + ", " + location.getY() + ", " + location.getZ());
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "SCHEMATIC ERROR: Failed to paste schematic '" + schematicName + "'", e);
            plugin.getLogger().severe("  Location: " + location);
            plugin.getLogger().severe("  World: " + (location.getWorld() != null ? location.getWorld().getName() : "null"));
            plugin.getLogger().severe("  File: " + schematicFile.getAbsolutePath());
            return false;
        }
    }

    private void createDefaultSchematics() {
        // Create info file about schematics
        File infoFile = new File(schematicFolder, "README.txt");
        if (!infoFile.exists()) {
            try {
                infoFile.createNewFile();
                java.nio.file.Files.write(infoFile.toPath(), 
                    ("SkyeBlock Schematics Folder\n" +
                     "==============================\n\n" +
                     "Place your island template schematics here:\n" +
                     "- island-normal.schem (or .schematic)\n" +
                     "- island-spruce.schem (or .schematic)\n" +
                     "- island-cherry.schem (or .schematic)\n\n" +
                     "You can create these schematics using WorldEdit:\n" +
                     "1. Build your island template\n" +
                     "2. Select the area with //wand\n" +
                     "3. Copy with //copy\n" +
                     "4. Save with //schem save <name>\n\n" +
                     "The schematics will be pasted at the specified coordinates when players create islands.").getBytes());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not create schematic info file", e);
            }
        }
    }

    public boolean pasteIslandTemplate(String islandType, Location location) {
        plugin.getLogger().info("Pasting island template for type: " + islandType);
        
        String templateName = plugin.getConfig().getString("island.templates." + islandType);
        if (templateName == null) {
            plugin.getLogger().info("No template mapping found for '" + islandType + "', using type name directly");
            // Use islandType directly if no template mapping exists
            templateName = islandType;
        } else {
            plugin.getLogger().info("Template mapping found: " + islandType + " -> " + templateName);
        }
        
        return pasteSchematic(templateName, location);
    }

    /**
     * Get list of available schematic files (without extensions)
     */
    public String[] getAvailableSchematics() {
        if (!schematicFolder.exists()) {
            return new String[0];
        }
        
        File[] files = schematicFolder.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".schem") || name.toLowerCase().endsWith(".schematic"));
        
        if (files == null) {
            return new String[0];
        }
        
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            // Remove file extension
            int dotIndex = fileName.lastIndexOf('.');
            names[i] = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        }
        
        return names;
    }

    public File getSchematicFolder() {
        return schematicFolder;
    }
}
