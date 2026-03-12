package ca.techgarage.pantheon.blocks.altar;

import ca.techgarage.pantheon.blocks.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class AltarBlockEntity extends BlockEntity {

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR, pos, state);
    }

    /**
     * Attempts to craft using the given recipe.
     * Checks that the player has all ingredients, then consumes them and gives the output.
     *
     * @return true if crafting succeeded, false otherwise
     */
    public boolean tryCraft(PlayerEntity player, AltarRecipe recipe) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        if (!recipe.playerHasIngredients(player)) {
            player.sendMessage(Text.literal("§cYou don't have the required ingredients!"), true);
            return false;
        }

        recipe.consumeIngredients(player);

        ItemStack output = recipe.getOutput();
        if (!player.getInventory().insertStack(output)) {
            // Inventory full — drop at player's feet
            player.dropItem(output, false);
        }

        // Give experience
        serverPlayer.addExperience(recipe.getExperience());

        player.sendMessage(Text.literal("§aCrafting successful!"), true);
        return true;
    }
}