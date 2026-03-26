package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.DamageSources.ModDamageSources;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = "knockback", // MojMap name for takeKnockback
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelBleedKnockback(double strength, double x, double z, CallbackInfo ci) {

        LivingEntity entity = (LivingEntity)(Object)this;

        DamageSource source = entity.getLastDamageSource();

        if (source != null && (source.is(ModDamageSources.BLEEDING) || source.is(ModDamageSources.SUN_POISONING))) {
            ci.cancel();
        }
    }
}