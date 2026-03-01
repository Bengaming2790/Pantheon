package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class DisableTotemMixin {

    @Inject(
            method = "tryUseDeathProtector",
            at = @At("HEAD"),
            cancellable = true
    )
    private void disableTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;

        // If entity has a totem, pretend it doesn't work
        if (self.getMainHandStack().isOf(Items.TOTEM_OF_UNDYING) || self.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING) && PantheonConfig.disableTotemOfUndying) {
            cir.setReturnValue(false);
        }
    }
}