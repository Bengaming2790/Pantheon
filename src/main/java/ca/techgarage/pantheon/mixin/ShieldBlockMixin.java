package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.items.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ShieldBlockMixin {

    @Inject(
            method = "hurtServer",
            at = @At("HEAD")
    )
    private void disableShield(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;

        if (!(entity instanceof Player player)) return;

        if (!player.isBlocking()) return;

        ItemStack shield = player.getUseItem();

        if (!(shield.is(Items.SHIELD) || shield.is(ModItems.AEGIS))) return;

        if (player.getCooldowns().isOnCooldown(shield)) return;

        player.getCooldowns().addCooldown(shield, 100);

        player.stopUsingItem();

        player.level().broadcastEntityEvent(player, (byte)30);
    }
}