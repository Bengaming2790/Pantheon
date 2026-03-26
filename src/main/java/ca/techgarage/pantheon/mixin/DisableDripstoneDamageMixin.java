package ca.techgarage.pantheon.mixin;


import ca.techgarage.pantheon.PantheonConfig;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DisableDripstoneDamageMixin {

    @Unique
    private void disableDripstoneDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (source.is(DamageTypes.STALAGMITE) && PantheonConfig.disableDripstoneDamage) {
            LivingEntity self = (LivingEntity)(Object)this;

            self.hurt(self.damageSources().fall(), amount);

            cir.setReturnValue(false);
        }
    }
}