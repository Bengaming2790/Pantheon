package ca.techgarage.pantheon.commands;

import com.mojang.brigadier.CommandDispatcher;
import ca.techgarage.pantheon.database.BanDatabase;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.sql.*;

public class TempBanListCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("tempbanlist")
                        .requires(Permissions.require("pantheon.tempban"))
                        .executes(ctx -> {

                            long now = System.currentTimeMillis();
                            boolean foundAny = false;

                            try (PreparedStatement ps = BanDatabase.getConnection().prepareStatement(
                                    "SELECT * FROM temp_bans")) {

                                ResultSet rs = ps.executeQuery();

                                while (rs.next()) {

                                    String uuidString = rs.getString("player_uuid");
                                    String playerName = rs.getString("player_name");
                                    String reason = rs.getString("ban_reason");
                                    long expires = rs.getLong("ban_expires_at");

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

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    playerName + " - Reason: " + reason + " - Unbans in: " + timeFormatted
                                            ),
                                            false
                                    );
                                }

                            } catch (SQLException e) {
                                e.printStackTrace();
                                ctx.getSource().sendFailure(Component.literal("Error reading ban database."));
                                return 0;
                            }

                            if (!foundAny) {
                                ctx.getSource().sendSuccess(
                                        () -> Component.literal("No active temporary bans."),
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