package ca.techgarage.pantheon.mixin;

import ca.techgarage.pantheon.PantheonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WebBlock; // MojMap name for CobwebBlock
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public abstract class NoCobwebSlowMixin {

    @Inject(
            method = "entityInside",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cancelCobwebSlow(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl, CallbackInfo ci) {
        if (PantheonConfig.noCobwebSlow) {
            ci.cancel();
        }
    }
}