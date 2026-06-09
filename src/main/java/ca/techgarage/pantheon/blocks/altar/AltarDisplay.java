package ca.techgarage.pantheon.blocks.altar;

import ca.techgarage.pantheon.items.ModItems;
import com.mojang.math.Axis;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Display.ItemDisplay;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import org.joml.Quaternionf;
import com.mojang.math.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AltarDisplay {

    private static final float ROTATION_SPEED_DEG = 2.0f;

    private final List<net.minecraft.world.entity.Entity> entities = new ArrayList<>();
    private final ItemDisplay floatingItem;
    private final ItemDisplay modelDisplay; // was ItemEntity — now a proper Display

    private float currentYaw = 0f;

    public AltarDisplay(ServerLevel world, BlockPos pos, AltarRecipe recipe) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        //  Altar model
        modelDisplay = new ItemDisplay(EntityType.ITEM_DISPLAY, world);
        modelDisplay.setPos(cx, cy, cz);
        modelDisplay.setItemStack(new ItemStack(ModItems.ALTAR_MODEL));
        modelDisplay.setBillboardConstraints(Display.BillboardConstraints.FIXED);
        spawnEntity(world, modelDisplay);

        //  Floating output item
        floatingItem = new ItemDisplay(EntityType.ITEM_DISPLAY, world);
        floatingItem.setPos(cx, cy + recipe.getItemHeight(), cz);
        floatingItem.setItemStack(recipe.getOutput());
        floatingItem.setBillboardConstraints(Display.BillboardConstraints.FIXED);
        spawnEntity(world, floatingItem);

        //  Output name
        Component nameText = Component.literal("§6§l")
                .append(recipe.getOutput().getHoverName());
        spawnTextLine(world, cx, cy + recipe.getTextYStart(), cz, nameText);

        //  Ingredient list
        int i = 1;
        for (Map.Entry<Item, Integer> entry : recipe.getIngredients().entrySet()) {
            String ingredientName = new ItemStack(entry.getKey())
                    .getHoverName()
                    .getString();

            Component line = Component.literal(
                    "§7" + ingredientName + ": §f" + entry.getValue());

            spawnTextLine(world, cx,
                    cy + recipe.getTextYStart() - (i * recipe.getTextYStep()),
                    cz, line);
            i++;
        }
    }

    public void tick() {
        currentYaw = (currentYaw + ROTATION_SPEED_DEG) % 360f;

        Quaternionf rotation = Axis.YP.rotationDegrees(currentYaw);

        Transformation transform = new Transformation(
                new Vector3f(0, 0, 0),
                rotation,
                new Vector3f(1, 1, 1),
                null
        );

        floatingItem.setTransformation(transform);
        floatingItem.setTransformationInterpolationDelay(0);
        floatingItem.setTransformationInterpolationDuration(0);
    }

    public void destroy() {
        for (net.minecraft.world.entity.Entity entity : entities) {
            entity.discard();
        }
        entities.clear();
    }

    private void spawnTextLine(ServerLevel world, double x, double y, double z, Component text) {
        Display.TextDisplay textDisplay = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
        textDisplay.setPos(x, y, z);
        textDisplay.setText(text);
        textDisplay.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
        textDisplay.setBackgroundColor(0x55000000);
        spawnEntity(world, textDisplay);
    }

    private void spawnEntity(ServerLevel world, net.minecraft.world.entity.Entity entity) {
        world.addFreshEntity(entity);
        entities.add(entity);
    }
}