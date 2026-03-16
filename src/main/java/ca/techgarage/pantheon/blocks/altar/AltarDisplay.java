package ca.techgarage.pantheon.blocks.altar;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.entity.decoration.DisplayEntity.ItemDisplayEntity;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AltarDisplay {

    private static final float ROTATION_SPEED = 2.0f; // degrees per tick

    private final List<DisplayEntity> entities = new ArrayList<>();
    private final ItemDisplayEntity floatingItem;

    public AltarDisplay(ServerWorld world, BlockPos pos, AltarRecipe recipe) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        // ── Block model display ────────────────────────────────────────
        // Uses a PAPER item with custom_model_data to show the altar model.
        // The model JSON for pantheon:item/altar_model should use
        // "parent": "pantheon:block/altar" to reference your block model.
        ItemStack modelStack = new ItemStack(Items.PAPER);
        modelStack.set(DataComponentTypes.ITEM_MODEL,
                net.minecraft.util.Identifier.of("pantheon", "block/altar"));
        ItemDisplayEntity modelDisplay = new ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
        modelDisplay.setPosition(cx, cy, cz);
        modelDisplay.setItemStack(modelStack);
        modelDisplay.setBillboardMode(BillboardMode.FIXED);
        spawnEntity(world, modelDisplay);

        // ── Floating output item ───────────────────────────────────────
        floatingItem = new ItemDisplayEntity(EntityType.ITEM_DISPLAY, world);
        floatingItem.setPosition(cx, cy + recipe.getItemHeight(), cz);
        floatingItem.setItemStack(recipe.getOutput());
        floatingItem.setBillboardMode(BillboardMode.FIXED);
        spawnEntity(world, floatingItem);

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

    public void tick() {
        floatingItem.rotate(ROTATION_SPEED, true, 0f, false);
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