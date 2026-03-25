package ca.techgarage.pantheon.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// TODO(Ravel): can not resolve target class MaceItem
@Mixin(MaceItem.class)
public class MaceItemMixin {

    @Unique
    private static final int SLAM_COOLDOWN_TICKS = 160;

    // TODO(Ravel): no target class
    @Inject(method = "postHit", at = @At("HEAD"))
    private void pantheon$applySlamCooldown(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfo ci) {

        if (!(attacker instanceof PlayerEntity player)) return;

        // Check if player already has cooldown
        if (player.getItemCooldownManager().isCoolingDown(stack)) {
            return;
        }

        // Detect slam attack (falling)
        if (attacker.fallDistance > 1.5f) {

            player.getItemCooldownManager().set(stack, SLAM_COOLDOWN_TICKS);
        }
    }
}