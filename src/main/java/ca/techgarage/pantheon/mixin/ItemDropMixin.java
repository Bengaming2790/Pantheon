package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import ca.techgarage.pantheon.api.ItemDenyList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemDropMixin {

    @Inject(
            method = "playerTouch",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onPickup(Player player, CallbackInfo ci) {
        if (!PantheonConfig.dropBannedItems) return;

        ItemEntity self = (ItemEntity)(Object)this;

        if (ItemDenyList.isDenied(self.getItem().getItem())) {
            player.displayClientMessage(Component.translatable("item.anvil.rename"), true);
            ci.cancel();
        }
    }
}