package skyeblock.nobleskye.dev.skyeblock.managers;

import skyeblock.nobleskye.dev.skyeblock.SkyeBlockPlugin;
import skyeblock.nobleskye.dev.skyeblock.models.Island;
import skyeblock.nobleskye.dev.skyeblock.permissions.IslandPermission;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages H2 embedded database for island data, settings, and player data storage.
 * Modeled after LuckPerms' approach: single file-based H2 database in the plugin data folder.
 */
public class DatabaseManager {
    private final SkyeBlockPlugin plugin;
    private Connection connection;
    private final File dbFile;

    public DatabaseManager(SkyeBlockPlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "skyeblock");
    }

    /**
     * Open the H2 connection and create tables if needed
     */
    public void open() {
        try {
            plugin.getDataFolder().mkdirs();
            Class.forName("org.h2.Driver");
            String url = "jdbc:h2:file:" + dbFile.getAbsolutePath() + ";MODE=MySQL;DB_CLOSE_ON_EXIT=TRUE;AUTO_RECONNECT=TRUE";
            connection = DriverManager.getConnection(url, "sa", "");
            plugin.getLogger().info("H2 database opened: " + dbFile.getName());
            createTables();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to open H2 database", e);
        }
    }

    /**
     * Close the H2 connection
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("H2 database closed");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to close H2 database", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Islands table - core island data
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS islands (
                    owner_uuid VARCHAR(36) PRIMARY KEY,
                    island_id VARCHAR(128),
                    island_type VARCHAR(64),
                    display_title VARCHAR(256),
                    display_description VARCHAR(512),
                    display_icon TEXT,
                    locked BOOLEAN DEFAULT FALSE,
                    adventure_mode_for_visitors BOOLEAN DEFAULT TRUE,
                    last_online_time BIGINT DEFAULT 0,
                    location_world VARCHAR(128),
                    location_x DOUBLE DEFAULT 0,
                    location_y DOUBLE DEFAULT 0,
                    location_z DOUBLE DEFAULT 0,
                    location_yaw FLOAT DEFAULT 0,
                    location_pitch FLOAT DEFAULT 0,
                    home_world VARCHAR(128),
                    home_x DOUBLE,
                    home_y DOUBLE,
                    home_z DOUBLE,
                    home_yaw FLOAT,
                    home_pitch FLOAT,
                    visit_world VARCHAR(128),
                    visit_x DOUBLE,
                    visit_y DOUBLE,
                    visit_z DOUBLE,
                    visit_yaw FLOAT,
                    visit_pitch FLOAT
                )
            """);

            // Visitor permissions table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_visitor_permissions (
                    owner_uuid VARCHAR(36) PRIMARY KEY,
                    can_break_blocks BOOLEAN DEFAULT FALSE,
                    can_place_blocks BOOLEAN DEFAULT FALSE,
                    can_open_containers BOOLEAN DEFAULT FALSE,
                    can_pickup_items BOOLEAN DEFAULT FALSE,
                    can_drop_items BOOLEAN DEFAULT FALSE,
                    can_interact_entities BOOLEAN DEFAULT FALSE,
                    can_use_pvp BOOLEAN DEFAULT FALSE,
                    can_use_redstone BOOLEAN DEFAULT FALSE
                )
            """);

            // Coop members table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_coop_members (
                    owner_uuid VARCHAR(36) NOT NULL,
                    coop_uuid VARCHAR(36) NOT NULL,
                    role VARCHAR(32) NOT NULL DEFAULT 'MEMBER',
                    PRIMARY KEY (owner_uuid, coop_uuid)
                )
            """);

            // Pending invites table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_pending_invites (
                    owner_uuid VARCHAR(36) NOT NULL,
                    invite_uuid VARCHAR(36) NOT NULL,
                    PRIMARY KEY (owner_uuid, invite_uuid)
                )
            """);

            // Votes table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_votes (
                    owner_uuid VARCHAR(36) NOT NULL,
                    voter_uuid VARCHAR(36) NOT NULL,
                    vote_time BIGINT NOT NULL,
                    PRIMARY KEY (owner_uuid, voter_uuid)
                )
            """);

            // Custom permissions table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_custom_permissions (
                    owner_uuid VARCHAR(36) NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    permission_node VARCHAR(128) NOT NULL,
                    PRIMARY KEY (owner_uuid, player_uuid, permission_node)
                )
            """);

            // Island settings (gamerules) table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS island_settings (
                    island_id VARCHAR(128) NOT NULL,
                    game_rule VARCHAR(128) NOT NULL,
                    value VARCHAR(256),
                    value_type VARCHAR(16) NOT NULL DEFAULT 'BOOLEAN',
                    PRIMARY KEY (island_id, game_rule)
                )
            """);

            // Player data table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_data (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    last_world VARCHAR(128),
                    last_x DOUBLE,
                    last_y DOUBLE,
                    last_z DOUBLE,
                    last_yaw FLOAT,
                    last_pitch FLOAT
                )
            """);

            plugin.getLogger().info("H2 database tables initialized");
        }
    }

    // ==================== Island Data Operations ====================

    /**
     * Save all islands to the database (transactional)
     */
    public void saveAllIslands(Map<UUID, Island> islands) {
        try {
            connection.setAutoCommit(false);

            // Clear all existing data
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM island_custom_permissions");
                stmt.executeUpdate("DELETE FROM island_votes");
                stmt.executeUpdate("DELETE FROM island_pending_invites");
                stmt.executeUpdate("DELETE FROM island_coop_members");
                stmt.executeUpdate("DELETE FROM island_visitor_permissions");
                stmt.executeUpdate("DELETE FROM islands");
            }

            // Insert all islands
            for (Island island : islands.values()) {
                insertIsland(island);
            }

            connection.commit();
            connection.setAutoCommit(true);
            plugin.getLogger().info("Saved " + islands.size() + " islands to H2 database");
        } catch (SQLException e) {
            rollback();
            plugin.getLogger().log(Level.SEVERE, "Failed to save islands to H2 database", e);
        }
    }

    /**
     * Load all islands from the database
     */
    public Map<UUID, Island> loadAllIslands() {
        Map<UUID, Island> islands = new HashMap<>();
        if (connection == null) return islands;

        try {
            // Load all islands from the islands table
            try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM islands");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    try {
                        UUID ownerUUID = UUID.fromString(rs.getString("owner_uuid"));
                        String islandId = rs.getString("island_id");
                        String islandType = rs.getString("island_type");

                        // Load location
                        String worldName = rs.getString("location_world");
                        if (worldName == null) {
                            plugin.getLogger().warning("Skipping island " + islandId + " - no world specified");
                            continue;
                        }

                        World world = getOrCreateWorld(worldName, islandId);
                        if (world == null) {
                            plugin.getLogger().warning("Skipping island " + islandId + " - world " + worldName + " could not be loaded");
                            continue;
                        }

                        double x = rs.getDouble("location_x");
                        double y = rs.getDouble("location_y");
                        double z = rs.getDouble("location_z");
                        float yaw = rs.getFloat("location_yaw");
                        float pitch = rs.getFloat("location_pitch");
                        Location location = new Location(world, x, y, z, yaw, pitch);

                        Island island = new Island(ownerUUID, islandType, location);

                        // Set basic properties
                        String title = rs.getString("display_title");
                        if (title != null) island.setDisplayTitle(title);
                        String desc = rs.getString("display_description");
                        if (desc != null) island.setDisplayDescription(desc);
                        island.setLocked(rs.getBoolean("locked"));
                        island.setAdventureModeForVisitors(rs.getBoolean("adventure_mode_for_visitors"));

                        // Load display icon
                        String iconJson = rs.getString("display_icon");
                        if (iconJson != null && !iconJson.isEmpty()) {
                            island.setDisplayIcon(deserializeItemStack(iconJson));
                        }

                        // Load last online time
                        long lastOnline = rs.getLong("last_online_time");
                        if (lastOnline > 0) {
                            try {
                                java.lang.reflect.Field field = Island.class.getDeclaredField("lastOnlineTime");
                                field.setAccessible(true);
                                field.set(island, lastOnline);
                            } catch (Exception e) {
                                island.updateLastOnlineTime();
                            }
                        }

                        // Load home location
                        String homeWorldName = rs.getString("home_world");
                        if (homeWorldName != null) {
                            World homeWorld = getOrCreateWorld(homeWorldName, islandId);
                            if (homeWorld == null) homeWorld = world;
                            island.setHomeLocation(new Location(homeWorld,
                                rs.getDouble("home_x"), rs.getDouble("home_y"), rs.getDouble("home_z"),
                                rs.getFloat("home_yaw"), rs.getFloat("home_pitch")));
                        }

                        // Load visit location
                        String visitWorldName = rs.getString("visit_world");
                        if (visitWorldName != null) {
                            World visitWorld = getOrCreateWorld(visitWorldName, islandId);
                            if (visitWorld == null) visitWorld = world;
                            island.setVisitLocation(new Location(visitWorld,
                                rs.getDouble("visit_x"), rs.getDouble("visit_y"), rs.getDouble("visit_z"),
                                rs.getFloat("visit_yaw"), rs.getFloat("visit_pitch")));
                        }

                        // Load visitor permissions
                        loadVisitorPermissions(ownerUUID, island);

                        // Load coop members
                        loadCoopMembers(ownerUUID, island);

                        // Load pending invites
                        loadPendingInvites(ownerUUID, island);

                        // Load votes
                        loadVotes(ownerUUID, island);

                        // Load custom permissions
                        loadCustomPermissions(ownerUUID, island);

                        islands.put(ownerUUID, island);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to load island from H2: " + e.getMessage());
                    }
                }
            }

            plugin.getLogger().info("Loaded " + islands.size() + " islands from H2 database");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load islands from H2 database", e);
        }

        return islands;
    }

    /**
     * Save a single island to the database
     */
    public void saveIsland(Island island) {
        try {
            connection.setAutoCommit(false);
            deleteIslandData(island.getOwnerUUID());
            insertIsland(island);
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            rollback();
            plugin.getLogger().log(Level.SEVERE, "Failed to save island to H2: " + island.getIslandId(), e);
        }
    }

    /**
     * Delete an island from the database
     */
    public void deleteIsland(UUID ownerUUID) {
        try {
            connection.setAutoCommit(false);
            deleteIslandData(ownerUUID);
            connection.commit();
            connection.setAutoCommit(true);
            plugin.getLogger().info("Deleted island data for " + ownerUUID + " from H2");
        } catch (SQLException e) {
            rollback();
            plugin.getLogger().log(Level.SEVERE, "Failed to delete island from H2: " + ownerUUID, e);
        }
    }

    private void deleteIslandData(UUID ownerUUID) throws SQLException {
        String uuid = ownerUUID.toString();
        try (PreparedStatement ps1 = connection.prepareStatement("DELETE FROM island_custom_permissions WHERE owner_uuid = ?");
             PreparedStatement ps2 = connection.prepareStatement("DELETE FROM island_votes WHERE owner_uuid = ?");
             PreparedStatement ps3 = connection.prepareStatement("DELETE FROM island_pending_invites WHERE owner_uuid = ?");
             PreparedStatement ps4 = connection.prepareStatement("DELETE FROM island_coop_members WHERE owner_uuid = ?");
             PreparedStatement ps5 = connection.prepareStatement("DELETE FROM island_visitor_permissions WHERE owner_uuid = ?");
             PreparedStatement ps6 = connection.prepareStatement("DELETE FROM islands WHERE owner_uuid = ?")) {
            ps1.setString(1, uuid); ps1.executeUpdate();
            ps2.setString(1, uuid); ps2.executeUpdate();
            ps3.setString(1, uuid); ps3.executeUpdate();
            ps4.setString(1, uuid); ps4.executeUpdate();
            ps5.setString(1, uuid); ps5.executeUpdate();
            ps6.setString(1, uuid); ps6.executeUpdate();
        }

        // Also delete settings for this island's islandId
        // We need to find the islandId first from the islands table before deletion
        // This is handled in the caller
    }

    private void insertIsland(Island island) throws SQLException {
        String uuid = island.getOwnerUUID().toString();

        // Insert main island data
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO islands (owner_uuid, island_id, island_type, display_title, display_description, display_icon, " +
                "locked, adventure_mode_for_visitors, last_online_time, " +
                "location_world, location_x, location_y, location_z, location_yaw, location_pitch, " +
                "home_world, home_x, home_y, home_z, home_yaw, home_pitch, " +
                "visit_world, visit_x, visit_y, visit_z, visit_yaw, visit_pitch) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")) {

            ps.setString(1, uuid);
            ps.setString(2, island.getIslandId());
            ps.setString(3, island.getIslandType());
            ps.setString(4, island.getDisplayTitle());
            ps.setString(5, island.getDisplayDescription());
            ps.setString(6, island.getDisplayIcon() != null ? serializeItemStack(island.getDisplayIcon()) : null);
            ps.setBoolean(7, island.isLocked());
            ps.setBoolean(8, island.isAdventureModeForVisitors());
            ps.setLong(9, island.getLastOnlineTime());

            // Location
            Location loc = island.getLocation();
            if (loc != null && loc.getWorld() != null) {
                ps.setString(10, loc.getWorld().getName());
                ps.setDouble(11, loc.getX());
                ps.setDouble(12, loc.getY());
                ps.setDouble(13, loc.getZ());
                ps.setFloat(14, loc.getYaw());
                ps.setFloat(15, loc.getPitch());
            }

            // Home location
            Location home = island.getHomeLocation();
            if (home != null && home.getWorld() != null && home != loc) {
                ps.setString(16, home.getWorld().getName());
                ps.setDouble(17, home.getX());
                ps.setDouble(18, home.getY());
                ps.setDouble(19, home.getZ());
                ps.setFloat(20, home.getYaw());
                ps.setFloat(21, home.getPitch());
            }

            // Visit location
            Location visit = island.getVisitLocation();
            if (visit != null && visit.getWorld() != null && visit != home) {
                ps.setString(22, visit.getWorld().getName());
                ps.setDouble(23, visit.getX());
                ps.setDouble(24, visit.getY());
                ps.setDouble(25, visit.getZ());
                ps.setFloat(26, visit.getYaw());
                ps.setFloat(27, visit.getPitch());
            }

            ps.executeUpdate();
        }

        // Insert visitor permissions
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO island_visitor_permissions " +
                "(owner_uuid, can_break_blocks, can_place_blocks, can_open_containers, can_pickup_items, " +
                "can_drop_items, can_interact_entities, can_use_pvp, can_use_redstone) " +
                "VALUES (?,?,?,?,?,?,?,?,?)")) {
            ps.setString(1, uuid);
            ps.setBoolean(2, island.canVisitorBreakBlocks());
            ps.setBoolean(3, island.canVisitorPlaceBlocks());
            ps.setBoolean(4, island.canVisitorOpenContainers());
            ps.setBoolean(5, island.canVisitorPickupItems());
            ps.setBoolean(6, island.canVisitorDropItems());
            ps.setBoolean(7, island.canVisitorInteractWithEntities());
            ps.setBoolean(8, island.canVisitorUsePvp());
            ps.setBoolean(9, island.canVisitorUseRedstone());
            ps.executeUpdate();
        }

        // Insert coop members
        for (Map.Entry<UUID, Island.CoopRole> entry : island.getCoopMembers().entrySet()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO island_coop_members (owner_uuid, coop_uuid, role) VALUES (?,?,?)")) {
                ps.setString(1, uuid);
                ps.setString(2, entry.getKey().toString());
                ps.setString(3, entry.getValue().name());
                ps.executeUpdate();
            }
        }

        // Insert pending invites
        for (UUID invite : island.getPendingInvites()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO island_pending_invites (owner_uuid, invite_uuid) VALUES (?,?)")) {
                ps.setString(1, uuid);
                ps.setString(2, invite.toString());
                ps.executeUpdate();
            }
        }

        // Insert votes (only recent ones - last 7 days)
        long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
        for (Map.Entry<UUID, Long> entry : island.getVotes().entrySet()) {
            if (entry.getValue() > sevenDaysAgo) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO island_votes (owner_uuid, voter_uuid, vote_time) VALUES (?,?,?)")) {
                    ps.setString(1, uuid);
                    ps.setString(2, entry.getKey().toString());
                    ps.setLong(3, entry.getValue());
                    ps.executeUpdate();
                }
            }
        }

        // Insert custom permissions
        for (Map.Entry<UUID, Set<IslandPermission>> entry : island.getCustomPlayerPermissions().entrySet()) {
            for (IslandPermission perm : entry.getValue()) {
                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO island_custom_permissions (owner_uuid, player_uuid, permission_node) VALUES (?,?,?)")) {
                    ps.setString(1, uuid);
                    ps.setString(2, entry.getKey().toString());
                    ps.setString(3, perm.getNode());
                    ps.executeUpdate();
                }
            }
        }
    }

    private void loadVisitorPermissions(UUID ownerUUID, Island island) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM island_visitor_permissions WHERE owner_uuid = ?")) {
            ps.setString(1, ownerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    island.setVisitorCanBreakBlocks(rs.getBoolean("can_break_blocks"));
                    island.setVisitorCanPlaceBlocks(rs.getBoolean("can_place_blocks"));
                    island.setVisitorCanOpenContainers(rs.getBoolean("can_open_containers"));
                    island.setVisitorCanPickupItems(rs.getBoolean("can_pickup_items"));
                    island.setVisitorCanDropItems(rs.getBoolean("can_drop_items"));
                    island.setVisitorCanInteractWithEntities(rs.getBoolean("can_interact_entities"));
                    island.setVisitorCanUsePvp(rs.getBoolean("can_use_pvp"));
                    island.setVisitorCanUseRedstone(rs.getBoolean("can_use_redstone"));
                }
            }
        }
    }

    private void loadCoopMembers(UUID ownerUUID, Island island) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT coop_uuid, role FROM island_coop_members WHERE owner_uuid = ?")) {
            ps.setString(1, ownerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID coopUuid = UUID.fromString(rs.getString("coop_uuid"));
                    Island.CoopRole role = Island.CoopRole.valueOf(rs.getString("role"));
                    island.addCoopMember(coopUuid, role);
                }
            }
        }
    }

    private void loadPendingInvites(UUID ownerUUID, Island island) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT invite_uuid FROM island_pending_invites WHERE owner_uuid = ?")) {
            ps.setString(1, ownerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    island.addPendingInvite(UUID.fromString(rs.getString("invite_uuid")));
                }
            }
        }
    }

    private void loadVotes(UUID ownerUUID, Island island) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT voter_uuid, vote_time FROM island_votes WHERE owner_uuid = ?")) {
            ps.setString(1, ownerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    island.getVotes().put(
                        UUID.fromString(rs.getString("voter_uuid")),
                        rs.getLong("vote_time")
                    );
                }
            }
        }
    }

    private void loadCustomPermissions(UUID ownerUUID, Island island) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT player_uuid, permission_node FROM island_custom_permissions WHERE owner_uuid = ?")) {
            ps.setString(1, ownerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID playerUuid = UUID.fromString(rs.getString("player_uuid"));
                    String node = rs.getString("permission_node");
                    for (IslandPermission perm : IslandPermission.values()) {
                        if (perm.getNode().equals(node)) {
                            island.addCustomPermission(playerUuid, perm);
                            break;
                        }
                    }
                }
            }
        }
    }

    // ==================== Island Settings Operations ====================

    /**
     * Load all island settings from the database
     */
    public Map<String, Map<GameRule<?>, Object>> loadAllIslandSettings() {
        Map<String, Map<GameRule<?>, Object>> allSettings = new HashMap<>();
        if (connection == null) return allSettings;

        try (PreparedStatement ps = connection.prepareStatement("SELECT DISTINCT island_id FROM island_settings");
             ResultSet rs = ps.executeQuery()) {

            List<String> islandIds = new ArrayList<>();
            while (rs.next()) {
                islandIds.add(rs.getString("island_id"));
            }

            for (String islandId : islandIds) {
                allSettings.put(islandId, loadIslandSettings(islandId));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load island settings from H2", e);
        }

        return allSettings;
    }

    /**
     * Load settings for a specific island
     */
    public Map<GameRule<?>, Object> loadIslandSettings(String islandId) {
        Map<GameRule<?>, Object> settings = new HashMap<>();

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT game_rule, value, value_type FROM island_settings WHERE island_id = ?")) {
            ps.setString(1, islandId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ruleName = rs.getString("game_rule");
                    String valueStr = rs.getString("value");
                    String valueType = rs.getString("value_type");

                    GameRule<?> gameRule = GameRule.getByName(ruleName);
                    if (gameRule == null) continue;

                    if ("BOOLEAN".equals(valueType) && gameRule.getType() == Boolean.class) {
                        settings.put(gameRule, Boolean.parseBoolean(valueStr));
                    } else if ("INTEGER".equals(valueType) && gameRule.getType() == Integer.class) {
                        settings.put(gameRule, Integer.parseInt(valueStr));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load settings for island " + islandId, e);
        }

        return settings;
    }

    /**
     * Save all island settings to the database
     */
    public void saveAllIslandSettings(Map<String, Map<GameRule<?>, Object>> allSettings) {
        try {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM island_settings");
            }

            for (Map.Entry<String, Map<GameRule<?>, Object>> entry : allSettings.entrySet()) {
                saveIslandSettings(entry.getKey(), entry.getValue());
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            rollback();
            plugin.getLogger().log(Level.SEVERE, "Failed to save island settings to H2", e);
        }
    }

    /**
     * Save settings for a specific island
     */
    public void saveIslandSettings(String islandId, Map<GameRule<?>, Object> settings) {
        try (PreparedStatement ps = connection.prepareStatement(
                "MERGE INTO island_settings (island_id, game_rule, value, value_type) KEY (island_id, game_rule) VALUES (?,?,?,?)")) {

            for (Map.Entry<GameRule<?>, Object> entry : settings.entrySet()) {
                GameRule<?> gameRule = entry.getKey();
                Object value = entry.getValue();

                ps.setString(1, islandId);
                ps.setString(2, gameRule.getName());

                if (gameRule.getType() == Boolean.class) {
                    ps.setString(3, String.valueOf(value));
                    ps.setString(4, "BOOLEAN");
                } else if (gameRule.getType() == Integer.class) {
                    ps.setString(3, String.valueOf(value));
                    ps.setString(4, "INTEGER");
                }
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save settings for island " + islandId, e);
        }
    }

    /**
     * Delete settings for a specific island
     */
    public void deleteIslandSettings(String islandId) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM island_settings WHERE island_id = ?")) {
            ps.setString(1, islandId);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete settings for island " + islandId, e);
        }
    }

    // ==================== Player Data Operations ====================

    /**
     * Load all player data from the database
     */
    public Map<UUID, Location> loadAllPlayerData() {
        Map<UUID, Location> lastLocations = new HashMap<>();
        if (connection == null) return lastLocations;

        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM player_data");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                try {
                    UUID playerUUID = UUID.fromString(rs.getString("player_uuid"));
                    String worldName = rs.getString("last_world");
                    if (worldName == null) continue;

                    World world = Bukkit.getWorld(worldName);
                    if (world == null) continue;

                    Location loc = new Location(world,
                        rs.getDouble("last_x"), rs.getDouble("last_y"), rs.getDouble("last_z"),
                        rs.getFloat("last_yaw"), rs.getFloat("last_pitch"));
                    lastLocations.put(playerUUID, loc);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load player data row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load player data from H2", e);
        }

        return lastLocations;
    }

    /**
     * Save all player data to the database
     */
    public void saveAllPlayerData(Map<UUID, Location> lastLocations) {
        try {
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("DELETE FROM player_data");
            }

            for (Map.Entry<UUID, Location> entry : lastLocations.entrySet()) {
                Location loc = entry.getValue();
                if (loc == null || loc.getWorld() == null) continue;

                try (PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO player_data (player_uuid, last_world, last_x, last_y, last_z, last_yaw, last_pitch) " +
                        "VALUES (?,?,?,?,?,?,?)")) {
                    ps.setString(1, entry.getKey().toString());
                    ps.setString(2, loc.getWorld().getName());
                    ps.setDouble(3, loc.getX());
                    ps.setDouble(4, loc.getY());
                    ps.setDouble(5, loc.getZ());
                    ps.setFloat(6, (float) loc.getYaw());
                    ps.setFloat(7, (float) loc.getPitch());
                    ps.executeUpdate();
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
            plugin.getLogger().info("Saved player data for " + lastLocations.size() + " players to H2");
        } catch (SQLException e) {
            rollback();
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data to H2", e);
        }
    }

    /**
     * Save a single player's data
     */
    public void savePlayerData(UUID playerUUID, Location location) {
        if (location == null || location.getWorld() == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "MERGE INTO player_data (player_uuid, last_world, last_x, last_y, last_z, last_yaw, last_pitch) " +
                "KEY (player_uuid) VALUES (?,?,?,?,?,?,?)")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, location.getWorld().getName());
            ps.setDouble(3, location.getX());
            ps.setDouble(4, location.getY());
            ps.setDouble(5, location.getZ());
            ps.setFloat(6, (float) location.getYaw());
            ps.setFloat(7, (float) location.getPitch());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player data for " + playerUUID, e);
        }
    }

    /**
     * Delete a player's data
     */
    public void deletePlayerData(UUID playerUUID) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM player_data WHERE player_uuid = ?")) {
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete player data for " + playerUUID, e);
        }
    }

    // ==================== Utility Methods ====================

    private World getOrCreateWorld(String worldName, String islandId) {
        World world = Bukkit.getWorld(worldName);
        if (world == null && plugin.getWorldManager() != null) {
            world = plugin.getWorldManager().getOrLoadIslandWorld(islandId);
            if (world != null && world.getName().equals(plugin.getWorldManager().getSkyBlockWorld().getName())) {
                return null;
            }
        }
        return world;
    }

    @SuppressWarnings("unchecked")
    private String serializeItemStack(ItemStack item) {
        if (item == null) return null;
        Map<String, Object> serialized = item.serialize();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : serialized.entrySet()) {
            if (sb.length() > 0) sb.append(";");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private ItemStack deserializeItemStack(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            Map<String, Object> map = new HashMap<>();
            String[] pairs = data.split(";");
            for (String pair : pairs) {
                int eq = pair.indexOf('=');
                if (eq > 0) {
                    map.put(pair.substring(0, eq), pair.substring(eq + 1));
                }
            }
            return ItemStack.deserialize(map);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize ItemStack: " + e.getMessage());
            return null;
        }
    }

    private void rollback() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to rollback transaction", e);
        }
    }
}
