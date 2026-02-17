package ca.techgarage.pantheon.altar;

import ca.techgarage.pantheon.bank.BankDatabase;
import ca.techgarage.pantheon.blocks.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;

public class AltarBlockEntity extends BlockEntity {

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR, pos, state);
    }


    public boolean tryCraft(PlayerEntity player, AltarRecipe recipe) {
        UUID uuid = player.getUuid();

        if (BankDatabase.getBalance(uuid) < recipe.getDrachmaCost()) {
            return false;
        }

        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            if (countItem(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }

        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            removeItem(player, entry.getKey(), entry.getValue());
        }

        BankDatabase.remove(uuid, recipe.getDrachmaCost());

        player.getInventory().insertStack(recipe.getResult());

        return true;
    }

    private int countItem(PlayerEntity player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory()) {
            if (stack.isOf(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeItem(PlayerEntity player, Item item, int amount) {
        int remaining = amount;

        for (ItemStack stack : player.getInventory()) {
            if (!stack.isOf(item)) continue;

            int remove = Math.min(stack.getCount(), remaining);
            stack.decrement(remove);
            remaining -= remove;

            if (remaining <= 0) return;
        }
    }
}
