package ca.techgarage.pantheon.blocks.altar;

import ca.techgarage.pantheon.blocks.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

public class AltarBlockEntity extends BlockEntity {

    private AltarDisplay display;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR, pos, state);
    }

    public void spawnDisplayIfNeeded(ServerWorld world, AltarRecipe recipe) {
        if (display != null) return;

        Box searchBox = new Box(pos).expand(1.0, recipe.getTextYStart() + 1.0, 1.0);
        List<DisplayEntity> stale = world.getEntitiesByClass(DisplayEntity.class, searchBox, Entity::isAlive);
        for (DisplayEntity e : stale) {
            e.discard();
        }

        display = new AltarDisplay(world, pos, recipe);
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

    public boolean tryCraft(PlayerEntity player, AltarRecipe recipe) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        if (!recipe.playerHasIngredients(player)) {
            player.sendMessage(Text.literal("§cYou don't have the required ingredients!"), true);
            return false;
        }

        recipe.consumeIngredients(player);

        ItemStack output = recipe.getOutput();
        if (!player.getInventory().insertStack(output)) {
            player.dropItem(output, false);
        }

        serverPlayer.addExperience(recipe.getExperience());
        player.sendMessage(Text.literal("§aCrafting successful!"), true);
        return true;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        removeDisplay();
    }
}