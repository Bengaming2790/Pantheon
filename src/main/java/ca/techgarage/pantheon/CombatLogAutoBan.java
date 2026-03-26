package ca.techgarage.pantheon;

import ca.techgarage.pantheon.database.BanManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLogAutoBan {

    private static final int COMBAT_TIME_SECONDS = 10;
    private static final long AUTO_BAN_TIME =
            (PantheonConfig.timeBannedFromCombatLogInMinutes * 60 * 1000L);

    // UUID -> combat end timestamp
    private static final Map<UUID, Long> combatTagged = new HashMap<>();

    public static void register() {

        // Remove expired combat tags
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            combatTagged.entrySet().removeIf(entry -> entry.getValue() < now);
        });

        // Player hits another player
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!world.isClientSide() && entity instanceof Player target) {

                long expireTime = System.currentTimeMillis() + (COMBAT_TIME_SECONDS * 1000L);

                combatTagged.put(player.getUUID(), expireTime);
                combatTagged.put(target.getUUID(), expireTime);
            }

            return InteractionResult.PASS;
        });

        // Player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {

            ServerPlayer player = handler.player;
            UUID uuid = player.getUUID();

            Long expireTime = combatTagged.get(uuid);

            if (expireTime != null && System.currentTimeMillis() < expireTime) {

                // Kill the player before banning
                DamageSource source = player.level()
                        .damageSources()
                        .generic();

                player.hurt(source, Float.MAX_VALUE);

                // Apply temp ban
                BanManager.ban(uuid, player.getName().getString(),
                        AUTO_BAN_TIME, "Combat Logging");

                System.out.println(player.getName().getString()
                        + " combat logged, was killed and temp banned.");
            }

            combatTagged.remove(uuid);
        });
    }
}