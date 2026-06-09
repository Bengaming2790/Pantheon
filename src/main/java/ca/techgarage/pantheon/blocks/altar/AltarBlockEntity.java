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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AltarBlockEntity extends BlockEntity {

    @Nullable
    private AltarDisplay display;
    private boolean hasBeenUsed = false;

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR, pos, state);
    }

    //  Display lifecycle
    public void spawnDisplayIfNeeded(ServerLevel world, @Nullable AltarRecipe recipe) {
        if (display != null || recipe == null) return;


        double heightExpand = recipe.getTextYStart() + 1.0;
        AABB searchBox = new AABB(getBlockPos()).inflate(1.0, heightExpand, 1.0);

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
    public boolean hasBeenUsed() {
        return hasBeenUsed;
    }

    public void setUsed() {
        this.hasBeenUsed = true;
        setChanged();
    }
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("hasBeenUsed", hasBeenUsed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hasBeenUsed = input.getBooleanOr("hasBeenUsed", false);
    }
    public boolean tryCraft(Player player, @Nullable AltarRecipe recipe) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;
        if (recipe == null) return false;

        if (!recipe.playerHasIngredients(player)) {
            serverPlayer.sendSystemMessage(
                    Component.literal("§cYou don't have the required ingredients!"), true);
            return false;
        }

        recipe.consumeIngredients(player);

        ItemStack output = recipe.getOutput();
        if (!player.getInventory().add(output)) {
            player.drop(output, false);
        }


        serverPlayer.giveExperienceLevels(recipe.getExperience());
        serverPlayer.sendSystemMessage(
                Component.literal("§aCrafting successful!"), true);
        return true;
    }
}