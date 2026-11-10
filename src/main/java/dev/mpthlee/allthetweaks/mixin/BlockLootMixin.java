package dev.mpthlee.allthetweaks.mixin;

import dev.mpthlee.allthetweaks.Protection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BlockBehaviour.class)
public abstract class BlockLootMixin {
    @Inject(method = "getDrops", at = @At("HEAD"), cancellable = true)
    private void att$preventDuplicateDrops(BlockState state, LootParams.Builder loot,
                                           CallbackInfoReturnable<List<ItemStack>> cir) {
        if (!Protection.isCandidate(state)) return;
        var origin = loot.getOptionalParameter(LootContextParams.ORIGIN);
        if (origin != null && Protection.isProtected(loot.getLevel(), BlockPos.containing(origin), state)) {
            cir.setReturnValue(List.of());
        }
    }
}
