package ca.techgarage.pantheon.database;

import java.sql.*;
import java.util.UUID;

public class BankDatabase {

    private static Connection connection;

    public static void init(String path) {
        try {

            Class.forName("org.sqlite.JDBC"); // <-- ADD THIS

            connection = DriverManager.getConnection("jdbc:sqlite:" + path);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS bank_accounts (
                        uuid TEXT PRIMARY KEY,
                        balance INTEGER NOT NULL
                    );
                """);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasAccount(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT uuid FROM bank_accounts WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException ignored) {
        }
        return false;
    }

    public static void createAccount(UUID uuid, int startingBalance) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO bank_accounts (uuid, balance) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setInt(2, startingBalance);
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    public static int getBalance(UUID uuid) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT balance FROM bank_accounts WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("balance");
            }
        } catch (SQLException ignored) {
        }
        return 0;
    }

    public static void add(UUID uuid, int amount) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_accounts SET balance = balance + ? WHERE uuid = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    public static void remove(UUID uuid, int amount) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE bank_accounts SET balance = MAX(balance - ?, 0) WHERE uuid = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {

        }
    }
}
