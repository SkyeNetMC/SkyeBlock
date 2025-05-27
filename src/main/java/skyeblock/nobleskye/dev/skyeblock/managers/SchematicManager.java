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
        this.schematicFolder = new File(plugin.getDataFolder(), "schematics");
        
        // Create schematics folder if it doesn't exist
        if (!schematicFolder.exists()) {
            schematicFolder.mkdirs();
        }
        
        // Create default schematic files if they don't exist
        createDefaultSchematics();
    }

    public boolean pasteIslandTemplate(String islandType, Location location) {
        String templateName = plugin.getConfig().getString("island.templates." + islandType);
        if (templateName == null) {
            plugin.getLogger().warning("No template found for island type: " + islandType);
            return false;
        }

        File schematicFile = new File(schematicFolder, templateName + ".schem");
        if (!schematicFile.exists()) {
            // Try .schematic extension as fallback
            schematicFile = new File(schematicFolder, templateName + ".schematic");
            if (!schematicFile.exists()) {
                plugin.getLogger().warning("Schematic file not found: " + templateName);
                return false;
            }
        }

        try {
            // Load the schematic
            ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
            if (format == null) {
                plugin.getLogger().warning("Unknown schematic format for file: " + schematicFile.getName());
                return false;
            }

            Clipboard clipboard;
            try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                clipboard = reader.read();
            }

            // Paste the schematic
            try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                        .ignoreAirBlocks(false)
                        .build();
                
                Operations.complete(operation);
                editSession.flushSession();
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to paste schematic: " + templateName, e);
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

    public File getSchematicFolder() {
        return schematicFolder;
    }
}
