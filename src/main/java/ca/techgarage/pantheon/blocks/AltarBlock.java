package ca.techgarage.pantheon.blocks;

import ca.techgarage.pantheon.blocks.altar.AltarBlockEntity;
import ca.techgarage.pantheon.blocks.altar.AltarRecipe;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AltarBlock extends BaseEntityBlock implements PolymerBlock {

    public boolean hasBeenUsed = false;

    public static final MapCodec<AltarBlock> CODEC =
            simpleCodec(settings -> new AltarBlock(settings, null));

    private final @Nullable AltarRecipe recipe;

    public AltarBlock(Properties settings, @Nullable AltarRecipe recipe) {
        super(settings);
        this.recipe = recipe;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    //  Polymer

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {

        return Blocks.BARRIER.defaultBlockState();
    }

    //  Block entity

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {

        if (level.isClientSide() || !type.equals(ModBlockEntities.ALTAR)) return null;

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
        return super.playerWillDestroy(level, pos, state, player);
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        if (this.recipe == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AltarBlockEntity altar)) return InteractionResult.PASS;

        if (altar.hasBeenUsed()) {
            level.playSound(null, pos,
                    SoundEvents.ITEM_BREAK.value(),
                    SoundSource.PLAYERS,
                    1.0F, 0.0F);
            return InteractionResult.FAIL;
        }

        if (altar.tryCraft(player, this.recipe)) {
            altar.setUsed();
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
}