package ca.techgarage.pantheon.database;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.sql.*;

public class BanDatabase {

    private static Connection connection;

    public static void init(MinecraftServer server) {
        try {

            Path dbPath = FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("database/pantheon-temp-ban.db");

            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);

            try (Statement stmt = connection.createStatement()) {

                stmt.execute("""
            CREATE TABLE IF NOT EXISTS temp_bans (
                player_uuid TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                banned_at INTEGER NOT NULL,
                ban_expires_at INTEGER NOT NULL,
                ban_reason TEXT NOT NULL
            );
            """);

                // Migration for old databases
                try {
                    stmt.execute("""
                ALTER TABLE temp_bans
                ADD COLUMN ban_reason TEXT DEFAULT 'No reason specified'
                """);
                } catch (SQLException ignored) {}

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}