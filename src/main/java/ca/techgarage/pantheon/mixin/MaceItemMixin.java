package ca.techgarage.pantheon.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public class MaceItemMixin {

    @Unique
    private static final int SLAM_COOLDOWN_TICKS = 160;


    @Inject(
            method = "hurtEnemy",
            at = @At("HEAD")
    )
    private void pantheon$applySlamCooldown(ItemStack itemStack, LivingEntity livingEntity, LivingEntity livingEntity2, CallbackInfo ci) {

        if (!(livingEntity instanceof Player player)) return;

        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return;
        }


        if (livingEntity.fallDistance > 1.5f) {
            player.getCooldowns().addCooldown(itemStack, SLAM_COOLDOWN_TICKS);
        }
    }
}