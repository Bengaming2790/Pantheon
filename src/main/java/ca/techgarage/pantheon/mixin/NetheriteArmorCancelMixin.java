package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.Pantheon;
import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu; // MojMap name for SmithingScreenHandler
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class NetheriteArmorCancelMixin {


    @Inject(method = "createResult", at = @At("TAIL"))
    private void preventNetheriteUpgrade(CallbackInfo ci) {
        if (!PantheonConfig.diableNetheriteUpgrade) return;

        SmithingMenu self = (SmithingMenu)(Object)this;
        // getSlot(3) is the output slot in SmithingMenu
        ItemStack stack = self.getSlot(3).getItem();

        if (pantheon$isNetheriteGear(stack)) {
            self.getSlot(3).set(ItemStack.EMPTY);
        }
    }


    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    private void preventTaking(Player player, ItemStack stack, CallbackInfo ci) {
        if (!PantheonConfig.diableNetheriteUpgrade) return;

        if (pantheon$isNetheriteGear(stack)) {
            player.sendSystemMessage(Component.translatable("item.anvil.rename"));
            ci.cancel();
        }
    }

    @Unique
    private boolean pantheon$isNetheriteGear(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // In 1.21.1 MojMap: isOf() -> is()
        return stack.is(Items.NETHERITE_HELMET)
                || stack.is(Items.NETHERITE_CHESTPLATE)
                || stack.is(Items.NETHERITE_LEGGINGS)
                || stack.is(Items.NETHERITE_BOOTS)
                || stack.is(Items.NETHERITE_AXE)
                || stack.is(Items.NETHERITE_HOE)
                || stack.is(Items.NETHERITE_SWORD)
                || stack.is(Items.NETHERITE_PICKAXE)
                || stack.is(Items.NETHERITE_SHOVEL)
                || stack.is(Items.NETHERITE_HORSE_ARMOR);
    }
}