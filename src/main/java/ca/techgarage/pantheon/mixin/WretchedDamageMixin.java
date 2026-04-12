package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.Pantheon;
import ca.techgarage.pantheon.PantheonConfig;
import ca.techgarage.pantheon.status.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class WretchedDamageMixin {

    @ModifyArg(
            method = "actuallyHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"
            ),
            index = 0
    )
    private float pantheon$wretchedDamage(float healthValue) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (!entity.level().isClientSide() && entity.hasEffect(ModEffects.WRETCHED)) {
            float currentHealth = entity.getHealth();
            float damage = currentHealth - healthValue;

            float percent = (float) (1.0f + (PantheonConfig.wretchedPercent/100));

            damage *= percent;
            return currentHealth - damage;
        }

        return healthValue;
    }
}