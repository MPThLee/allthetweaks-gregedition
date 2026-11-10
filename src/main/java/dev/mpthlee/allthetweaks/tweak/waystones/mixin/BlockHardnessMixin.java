package dev.mpthlee.allthetweaks.tweak.waystones.mixin;

import dev.mpthlee.allthetweaks.tweak.waystones.WaystoneProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockHardnessMixin {
    @Inject(method = "getDestroySpeed", at = @At("HEAD"), cancellable = true)
    private void att$skipProtectedWaystones(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (WaystoneProtection.isProtected(world, pos, (BlockState) (Object) this)) {
            // Mining planners should skip this target, rather than abort after a rejected removal.
            cir.setReturnValue(-1f);
        }
    }
}
