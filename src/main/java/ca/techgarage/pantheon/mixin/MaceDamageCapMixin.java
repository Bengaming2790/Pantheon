package ca.techgarage.pantheon.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public class MaceDamageCapMixin {

    /**
     * In 1.21.1 MojMap:
     * - getBonusAttackDamage is the correct name.
     * - PlayerEntity is Player.
     * - getMainHandStack() is getMainHandItem().
     */
    @Inject(
            method = "getAttackDamageBonus", // Check if your mappings use 'getAttackDamageBonus' or 'getBonusAttackDamage'
            at = @At("RETURN"),
            cancellable = true
    )
    private void capMaceDamage(Entity target, float baseAttackDamage, DamageSource damageSource, CallbackInfoReturnable<Float> cir) {

        float damage = cir.getReturnValue();

        // Yarn: PlayerEntity -> MojMap: Player
        if (!(damageSource.getEntity() instanceof Player player)) return;

        // Yarn: getItemCooldownManager() -> MojMap: getCooldowns()
        // Yarn: getMainHandStack() -> MojMap: getMainHandItem()
        if (player.getCooldowns().isOnCooldown(player.getMainHandItem())) {
            cir.setReturnValue(-1f);
            return;
        }

        if (damage > 12f) {
            cir.setReturnValue(12f);
        }
    }
}