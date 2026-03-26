package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;


import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DisableTotemMixin {

    @Inject(
            method = "checkTotemDeathProtection",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (self.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || self.getOffhandItem().is(Items.TOTEM_OF_UNDYING) && PantheonConfig.disableTotemOfUndying) {
            cir.setReturnValue(false);
        }
    }
}