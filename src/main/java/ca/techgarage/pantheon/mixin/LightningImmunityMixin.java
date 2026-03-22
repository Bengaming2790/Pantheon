package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.items.weapons.Astrape;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LightningImmunityMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void pantheon$astrapeLightningImmunity(ServerWorld world,
                                                   DamageSource source,
                                                   float amount,
                                                   CallbackInfoReturnable<Boolean> cir) {


        LivingEntity self = (LivingEntity) (Object) this;
        if (source == self.getDamageSources().lightningBolt()) return;

        if (self.getMainHandStack().getItem() instanceof Astrape) {
            cir.setReturnValue(false);
            return;
        }

        if (self.getOffHandStack().getItem() instanceof Astrape) {
            cir.setReturnValue(false);
        }
    }
}