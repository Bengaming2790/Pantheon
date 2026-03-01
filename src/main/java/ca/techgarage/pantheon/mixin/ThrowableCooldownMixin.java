package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.component.type.UseCooldownComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public abstract class ThrowableCooldownMixin {

    @Redirect(
            method = "applyRemainderAndCooldown",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/component/type/UseCooldownComponent;set(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V"
            )
    )
    private void modifyPearlCooldown(
            UseCooldownComponent component,
            ItemStack stack,
            LivingEntity user
    ) {

        if (stack.isOf(Items.ENDER_PEARL)) {
            // Replace vanilla cooldown with custom one
            new UseCooldownComponent((float) PantheonConfig.enderPearlCooldown).set(stack, user);
        } else if (stack.isOf(Items.WIND_CHARGE)) {
            new UseCooldownComponent((float) PantheonConfig.windChargeCooldown).set(stack, user);
        }
        else {
            component.set(stack, user);
        }
    }
}