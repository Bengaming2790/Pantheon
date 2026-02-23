package ca.techgarage.pantheon.database;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.sql.*;

public class BanDatabase {

    private static Connection connection;

    public static void init(MinecraftServer server) {
        try {
            Path dbPath = server.getRunDirectory().toAbsolutePath().resolve("tempbans.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS temp_bans (
                        player_uuid TEXT PRIMARY KEY,
                        banned_at INTEGER NOT NULL,
                        ban_expires_at INTEGER NOT NULL
                    );
                """);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
