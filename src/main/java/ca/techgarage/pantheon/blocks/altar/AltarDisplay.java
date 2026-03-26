package ca.techgarage.pantheon.blocks.altar;


import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Display.ItemDisplay;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.component.CustomModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AltarDisplay {

    private static final float ROTATION_SPEED = 2.0f;

    private final List<Display> entities = new ArrayList<>();
    private final ItemDisplay floatingItem;

    public AltarDisplay(ServerLevel world, BlockPos pos, AltarRecipe recipe) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY();
        double cz = pos.getZ() + 0.5;

        // ── Block model display (via custom_model_data) ──
        ItemStack modelStack = new ItemStack(Items.PAPER);
        modelStack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(),List.of(),List.of(),List.of(1))); // resource pack handles model

        ItemDisplay modelDisplay = new ItemDisplay(EntityType.ITEM_DISPLAY, world);
        modelDisplay.setPos(cx, cy, cz);
        modelDisplay.setItemStack(modelStack);
        modelDisplay.setBillboardConstraints(Display.BillboardConstraints.FIXED);
        spawnEntity(world, modelDisplay);

        // ── Floating output item ──
        floatingItem = new ItemDisplay(EntityType.ITEM_DISPLAY, world);
        floatingItem.setPos(cx, cy + recipe.getItemHeight(), cz);
        floatingItem.setItemStack(recipe.getOutput());
        floatingItem.setBillboardConstraints(Display.BillboardConstraints.FIXED);
        spawnEntity(world, floatingItem);

        // ── Output name ──
        Component nameText = Component.literal("§6§l")
                .append(recipe.getOutput().getHoverName());

        spawnTextLine(world, cx, cy + recipe.getTextYStart(), cz, nameText);

        // ── Ingredients ──
        int i = 1;
        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            String ingredientName = new ItemStack(entry.getKey())
                    .getHoverName()
                    .getString();

            Component line = Component.literal("§7" + ingredientName + ": §f" + entry.getValue());

            spawnTextLine(
                    world,
                    cx,
                    cy + recipe.getTextYStart() - (i * recipe.getTextYStep()),
                    cz,
                    line
            );
            i++;
        }
    }

    public void tick() {
        // Simple rotation
        floatingItem.setYRot(floatingItem.getYRot() + ROTATION_SPEED);
    }

    private void spawnTextLine(ServerLevel world, double x, double y, double z, Component text) {
        Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
        textDisplay.setPos(x, y, z);
        textDisplay.setText(text);
        textDisplay.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
        textDisplay.setBackgroundColor(0x55000000);
        spawnEntity(world, textDisplay);
    }

    private void spawnEntity(ServerLevel world, Display entity) {
        world.addFreshEntity(entity);
        entities.add(entity);
    }

    public void destroy() {
        for (Display entity : entities) {
            entity.discard();
        }
        entities.clear();
    }
}