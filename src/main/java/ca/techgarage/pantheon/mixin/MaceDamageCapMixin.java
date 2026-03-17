package ca.techgarage.pantheon.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.item.MaceItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.damage.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public class MaceDamageCapMixin {

    @Inject(method = "getBonusAttackDamage", at = @At("RETURN"), cancellable = true)
    private void capMaceDamage(Entity target, float baseAttackDamage, DamageSource damageSource, CallbackInfoReturnable<Float> cir) {

        float damage = cir.getReturnValue();

        if (!(damageSource.getAttacker() instanceof PlayerEntity player)) return;

        if (player.getItemCooldownManager().isCoolingDown(player.getMainHandStack())) {
            cir.setReturnValue(-1f);
            return;
        }

        if (damage > 12f) {
            cir.setReturnValue(12f);
        }
    }
}