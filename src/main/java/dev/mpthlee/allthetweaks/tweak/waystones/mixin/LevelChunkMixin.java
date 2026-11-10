package dev.mpthlee.allthetweaks.tweak.waystones.mixin;

import dev.mpthlee.allthetweaks.tweak.waystones.WaystoneProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Shadow public abstract Level getLevel();
    @Shadow public abstract BlockState getBlockState(BlockPos pos);

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void att$protectReplacement(BlockPos pos, BlockState replacement, boolean moving,
                                        CallbackInfoReturnable<BlockState> cir) {
        if (!WaystoneProtection.enabled() || getLevel().isClientSide) return;
        if (WaystoneProtection.blocksReplacement(getLevel(), pos, getBlockState(pos), replacement)) {
            // Null is the native chunk API's "no change" result; Level.setBlock propagates false.
            cir.setReturnValue(null);
        }
    }
}
