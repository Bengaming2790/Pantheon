package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseCooldown;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ThrowableCooldownMixin {

    @Redirect(
            method = "applyAfterUseComponentSideEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/component/UseCooldown;apply(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private void modifyThrowableCooldown(UseCooldown instance, ItemStack stack, LivingEntity user) {
        if (stack.is(Items.ENDER_PEARL)) {
            new UseCooldown((float) PantheonConfig.enderPearlCooldown, Optional.empty()).apply(stack, user);
        } else if (stack.is(Items.WIND_CHARGE)) {
            new UseCooldown((float) PantheonConfig.windChargeCooldown, Optional.empty()).apply(stack, user);
        } else {
            instance.apply(stack, user);
        }
    }
}