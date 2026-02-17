package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.altar.AltarBlockEntity;
import ca.techgarage.pantheon.altar.AltarRecipe;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class AltarBlock extends Block implements PolymerBlock, BlockEntityProvider {

    private final AltarRecipe recipe;

    public AltarBlock(Settings settings, AltarRecipe recipe) {
        super(settings);
        this.recipe = recipe;
    }

    public Block getPolymerBlock(BlockState state) {
        return Blocks.ENCHANTING_TABLE; // client-side appearance
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    public ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof AltarBlockEntity altar) {
            return altar.tryCraft(player, this.recipe)
                    ? ActionResult.SUCCESS
                    : ActionResult.FAIL;
        }


        return ActionResult.FAIL;
    }
    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.ENCHANTING_TABLE.getDefaultState();
    }

}
