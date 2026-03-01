package ca.techgarage.pantheon;

import ca.techgarage.pantheon.database.BanManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLogAutoBan {

    private static final int COMBAT_TIME_SECONDS = 10;
    private static final long AUTO_BAN_TIME = (PantheonConfig.timeBannedFromCombatLogInMinutes * 60 * 1000L);

    // UUID -> combat end timestamp (millis)
    private static final Map<UUID, Long> combatTagged = new HashMap<>();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long now = System.currentTimeMillis();
            combatTagged.entrySet().removeIf(entry -> entry.getValue() < now);
        });
        // When player attacks another player
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!world.isClient() && entity instanceof PlayerEntity target) {

                long expireTime = System.currentTimeMillis() + (COMBAT_TIME_SECONDS * 1000L);

                combatTagged.put(player.getUuid(), expireTime);
                combatTagged.put(target.getUuid(), expireTime);
            }

            return ActionResult.PASS;
        });

        // When player disconnects
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {

            ServerPlayerEntity player = handler.player;
            UUID uuid = player.getUuid();

            Long expireTime = combatTagged.get(uuid);

            if (expireTime != null && System.currentTimeMillis() < expireTime) {

                BanManager.ban(uuid, player.getName().toString(), AUTO_BAN_TIME);

                System.out.println(player.getName().getString()
                        + " combat logged and was auto temp banned.");
            }

            // Clean up
            combatTagged.remove(uuid);
        });
    }
}