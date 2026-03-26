package ca.techgarage.pantheon.mixin;


import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO(Ravel): can not resolve target class SnowballEntity
@Mixin(Snowball.class)
public class SnowballHitMixin {

    // TODO(Ravel): no target class
    @Inject(method = "onHitEntity", at = @At("HEAD"))
    private void pantheon$partyFoul(EntityHitResult hit, CallbackInfo ci) {
        if (!(hit.getEntity() instanceof LivingEntity living)) return;

        living.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0));
        living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 1));
        living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
    }
}

