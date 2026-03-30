package ca.techgarage.pantheon.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CooldownMessages {

    private CooldownMessages() {}

    /** Sends a message when cooldown is finished */
    public static void cooldownFinishMessage(ServerPlayer player, String key, String text) {
        // true = action bar (center)
        player.sendSystemMessage(Component.literal(text), true);
    }

    /** Optional: sends a message if cooldown is active */
    public static void cooldownActiveMessage(ServerPlayer player, String key, String text) {
        player.sendSystemMessage(Component.literal(text), true);
    }
}