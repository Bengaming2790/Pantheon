package ca.techgarage.pantheon.commands;

import com.mojang.brigadier.CommandDispatcher;
import ca.techgarage.pantheon.database.BanDatabase;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.sql.*;
import java.util.UUID;

public class TempBanListCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("tempbanlist")
                        .executes(ctx -> {

                            long now = System.currentTimeMillis();
                            boolean foundAny = false;

                            try (PreparedStatement ps = BanDatabase.getConnection().prepareStatement(
                                    "SELECT * FROM temp_bans")) {

                                ResultSet rs = ps.executeQuery();

                                while (rs.next()) {

                                    String uuidString = rs.getString("player_uuid");
                                    long expires = rs.getLong("ban_expires_at");

                                    // Remove expired bans automatically
                                    if (expires <= now) {
                                        try (PreparedStatement delete = BanDatabase.getConnection().prepareStatement(
                                                "DELETE FROM temp_bans WHERE player_uuid = ?")) {
                                            delete.setString(1, uuidString);
                                            delete.executeUpdate();
                                        }
                                        continue;
                                    }

                                    foundAny = true;

                                    long remaining = expires - now;

                                    String timeFormatted = formatDuration(remaining);

                                    ctx.getSource().sendFeedback(
                                            () -> Text.literal(
                                                    uuidString + " - Unbans in: " + timeFormatted
                                            ),
                                            false
                                    );
                                }

                            } catch (SQLException e) {
                                e.printStackTrace();
                                ctx.getSource().sendError(Text.literal("Error reading ban database."));
                                return 0;
                            }

                            if (!foundAny) {
                                ctx.getSource().sendFeedback(
                                        () -> Text.literal("No active temporary bans."),
                                        false
                                );
                            }

                            return 1;
                        })
        );
    }

    private static String formatDuration(long millis) {

        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        return days + "d " +
                hours + "h " +
                minutes + "m " +
                seconds + "s";
    }
}
