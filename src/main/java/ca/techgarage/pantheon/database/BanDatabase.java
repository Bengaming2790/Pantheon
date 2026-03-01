package ca.techgarage.pantheon.database;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.sql.*;

public class BanDatabase {

    private static Connection connection;

    public static void init(MinecraftServer server) {
        try {
            Path dbPath = Path.of(FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("pantheon-temp-ban.db")
                    .toString());
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                CREATE TABLE IF NOT EXISTS temp_bans (
                    player_uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    banned_at INTEGER NOT NULL,
                    ban_expires_at INTEGER NOT NULL
                    );
                """);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
        ALTER TABLE temp_bans
        ADD COLUMN player_name TEXT
    """);
        } catch (SQLException ignored) {}
    }

    public static Connection getConnection() {
        return connection;
    }
}
