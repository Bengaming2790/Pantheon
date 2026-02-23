package ca.techgarage.pantheon.events;

import ca.techgarage.pantheon.database.BanManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.text.Text;

import java.util.UUID;

public class JoinListener {

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {

            UUID uuid = handler.player.getUuid();

            if (BanManager.isBanned(uuid)) {

                long remaining = BanManager.getRemainingTime(uuid);

                if (remaining > 0) {
                    handler.disconnect(Text.literal(
                            "You are temporarily banned.\n" +
                                    "Time remaining: " + formatDuration(remaining)
                    ));
                } else {
                    BanManager.remove(uuid);
                }
            }
        });
    }

    private static String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " days";
        if (hours > 0) return hours + " hours";
        if (minutes > 0) return minutes + " minutes";
        return seconds + " seconds";
    }
}