package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.DamageSources.ModDamageSources;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    private void cancelBleedKnockback(double strength, double x, double z, CallbackInfo ci) {

        LivingEntity entity = (LivingEntity)(Object)this;

        DamageSource source = entity.getRecentDamageSource();

        if (source != null && (source.isOf(ModDamageSources.BLEEDING) || source.isOf(ModDamageSources.SUN_POISONING))) {
            ci.cancel();
        }
    }
}