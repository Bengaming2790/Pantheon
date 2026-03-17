package ca.techgarage.pantheon.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public class BanManager {

    public static void ban(UUID uuid, String playerName, long durationMillis, String reason) {
        long now = Instant.now().toEpochMilli();
        long expires = now + durationMillis;

        if (reason == null || reason.isBlank()) {
            reason = "No reason specified";
        }

        try (Connection conn = BanDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                INSERT OR REPLACE INTO temp_bans
                (player_uuid, player_name, banned_at, ban_expires_at, ban_reason)
                VALUES (?, ?, ?, ?, ?);
             """)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, playerName);
            ps.setLong(3, now);
            ps.setLong(4, expires);
            ps.setString(5, reason);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBanned(UUID uuid) {
        long now = Instant.now().toEpochMilli();

        try (Connection conn = BanDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT ban_expires_at FROM temp_bans WHERE player_uuid = ?;
             """)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    long expires = rs.getLong("ban_expires_at");

                    if (expires <= now) {
                        remove(uuid);
                        return false;
                    }

                    return true;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static long getRemainingTime(UUID uuid) {
        long now = Instant.now().toEpochMilli();

        try (Connection conn = BanDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT ban_expires_at FROM temp_bans WHERE player_uuid = ?;
             """)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    long remaining = rs.getLong("ban_expires_at") - now;
                    return Math.max(remaining, 0);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static String getStoredName(UUID uuid) {

        try (Connection conn = BanDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT player_name FROM temp_bans WHERE player_uuid = ?;
             """)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("player_name");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Unknown";
    }

    public static String getReason(UUID uuid) {

        try (Connection conn = BanDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                SELECT ban_reason FROM temp_bans WHERE player_uuid = ?;
             """)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ban_reason");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "No reason specified";
    }

    public static void remove(UUID uuid) {

        try (Connection conn = BanDatabase.getConnection();
             PreparedStatement ps = conn.prepareStatement("""
                DELETE FROM temp_bans WHERE player_uuid = ?;
             """)) {

            ps.setString(1, uuid.toString());
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}