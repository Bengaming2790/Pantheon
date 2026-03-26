package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;

import java.util.Set;

public class ItemFrameBlocker {

    private static final Set<Item> BLOCKED_ITEMS = ModItems.getAllItems();

    public static void register() {

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {

            if (!(entity instanceof ItemFrame)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);

            if (BLOCKED_ITEMS.contains(stack.getItem())) {
                player.displayClientMessage(Component.translatable("item.anvil.rename"), true);
                return InteractionResult.FAIL;
            }

            return InteractionResult.PASS;
        });
    }
}