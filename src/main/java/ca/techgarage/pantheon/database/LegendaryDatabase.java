package ca.techgarage.pantheon.database;

import ca.techgarage.pantheon.items.ModItems;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LegendaryDatabase {

    private static HikariDataSource dataSource;

    public static void init(MinecraftServer server) {
        try {

            Path dbFolder = FabricLoader.getInstance()
                    .getGameDir()
                    .resolve("database");

            Files.createDirectories(dbFolder);

            Path dbPath = dbFolder.resolve("pantheon-legendary.db");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + dbPath);
            config.setMaximumPoolSize(10);
            config.setPoolName("PantheonLegendaryPool");

            dataSource = new HikariDataSource(config);

            try (Connection connection = dataSource.getConnection();
                 Statement stmt = connection.createStatement()) {

                stmt.execute("""
                CREATE TABLE IF NOT EXISTS legendary_holders (
                    item_id TEXT PRIMARY KEY,
                    player_uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL
                );
                """);
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

    public static void setHolder(Item item, UUID uuid, String playerName) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("""
                INSERT OR REPLACE INTO legendary_holders
                (item_id, player_uuid, player_name)
                VALUES (?, ?, ?)
             """)) {

            stmt.setString(
                    1,
                    BuiltInRegistries.ITEM.getKey(item).toString()
            );
            stmt.setString(
                    2,
                    uuid.toString()
            );
            stmt.setString(
                    3,
                    playerName
            );

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getHolder(Item item) {
        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement("""
                SELECT player_name
                FROM legendary_holders
                WHERE item_id = ?
             """)) {

            stmt.setString(
                    1,
                    BuiltInRegistries.ITEM.getKey(item).toString()
            );

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("player_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Nobody";
    }

    public static Map<String, String> getAllHolders() {

        Map<String, String> holders = new HashMap<>();

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            ResultSet rs = stmt.executeQuery("""
                SELECT item_id, player_name
                FROM legendary_holders
            """);

            while (rs.next()) {
                holders.put(
                        rs.getString("item_id"),
                        rs.getString("player_name")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return holders;
    }

    public static void updateLegendaryOwnership(ServerPlayer player) {

        for (ItemStack stack : player.getInventory()) {

            if (stack.isEmpty())
                continue;

            Item item = stack.getItem();

            if (
                       item == ModItems.VARATHA
                    || item == ModItems.KHALKEOUS
                    || item == ModItems.AEGIS
                    || item == ModItems.KYNTHIA
                    || item == ModItems.ENYALIOS
                    || item == ModItems.ASTRAPE
                    || item == ModItems.PEITHO
                    || item == ModItems.TRIAINA
                    || item == ModItems.CADUCEUS
                    || item == ModItems.PHOEBUS
                    || item == ModItems.THYRSUS
                    || item == ModItems.GLACIERA
                    || item == ModItems.ORCUS
                    || item == ModItems.URANICIDE
            ) {
                LegendaryDatabase.setHolder(
                        item,
                        player.getUUID(),
                        player.getGameProfile().name()
                );
            }
        }
    }
}