package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseCooldown; // MojMap name for UseCooldownComponent
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ThrowableCooldownMixin {

    @Redirect(
            method = "consumeAndApplyCooldown", // MojMap name for applyRemainderAndCooldown
            at = @At(
                    value = "INVOKE",
                    // target matches the new UseCooldown record's apply method
                    target = "Lnet/minecraft/world/item/component/UseCooldown;apply(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void modifyPearlCooldown(UseCooldown instance, ItemStack stack, LivingEntity user) {

        // In 1.21.1 MojMap: isOf() -> is()
        if (stack.is(Items.ENDER_PEARL)) {
            // UseCooldown is a record in 1.21.1. Float is the duration in seconds.
            new UseCooldown((float) PantheonConfig.enderPearlCooldown, Optional.empty()).apply(stack, user);
        }
        else if (stack.is(Items.WIND_CHARGE)) {
            new UseCooldown((float) PantheonConfig.windChargeCooldown, Optional.empty()).apply(stack, user);
        }
        else {
            instance.apply(stack, user);
        }
    }
}