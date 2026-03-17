package ca.techgarage.pantheon.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BanDatabase {

    private static HikariDataSource dataSource;

    public static void init(MinecraftServer server) {
        try {

            Path dbFolder = FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("database");

            Files.createDirectories(dbFolder);

            Path dbPath = dbFolder.resolve("pantheon-temp-ban.db");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setMaximumPoolSize(10);
            config.setPoolName("PantheonBanPool");

            dataSource = new HikariDataSource(config);

            try (Connection connection = dataSource.getConnection();
                 Statement stmt = connection.createStatement()) {

                stmt.execute("""
                CREATE TABLE IF NOT EXISTS temp_bans (
                    player_uuid TEXT PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    banned_at INTEGER NOT NULL,
                    ban_expires_at INTEGER NOT NULL,
                    ban_reason TEXT NOT NULL
                );
                """);

                try {
                    stmt.execute("""
                    ALTER TABLE temp_bans
                    ADD COLUMN ban_reason TEXT DEFAULT 'No reason specified'
                    """);
                } catch (SQLException ignored) {}

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}