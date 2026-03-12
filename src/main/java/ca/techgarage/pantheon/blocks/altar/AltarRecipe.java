package ca.techgarage.pantheon.blocks.altar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class AltarRecipe {

    private final Map<Item, Integer> ingredients;
    private final int experience;
    private final ItemStack output;

    public AltarRecipe(Map<Item, Integer> ingredients, int experience, ItemStack output) {
        this.ingredients = ingredients;
        this.experience = experience;
        this.output = output;
    }

    public Map<Item, Integer> getIngredients() {
        return ingredients;
    }

    public int getExperience() {
        return experience;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    /**
     * Returns true if the player has ALL required ingredients in their inventory.
     */
    public boolean playerHasIngredients(net.minecraft.entity.player.PlayerEntity player) {
        for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
            int needed = entry.getValue();
            int found = 0;

            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.getItem() == entry.getKey()) {
                    found += stack.getCount();
                }
            }

            if (found < needed) return false;
        }
        return true;
    }

    /**
     * Consumes ALL required ingredients from the player's inventory.
     * Call only after playerHasIngredients() returns true.
     */
    public void consumeIngredients(net.minecraft.entity.player.PlayerEntity player) {
        for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
            int toConsume = entry.getValue();

            for (int i = 0; i < player.getInventory().size() && toConsume > 0; i++) {
                ItemStack stack = player.getInventory().getStack(i);
                if (stack.getItem() == entry.getKey()) {
                    int remove = Math.min(toConsume, stack.getCount());
                    stack.decrement(remove);
                    toConsume -= remove;
                }
            }
        }
    }
}