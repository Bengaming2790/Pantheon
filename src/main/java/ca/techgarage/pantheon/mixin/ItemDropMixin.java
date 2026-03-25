package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import ca.techgarage.pantheon.api.ItemDenyList;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO(Ravel): can not resolve target class ItemEntity
@Mixin(ItemEntity.class)
public class ItemDropMixin {
    // TODO(Ravel): no target class
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void onPickup(PlayerEntity player, CallbackInfo ci) {
        if (!PantheonConfig.dropBannedItems) return;
        ItemEntity self = (ItemEntity)(Object)this;
        if (ItemDenyList.isDenied(self.getStack().getItem())) {
            player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
            ci.cancel();
        }
    }
}
