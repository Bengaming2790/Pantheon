package ca.techgarage.pantheon.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
public class SnowballHitMixin {

    @Inject(method = "onEntityHit", at = @At("HEAD"))
    private void pantheon$partyFoul(EntityHitResult hit, CallbackInfo ci) {
        if (!(hit.getEntity() instanceof LivingEntity living)) return;

        living.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
        living.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0));
    }
}

