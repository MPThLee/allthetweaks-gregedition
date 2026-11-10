package dev.mpthlee.allthetweaks.mixin;

import dev.mpthlee.allthetweaks.Protection;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(WaystoneBlockBase.class)
public abstract class WaystoneBlockMixin {
    @Inject(method = "playerWillDestroy", at = @At("HEAD"), cancellable = true)
    private void att$protectBeforeSideEffects(Level world, BlockPos pos, BlockState state, Player player,
                                              CallbackInfo ci) {
        if (world.isClientSide || !Protection.isProtected(world, pos, state)) return;
        if (player.isCreative()) {
            Protection.creativeBreak(world, pos, state, () -> {
                state.getBlock().playerWillDestroy(world, pos, state, player);
                return null;
            });
        }
        ci.cancel();
    }
}
