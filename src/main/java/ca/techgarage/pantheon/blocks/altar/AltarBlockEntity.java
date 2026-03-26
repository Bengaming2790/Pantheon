package ca.techgarage.pantheon.blocks.altar;

import ca.techgarage.pantheon.blocks.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class AltarBlockEntity extends BlockEntity {

    private AltarDisplay display;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR, pos, state);
    }

    public void spawnDisplayIfNeeded(ServerLevel world, AltarRecipe recipe) {
        if (display != null) return;

        double heightExpand = recipe.getTextYStart() + 1.0;

        AABB searchBox = new AABB(getBlockPos())
                .inflate(1.0, heightExpand, 1.0);

        // Get all display entities in the area and remove stale ones
        List<net.minecraft.world.entity.Display> stale = world.getEntitiesOfClass(
                net.minecraft.world.entity.Display.class,
                searchBox,
                net.minecraft.world.entity.Entity::isAlive
        );

        for (net.minecraft.world.entity.Display e : stale) {
            e.discard();
        }

        display = new AltarDisplay(world, getBlockPos(), recipe);
    }

    public void tickDisplay() {
        if (display != null) display.tick();
    }

    public void removeDisplay() {
        if (display != null) {
            display.destroy();
            display = null;
        }
    }

    public boolean tryCraft(Player player, AltarRecipe recipe) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        if (!recipe.playerHasIngredients(player)) {
            serverPlayer.sendSystemMessage(Component.literal("§cYou don't have the required ingredients!"), true);
            return false;
        }

        recipe.consumeIngredients(player);

        ItemStack output = recipe.getOutput();
        if (!player.getInventory().add(output)) {
            player.drop(output, false);
        }

        serverPlayer.experienceLevel += recipe.getExperience();
        serverPlayer.sendSystemMessage(  Component.literal("§aCrafting successful!"), true);
        return true;
    }


}