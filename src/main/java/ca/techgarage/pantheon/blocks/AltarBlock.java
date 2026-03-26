package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.blocks.altar.AltarBlockEntity;
import ca.techgarage.pantheon.blocks.altar.AltarRecipe;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;

import net.minecraft.core.BlockPos;

import net.minecraft.server.level.ServerLevel;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.player.Player;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RenderShape;

import net.minecraft.world.phys.BlockHitResult;

import xyz.nucleoid.packettweaker.PacketContext;

public class AltarBlock extends BaseEntityBlock implements PolymerBlock {

    private final AltarRecipe recipe;

    public static final MapCodec<AltarBlock> CODEC =
            simpleCodec(settings -> new AltarBlock(settings, null));

    public AltarBlock(Properties settings, AltarRecipe recipe) {
        super(settings);
        this.recipe = recipe;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (level.isClientSide()) return null;
        if (type.equals(ModBlockEntities.ALTAR)) return null;

        AltarRecipe capturedRecipe = this.recipe;

        return (lvl, pos, st, be) -> {
            if (be instanceof AltarBlockEntity altar) {
                altar.spawnDisplayIfNeeded((ServerLevel) lvl, capturedRecipe);
                altar.tickDisplay();
            }
        };
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AltarBlockEntity altar) {
                altar.removeDisplay();
            }
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);

        if (be instanceof AltarBlockEntity altar) {

            if (altar.tryCraft(player, this.recipe)) {

                level.playSound(null, pos,
                        SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                        SoundSource.PLAYERS,
                        1.0F, 0.75F);

                return InteractionResult.SUCCESS;
            } else {

                level.playSound(null, pos,
                        SoundEvents.ITEM_BREAK.value(),
                        SoundSource.PLAYERS,
                        1.0F, 0.0F);

                return InteractionResult.FAIL;
            }
        }

        return InteractionResult.FAIL;
    }
}