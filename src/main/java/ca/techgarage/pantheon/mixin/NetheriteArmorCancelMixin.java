package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.SmithingScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingScreenHandler.class)
public abstract class NetheriteArmorCancelMixin {

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void preventNetheriteArmor(CallbackInfo ci) {

        if (!PantheonConfig.diableNetheriteUpgrade) return;

        ItemStack stack = ((SmithingScreenHandler)(Object)this)
                .getSlot(3)
                .getStack();

        if (isNetheriteGear(stack)) {
            ((SmithingScreenHandler)(Object)this)
                    .getSlot(3)
                    .setStack(ItemStack.EMPTY);

        }
    }
    @Inject(method = "onTakeOutput", at = @At("HEAD"), cancellable = true)
    private void preventTaking(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!PantheonConfig.diableNetheriteUpgrade) return;

        if (isNetheriteGear(stack)) {
            ci.cancel();
            player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
        }
    }
    @Unique
    private boolean isNetheriteGear(ItemStack stack) {
        if (stack.isEmpty()) return false;

        return stack.isOf(Items.NETHERITE_HELMET)
                || stack.isOf(Items.NETHERITE_CHESTPLATE)
                || stack.isOf(Items.NETHERITE_LEGGINGS)
                || stack.isOf(Items.NETHERITE_BOOTS)
                || stack.isOf(Items.NETHERITE_AXE)
                || stack.isOf(Items.NETHERITE_HOE)
                || stack.isOf(Items.NETHERITE_SWORD)
                || stack.isOf(Items.NETHERITE_PICKAXE)
                || stack.isOf(Items.NETHERITE_SHOVEL)
                || stack.isOf(Items.NETHERITE_HORSE_ARMOR);
    }
}