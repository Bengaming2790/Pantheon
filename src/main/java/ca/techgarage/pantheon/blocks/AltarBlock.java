package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.blocks.altar.AltarBlockEntity;
import ca.techgarage.pantheon.blocks.altar.AltarRecipe;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

public class AltarBlock extends BlockWithEntity implements PolymerBlock {

    private final AltarRecipe recipe;

    public AltarBlock(Settings settings, AltarRecipe recipe) {
        super(settings);
        this.recipe = recipe;
    }

    public net.minecraft.block.Block getPolymerBlock(BlockState state) {
        return Blocks.ENCHANTING_TABLE;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.ENCHANTING_TABLE.getDefaultState();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) return null;
        if (type != ModBlockEntities.ALTAR) return null;
        AltarRecipe capturedRecipe = this.recipe;
        return (BlockEntityTicker<T>) (BlockEntityTicker<AltarBlockEntity>)
                (w, p, s, be) -> be.spawnDisplayIfNeeded((net.minecraft.server.world.ServerWorld) w, capturedRecipe);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof AltarBlockEntity altar) {
                altar.removeDisplay();
            }
        }
        super.onBreak(world, pos, state, player);
        return state;
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos,
                                BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof AltarBlockEntity altar) {
                altar.removeDisplay();
            }
        }
        super.onStateReplaced(state, (ServerWorld) world, pos, moved);
    }
    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof AltarBlockEntity altar) {
            if (altar.tryCraft(player, this.recipe)) {

                world.playSound(
                        null,
                        pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundCategory.PLAYERS,
                        1.0F, // volume
                        0.75F  // pitch
                );
                return ActionResult.SUCCESS;
            } else {
                world.playSound(
                        null,
                        pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ENTITY_ITEM_BREAK,
                        SoundCategory.PLAYERS,
                        1.0F, // volume
                        0F  // pitch
                );
                return ActionResult.FAIL;
            }
        }

        return ActionResult.FAIL;
    }



    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return null;
    }
}