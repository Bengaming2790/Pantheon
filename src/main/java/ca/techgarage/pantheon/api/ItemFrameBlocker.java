package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

import java.util.Set;

public class ItemFrameBlocker {

    private static final Set<Item> BLOCKED_ITEMS = ModItems.getAllItems();

    public static void register() {

        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {

            if (!(entity instanceof ItemFrameEntity)) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);

            if (BLOCKED_ITEMS.contains(stack.getItem())) {
                player.sendMessage(Text.translatable("item.anvil.rename").formatted(), true);
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
    }
}