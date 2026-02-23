package ca.techgarage.pantheon.database;

import java.sql.*;
import java.time.Instant;
import java.util.UUID;

public class BanManager {

    public static void ban(UUID uuid, long durationMillis) {
        long now = Instant.now().toEpochMilli();
        long expires = now + durationMillis;

        try (PreparedStatement ps = BanDatabase.getConnection().prepareStatement("""
                INSERT OR REPLACE INTO temp_bans (player_uuid, banned_at, ban_expires_at)
                VALUES (?, ?, ?);
        """)) {
            ps.setString(1, uuid.toString());
            ps.setLong(2, now);
            ps.setLong(3, expires);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBanned(UUID uuid) {
        try (PreparedStatement ps = BanDatabase.getConnection().prepareStatement("""
                SELECT ban_expires_at FROM temp_bans WHERE player_uuid = ?;
        """)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                long expires = rs.getLong("ban_expires_at");
                return expires > Instant.now().toEpochMilli();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static long getRemainingTime(UUID uuid) {
        try (PreparedStatement ps = BanDatabase.getConnection().prepareStatement("""
                SELECT ban_expires_at FROM temp_bans WHERE player_uuid = ?;
        """)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong("ban_expires_at") - Instant.now().toEpochMilli();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void remove(UUID uuid) {
        try (PreparedStatement ps = BanDatabase.getConnection().prepareStatement("""
                DELETE FROM temp_bans WHERE player_uuid = ?;
        """)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
