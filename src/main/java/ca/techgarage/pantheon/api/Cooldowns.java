package ca.techgarage.pantheon.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Cooldowns {

    // Store cooldown end times
    private static final Map<UUID, Map<String, Long>> COOLDOWNS = new HashMap<>();

    // Track if the finish message was already sent
    private static final Map<UUID, Map<String, Boolean>> FINISHED_NOTIFIED = new HashMap<>();

    // Store the display name for messages
    private static final Map<UUID, Map<String, String>> DISPLAY_NAMES = new HashMap<>();
    private static final Map<UUID, Map<String, Integer>> INT_DATA = new HashMap<>();

    private Cooldowns() {}

    /** Checks if a cooldown is active */
    public static boolean isOnCooldown(Player player, String key) {
        long now = player.level().getGameTime();
        return COOLDOWNS
                .getOrDefault(player.getUUID(), Map.of())
                .getOrDefault(key, 0L) > now;
    }

    /** Get remaining ticks */
    public static long getRemaining(Player player, String key) {
        long now = player.level().getGameTime();
        return Math.max(
                0,
                COOLDOWNS
                        .getOrDefault(player.getUUID(), Map.of())
                        .getOrDefault(key, 0L) - now
        );
    }

    /** Start a cooldown and store display name */
    public static void start(Player player, String key, int ticks, String displayName) {
        COOLDOWNS
                .computeIfAbsent(player.getUUID(), _ -> new HashMap<>())
                .put(key, player.level().getGameTime() + ticks);

        FINISHED_NOTIFIED
                .computeIfAbsent(player.getUUID(), _ -> new HashMap<>())
                .put(key, false);

        DISPLAY_NAMES
                .computeIfAbsent(player.getUUID(), _ -> new HashMap<>())
                .put(key, displayName);
    }

    public static void start(Player player, String key, int ticks) {
        COOLDOWNS
                .computeIfAbsent(player.getUUID(), _ -> new HashMap<>())
                .put(key, player.level().getGameTime() + ticks);
    }
    /** Clear a cooldown */
    public static void clear(Player player, String key) {
        Map<String, Long> map = COOLDOWNS.get(player.getUUID());
        if (map != null) map.remove(key);
        Map<String, Boolean> notified = FINISHED_NOTIFIED.get(player.getUUID());
        if (notified != null) notified.remove(key);
        Map<String, String> names = DISPLAY_NAMES.get(player.getUUID());
        if (names != null) names.remove(key);
    }

    /** Clear all cooldowns */
    public static void clearAll(Player player) {
        if (player == null) return;
        COOLDOWNS.remove(player.getUUID());
        FINISHED_NOTIFIED.remove(player.getUUID());
        DISPLAY_NAMES.remove(player.getUUID());
    }

    /** Tick method to auto-send finished messages (call each server tick) */
    public static void tick(ServerPlayer player) {
        long now = player.level().getGameTime();

        Map<String, Long> playerCooldowns = COOLDOWNS.get(player.getUUID());
        if (playerCooldowns == null) return;

        Map<String, Boolean> notified = FINISHED_NOTIFIED.computeIfAbsent(player.getUUID(), _ -> new HashMap<>());
        Map<String, String> names = DISPLAY_NAMES.computeIfAbsent(player.getUUID(), _ -> new HashMap<>());

        for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
            String key = entry.getKey();
            long endTime = entry.getValue();

            if (endTime <= now && !notified.getOrDefault(key, false)) {
                String displayName = names.getOrDefault(key, key);
                CooldownMessages.cooldownFinishMessage(player, key, displayName + " is ready!");
                notified.put(key, true);
            }
        }
    }


    public static int getInt(Player player, String key) {
        return INT_DATA
                .getOrDefault(player.getUUID(), Map.of())
                .getOrDefault(key, 0);
    }

    public static void setInt(Player player, String key, int value) {
        INT_DATA
                .computeIfAbsent(player.getUUID(), _ -> new HashMap<>())
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