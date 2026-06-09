package ca.techgarage.pantheon.blocks.altar;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.Supplier;

public class AltarRecipe {

    public static final double DEFAULT_ITEM_HEIGHT  = 1.4;
    public static final double DEFAULT_TEXT_Y_START = 2.5;
    public static final double DEFAULT_TEXT_Y_STEP  = 0.3;
    public static final int    DEFAULT_ITEM_SIZE     = 16;

    private final Map<Item, Integer> ingredients;
    private final int experience;
    private final Supplier<ItemStack> outputSupplier;

    private double itemHeight = DEFAULT_ITEM_HEIGHT;
    private double textYStart = DEFAULT_TEXT_Y_START;
    private double textYStep  = DEFAULT_TEXT_Y_STEP;
    private int    itemSize   = DEFAULT_ITEM_SIZE;

    public AltarRecipe(Map<Item, Integer> ingredients, int experience, Supplier<ItemStack> output,
                       double itemHeight, double textYStart, double textYStep) {
        this.ingredients    = Map.copyOf(ingredients);
        this.experience     = experience;
        this.outputSupplier = output;
        this.itemHeight     = itemHeight;
        this.textYStart     = textYStart;
        this.textYStep      = textYStep;
    }

    // ── Builder-style setters ─────────────────────────────────────────

    public AltarRecipe itemHeight(double value) { this.itemHeight = value; return this; }
    public AltarRecipe textYStart(double value) { this.textYStart = value; return this; }
    public AltarRecipe textYStep(double value)  { this.textYStep  = value; return this; }
    public AltarRecipe itemSize(int value)       { this.itemSize   = value; return this; }

    // ── Getters ───────────────────────────────────────────────────────

    public Map<Item, Integer> getIngredients() { return ingredients; }
    public int       getExperience()  { return experience; }
    public ItemStack getOutput()      { return outputSupplier.get().copy(); }
    public double    getItemHeight()  { return itemHeight; }
    public double    getTextYStart()  { return textYStart; }
    public double    getTextYStep()   { return textYStep; }
    public int       getItemSize()    { return itemSize; }

    // ── Inventory helpers ─────────────────────────────────────────────

    public boolean playerHasIngredients(Player player) {
        for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
            int needed = entry.getValue();
            int found  = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(entry.getKey())) found += stack.getCount();
            }
            if (found < needed) return false;
        }
        return true;
    }

    public void consumeIngredients(Player player) {
        for (Map.Entry<Item, Integer> entry : ingredients.entrySet()) {
            int toConsume = entry.getValue();
            for (int i = 0; i < player.getInventory().getContainerSize() && toConsume > 0; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(entry.getKey())) {
                    int remove = Math.min(toConsume, stack.getCount());
                    stack.shrink(remove);
                    toConsume -= remove;
                }
            }
        }
    }
}