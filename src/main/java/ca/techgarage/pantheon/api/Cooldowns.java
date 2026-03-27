package ca.techgarage.pantheon.api;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Cooldowns {

    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    private Cooldowns() {}
    private static final Map<UUID, Map<String, Integer>> INT_DATA = new HashMap<>();

    public static boolean isOnCooldown(Player player, String key) {
        long now = player.level().getGameTime();
        return COOLDOWNS
                .getOrDefault(player.getUUID(), Map.of())
                .getOrDefault(key, 0L) > now;
    }

    public static long getRemaining(Player player, String key) {
        long now = player.level().getGameTime();
        return Math.max(
                0,
                COOLDOWNS
                        .getOrDefault(player.getUUID(), Map.of())
                        .getOrDefault(key, 0L) - now
        );
    }

    public static void start(Player player, String key, int ticks) {
        COOLDOWNS
                .computeIfAbsent(player.getUUID(), u -> new HashMap<>())
                .put(key, player.level().getGameTime() + ticks);
    }

    public static void clear(Player player, String key) {
        Map<String, Long> map = COOLDOWNS.get(player.getUUID());
        if (map != null) map.remove(key);
    }

    public static void clearAll(Player player) {
        if (player == null) return;
        COOLDOWNS.remove(player.getUUID());
    }

    public static int getInt(Player player, String key) {
        return INT_DATA
                .getOrDefault(player.getUUID(), Map.of())
                .getOrDefault(key, 0);
    }

    public static void setInt(Player player, String key, int value) {
        INT_DATA
                .computeIfAbsent(player.getUUID(), u -> new HashMap<>())
                .put(key, value);
    }

    public static void incrementInt(Player player, String key) {
        setInt(player, key, getInt(player, key) + 1);
    }

    public static void clearInt(Player player, String key) {
        Map<String, Integer> map = INT_DATA.get(player.getUUID());
        if (map != null) map.remove(key);
    }

}
