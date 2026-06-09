package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.items.weapons.Orcus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class BreakShadowstepOnHitMixin {
    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void onHurt(ServerLevel level, DamageSource source, float dmg, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (source.getEntity() instanceof ServerPlayer attacker)
            Orcus.deactivateShadowstep(attacker);

        if (self instanceof ServerPlayer victim)
            Orcus.deactivateShadowstep(victim);
    }
}
