package ca.techgarage.pantheon.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CooldownMessages {

    private CooldownMessages() {}

    public static void cooldownFinishMessage(ServerPlayer player, String key, String text) {
        // true = action bar (center)
        player.sendSystemMessage(Component.literal(text), true);
    }

    public static void cooldownActiveMessage(ServerPlayer player, String key, String text) {
        player.sendSystemMessage(Component.literal(text), true);
    }
}