package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.items.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ShieldBlockMixin {
//todo: fix
//    @Inject(
//            // We use the full descriptor to ensure it finds the right 'hurt' method
//            method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void disableShieldAfterFirstBlock(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
//        Player player = (Player)(Object)this;
//
//        if (!player.isBlocking()) return;
//
//        ItemStack activeItem = player.getUseItem();
//
//        if (!(activeItem.is(Items.SHIELD) || activeItem.is(ModItems.AEGIS))) return;
//
//        if (player.getCooldowns().isOnCooldown(activeItem)) {
//            return;
//        }
//        player.getCooldowns().addCooldown(activeItem, 100);
//        player.stopUsingItem();
//        player.level().broadcastEntityEvent(player, (byte)30);
//    }
}