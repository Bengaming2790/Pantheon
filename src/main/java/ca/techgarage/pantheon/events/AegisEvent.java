package ca.techgarage.pantheon.events;

import ca.techgarage.pantheon.api.PeithoTick;
import ca.techgarage.pantheon.items.ModItems;
import ca.techgarage.pantheon.items.weapons.Aegis;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class AegisEvent {

    public static void register() {

        ServerTickEvents.END_SERVER_TICK.register(AegisEvent::tick);

        ServerLivingEntityEvents.AFTER_DAMAGE.register(
                (entity, source, baseDamage, damageTaken, blocked) -> {
                    if (!(entity instanceof PlayerEntity player)) return;

                    ItemStack main = player.getMainHandStack();
                    ItemStack off = player.getOffHandStack();

                    if (main.getItem() instanceof Aegis) {
                        triggerCooldown(player, main);
                    } else if (off.getItem() instanceof Aegis) {
                        triggerCooldown(player, off);
                    }
                }
        );
    }

    private static void tick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {

            boolean holdingAegis = false;

                if (player.getMainHandStack().isOf(ModItems.AEGIS) || player.getOffHandStack().isOf(ModItems.AEGIS)) {
                    holdingAegis = true;
                }

            if (holdingAegis) {
                player.setStatusEffect(
                        new StatusEffectInstance(StatusEffects.RESISTANCE, Integer.MAX_VALUE, 0, true, false, false),
                        player
                );
            }
        }
    }

    private static void triggerCooldown(PlayerEntity player, ItemStack stack) {
        if (!player.getItemCooldownManager().isCoolingDown(stack)) {
            if (!player.isCreative()) {
                player.getItemCooldownManager().set(stack, 20 * 60); // 1 minute
            }
        }
    }
}

