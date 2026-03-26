package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public class PeithoTick {

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(PeithoTick::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {

            boolean hasPeitho = false;
            for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                if (stack.is(ModItems.PEITHO)) {
                    hasPeitho = true;
                    break;
                }
            }

            if (!hasPeitho && player.getOffhandItem().is(ModItems.PEITHO)) {
                hasPeitho = true;
            }

            if (!hasPeitho) continue;

            // addEffect is the Mojang equivalent for setStatusEffect
            player.addEffect(
                    new MobEffectInstance(MobEffects.HEALTH_BOOST, 40, 1, false, false, false)
            );
        }
    }
}