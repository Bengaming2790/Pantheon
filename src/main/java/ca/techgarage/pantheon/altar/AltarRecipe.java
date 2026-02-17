package ca.techgarage.pantheon.altar;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class AltarRecipe {

    private final Map<Item, Integer> ingredients;
    private final int drachmaCost;
    private final ItemStack result;

    public AltarRecipe(
            Map<Item, Integer> ingredients,
            int drachmaCost,
            ItemStack result
    ) {
        this.ingredients = ingredients;
        this.drachmaCost = drachmaCost;
        this.result = result;
    }

    public Map<Item, Integer> getIngredients() {
        return ingredients;
    }

    public int getDrachmaCost() {
        return drachmaCost;
    }

    public ItemStack getResult() {
        return result.copy();
    }
}
