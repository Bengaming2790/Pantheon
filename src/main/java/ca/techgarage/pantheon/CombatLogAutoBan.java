package ca.techgarage.pantheon;

import ca.techgarage.pantheon.database.BanManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import ca.techgarage.pantheon.api.Cooldowns;
import java.util.UUID;

public class CombatLogAutoBan {

    private static final String COMBAT_TAG = "combat_tag";
    private static final int COMBAT_TIME = 10 * 20; // 10 seconds
    private static final long AUTO_BAN_TIME =
            (60 * 60 * 1000L) + (30 * 60 * 1000L); // 1h 30m

    public static void register() {

        // When player attacks another player
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!world.isClient() && entity instanceof PlayerEntity target) {

                UUID attackerUUID = player.getUuid();
                UUID targetUUID = target.getUuid();

                // Apply combat cooldown to both
                Cooldowns.start(player, COMBAT_TAG, COMBAT_TIME);
                Cooldowns.start(target, COMBAT_TAG, COMBAT_TIME);
            }

            return ActionResult.PASS;
        });

        // When player leaves server
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {

            ServerPlayerEntity player = handler.player;
            UUID uuid = player.getUuid();

            if (Cooldowns.isOnCooldown(player, COMBAT_TAG)) {

                BanManager.ban(uuid, AUTO_BAN_TIME);

                System.out.println(player.getName().getString()
                        + " combat logged and was auto temp banned.");
            }
        });
    }
}
