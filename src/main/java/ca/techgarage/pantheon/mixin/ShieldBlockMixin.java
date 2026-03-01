package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.items.ModItems;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerEntity.class)
public abstract class ShieldBlockMixin {

    @Inject(
            method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
            at = @At("HEAD")
    )
    private void disableShieldAfterFirstBlock(ServerWorld world,
                                              DamageSource source,
                                              float amount,
                                              CallbackInfoReturnable<Boolean> cir) {

        PlayerEntity player = (PlayerEntity)(Object)this;

        if (!player.isBlocking()) return;

        ItemStack activeItem = player.getActiveItem();
        if (!activeItem.isOf(Items.SHIELD) || !activeItem.isOf(ModItems.AEGIS)) return;

        if (!player.getItemCooldownManager().isCoolingDown(activeItem)) {

            player.getItemCooldownManager().set(activeItem, 100);
            player.clearActiveItem();
        }
    }

}