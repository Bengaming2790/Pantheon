package ca.techgarage.pantheon.api;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Cooldowns {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    private Cooldowns() {}

    public static boolean isOnCooldown(PlayerEntity player, String key) {
        long now = player.getEntityWorld().getTime();
        return COOLDOWNS
                .getOrDefault(player.getUuid(), Map.of())
                .getOrDefault(key, 0L) > now;
    }

    public static long getRemaining(PlayerEntity player, String key) {
        long now = player.getEntityWorld().getTime();
        return Math.max(
                0,
                COOLDOWNS
                        .getOrDefault(player.getUuid(), Map.of())
                        .getOrDefault(key, 0L) - now
        );
    }

    public static void start(PlayerEntity player, String key, int ticks) {
        COOLDOWNS
                .computeIfAbsent(player.getUuid(), u -> new HashMap<>())
                .put(key, player.getEntityWorld().getTime() + ticks);
    }

    public static void clear(PlayerEntity player, String key) {
        Map<String, Long> map = COOLDOWNS.get(player.getUuid());
        if (map != null) map.remove(key);
    }

    public static void clearAll(PlayerEntity player) {
        COOLDOWNS.remove(player.getUuid());
    }
}
