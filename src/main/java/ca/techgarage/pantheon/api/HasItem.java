package ca.techgarage.pantheon.api;

import ca.techgarage.pantheon.items.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HasItem {

    public static boolean hasItem(ServerPlayer player, Item item) {
        boolean hasItem = false;
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(item)) {
                hasItem = true;
                break;
            }
        }

        return hasItem;
    }

}
