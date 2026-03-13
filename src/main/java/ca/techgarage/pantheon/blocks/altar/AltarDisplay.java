package ca.techgarage.pantheon.blocks.altar;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AltarDisplay {

    private static final float ROTATION_SPEED = 2.0f; // degrees per tick

    private final List<DisplayEntity> entities = new ArrayList<>();
    private final ItemDisplayEntity itemDisplay;

    public AltarDisplay(ServerWorld world, BlockPos pos, AltarRecipe recipe) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        // ── Floating output item ───────────────────────────────────────
        itemDisplay = new ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
        itemDisplay.setPosition(cx, cy + recipe.getItemHeight(), cz);
        itemDisplay.setItemStack(recipe.getOutput());
        itemDisplay.setBillboardMode(BillboardMode.FIXED);
        spawnEntity(world, itemDisplay);

        // ── Output name ────────────────────────────────────────────────
        Text nameText = Text.literal("§6§l").append(recipe.getOutput().getName());
        spawnTextLine(world, cx, cy + recipe.getTextYStart(), cz, nameText);

        // ── Ingredient lines ───────────────────────────────────────────
        int i = 1;
        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            String ingredientName = entry.getKey().getName().getString();
            Text line = Text.literal("§7" + ingredientName + ": §f" + entry.getValue());
            spawnTextLine(world, cx, cy + recipe.getTextYStart() - (i * recipe.getTextYStep()), cz, line);
            i++;
        }
    }

    /**
     * Call every tick from AltarBlockEntity to spin the item.
     */
    public void tick() {
        itemDisplay.rotate(ROTATION_SPEED, true, 0f, false);
    }

    private void spawnTextLine(ServerWorld world, double x, double y, double z, Text text) {
        TextDisplayEntity textDisplay = new TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
        textDisplay.setPosition(x, y, z);
        textDisplay.setText(text);
        textDisplay.setBillboardMode(BillboardMode.VERTICAL);
        textDisplay.setBackground(0x55000000);
        spawnEntity(world, textDisplay);
    }

    private void spawnEntity(ServerWorld world, DisplayEntity entity) {
        world.spawnEntity(entity);
        entities.add(entity);
    }

    public void destroy() {
        for (DisplayEntity entity : entities) {
            entity.discard();
        }
        entities.clear();
    }
}