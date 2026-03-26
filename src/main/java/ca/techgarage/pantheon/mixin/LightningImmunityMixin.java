package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.items.weapons.Astrape;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LightningImmunityMixin {

    @Inject(
            method = "hurt",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pantheon$astrapeLightningImmunity(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Using your class's damageSources() method to check for lightning
        if (source.is((ResourceKey<DamageType>) self.damageSources().lightningBolt().typeHolder())) {
            // Check hand items - using MojMap names found in your source imports
            if (self.getMainHandItem().getItem() instanceof Astrape ||
                    self.getOffhandItem().getItem() instanceof Astrape) {
                cir.setReturnValue(false);
            }
        }
    }
}