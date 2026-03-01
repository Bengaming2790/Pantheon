package ca.techgarage.pantheon.mixin;


import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DisableDripstoneDamageMixin {
//todo FIX THIS
    @Inject(
            method = "damage",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableDripstoneDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (source.isOf(DamageTypes.STALAGMITE) && PantheonConfig.disableDripstoneDamage) {
            LivingEntity self = (LivingEntity)(Object)this;

            self.damage(world, self.getDamageSources().fall(), amount);

            cir.setReturnValue(false);
        }
    }
}