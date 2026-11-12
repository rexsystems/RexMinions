package me.rexsystems.rexMinions.storage;

import me.rexsystems.rexMinions.RexMinions;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MinionDatabase {

    private final RexMinions plugin;
    private Connection connection;

    public MinionDatabase(RexMinions plugin) {
        this.plugin = plugin;
    }

    public synchronized void open() throws SQLException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new SQLException("Could not create the data folder.");
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQLite driver is missing from the jar.", ex);
        }
        File file = new File(plugin.getDataFolder(), "minions.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout=5000");
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS minions (
                        id TEXT PRIMARY KEY,
                        type TEXT NOT NULL,
                        level INTEGER NOT NULL,
                        world TEXT NOT NULL,
                        x DOUBLE NOT NULL,
                        y DOUBLE NOT NULL,
                        z DOUBLE NOT NULL,
                        yaw DOUBLE NOT NULL,
                        owner TEXT NOT NULL,
                        tool TEXT,
                        storage TEXT,
                        link_world TEXT,
                        link_x INTEGER,
                        link_y INTEGER,
                        link_z INTEGER,
                        balance DOUBLE NOT NULL DEFAULT 0,
                        cursor INTEGER NOT NULL DEFAULT 0
                    )
                    """);
        }
    }

    public synchronized List<StoredMinion> loadAll() {
        List<StoredMinion> rows = new ArrayList<>();
        if (connection == null) {
            return rows;
        }
        String sql = "SELECT id, type, level, world, x, y, z, yaw, owner, tool, storage, link_world, link_x, link_y, link_z, balance, cursor FROM minions";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                rows.add(new StoredMinion(
                        UUID.fromString(result.getString("id")),
                        result.getString("type"),
                        result.getInt("level"),
                        result.getString("world"),
                        result.getDouble("x"),
                        result.getDouble("y"),
                        result.getDouble("z"),
                        (float) result.getDouble("yaw"),
                        UUID.fromString(result.getString("owner")),
                        result.getString("tool"),
                        result.getString("storage"),
                        result.getString("link_world"),
                        boxed(result, "link_x"),
                        boxed(result, "link_y"),
                        boxed(result, "link_z"),
                        result.getDouble("balance"),
                        result.getInt("cursor")
                ));
            }
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not read minions.db: " + ex.getMessage());
        }
        return rows;
    }

    public synchronized void save(StoredMinion row) {
        if (connection == null || row == null) {
            return;
        }
        String sql = """
                INSERT INTO minions (
                    id, type, level, world, x, y, z, yaw, owner, tool, storage,
                    link_world, link_x, link_y, link_z, balance, cursor
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    type = excluded.type,
                    level = excluded.level,
                    world = excluded.world,
                    x = excluded.x,
                    y = excluded.y,
                    z = excluded.z,
                    yaw = excluded.yaw,
                    owner = excluded.owner,
                    tool = excluded.tool,
                    storage = excluded.storage,
                    link_world = excluded.link_world,
                    link_x = excluded.link_x,
                    link_y = excluded.link_y,
                    link_z = excluded.link_z,
                    balance = excluded.balance,
                    cursor = excluded.cursor
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, row.id().toString());
            statement.setString(2, row.type());
            statement.setInt(3, row.level());
            statement.setString(4, row.world());
            statement.setDouble(5, row.x());
            statement.setDouble(6, row.y());
            statement.setDouble(7, row.z());
            statement.setDouble(8, row.yaw());
            statement.setString(9, row.owner().toString());
            statement.setString(10, row.tool());
            statement.setString(11, row.storage());
            statement.setString(12, row.linkWorld());
            if (row.linkX() == null) {
                statement.setNull(13, java.sql.Types.INTEGER);
                statement.setNull(14, java.sql.Types.INTEGER);
                statement.setNull(15, java.sql.Types.INTEGER);
            } else {
                statement.setInt(13, row.linkX());
                statement.setInt(14, row.linkY());
                statement.setInt(15, row.linkZ());
            }
            statement.setDouble(16, row.balance());
            statement.setInt(17, row.cursor());
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not save minion " + row.id() + ": " + ex.getMessage());
        }
    }

    public synchronized void delete(UUID id) {
        if (connection == null || id == null) {
            return;
        }
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM minions WHERE id = ?")) {
            statement.setString(1, id.toString());
            statement.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not delete minion " + id + ": " + ex.getMessage());
        }
    }

    private static Integer boxed(ResultSet result, String column) throws SQLException {
        int value = result.getInt(column);
        return result.wasNull() ? null : value;
    }

    public synchronized void close() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().warning("Could not close minions.db: " + ex.getMessage());
        }
        connection = null;
    }

    public record StoredMinion(
            UUID id,
            String type,
            int level,
            String world,
            double x,
            double y,
            double z,
            float yaw,
            UUID owner,
            String tool,
            String storage,
            String linkWorld,
            Integer linkX,
            Integer linkY,
            Integer linkZ,
            double balance,
            int cursor
    ) {
    }
}
