package dev.mpthlee.allthetweaks.mixin;

import dev.mpthlee.allthetweaks.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
            at = @At("HEAD"), cancellable = true)
    private void att$protectDestruction(BlockPos pos, boolean drop, Entity actor, int recursion,
                                       CallbackInfoReturnable<Boolean> cir) {
        Level world = (Level) (Object) this;
        if (!Protection.enabled() || world.isClientSide) return;
        var state = world.getBlockState(pos);
        if (!Protection.isProtected(world, pos, state)) return;
        if (actor instanceof Player player && player.isCreative()) {
            cir.setReturnValue(Protection.creativeBreak(world, pos, state,
                    () -> world.destroyBlock(pos, drop, actor, recursion)));
        } else {
            // Cancel before Level creates drops, particles, or invokes block callbacks.
            cir.setReturnValue(false);
        }
    }
}
