package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.items.weapons.Peitho;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;

public class PeithoTick {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PeithoTick::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            boolean hasPeitho = false;
            for (var stack : player.getInventory()) {
                if (stack.isOf(ModItems.PEITHO)) {
                    hasPeitho = true;
                    break;
                }
            }

            if (!hasPeitho) continue;

            player.setStatusEffect(
                    new StatusEffectInstance(StatusEffects.HEALTH_BOOST, 40, 1, false, false, false),
                    player
            );
        }
    }
}
