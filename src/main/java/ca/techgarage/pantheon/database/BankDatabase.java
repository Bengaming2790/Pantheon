package ca.techgarage.pantheon.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.UUID;

public class BankDatabase {

    private static HikariDataSource dataSource;

    public static void init(Path path) {
        try {

            Files.createDirectories(path.getParent());

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:sqlite:" + path);
            config.setMaximumPoolSize(10);
            config.setPoolName("PantheonBankPool");

            dataSource = new HikariDataSource(config);

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS bank_accounts (
                        uuid TEXT PRIMARY KEY,
                        balance INTEGER NOT NULL
                    );
                """);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static boolean hasAccount(UUID uuid) {

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT uuid FROM bank_accounts WHERE uuid = ?")) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void createAccount(UUID uuid, int startingBalance) {

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO bank_accounts (uuid, balance) VALUES (?, ?)")) {

            ps.setString(1, uuid.toString());
            ps.setInt(2, startingBalance);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getBalance(UUID uuid) {

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT balance FROM bank_accounts WHERE uuid = ?")) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("balance");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void add(UUID uuid, int amount) {

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE bank_accounts SET balance = balance + ? WHERE uuid = ?")) {

            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void remove(UUID uuid, int amount) {

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE bank_accounts SET balance = MAX(balance - ?, 0) WHERE uuid = ?")) {

            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}