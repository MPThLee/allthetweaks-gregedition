package dev.mpthlee.allthetweaks.compat;

import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class WaystoneRules {
    private WaystoneRules() {}

    public static boolean isCandidate(BlockState state) {
        return state.getBlock() instanceof WaystoneBlockBase;
    }

    public static boolean isProtected(BlockGetter world, BlockPos pos, BlockState state) {
        if (!isCandidate(state)
                || !WaystonesConfig.getActive().restrictions.generatedWaystonesUnbreakable) return false;
        WaystoneOrigin origin = state.getValue(WaystoneBlockBase.ORIGIN);
        if (origin == WaystoneOrigin.WILDERNESS || origin == WaystoneOrigin.VILLAGE
                || origin == WaystoneOrigin.DUNGEON) return true;
        // Older worlds can retain UNKNOWN in the blockstate while their saved destination is generated.
        return origin == WaystoneOrigin.UNKNOWN
                && world.getBlockEntity(pos) instanceof WaystoneBlockEntityBase entity
                && entity.getWaystone().wasGenerated();
    }

    public static boolean sameStructure(BlockState oldState, BlockState newState) {
        return oldState.is(newState.getBlock())
                && oldState.getValue(WaystoneBlockBase.ORIGIN) == newState.getValue(WaystoneBlockBase.ORIGIN)
                && (!oldState.hasProperty(WaystoneBlockBase.HALF)
                    || oldState.getValue(WaystoneBlockBase.HALF) == newState.getValue(WaystoneBlockBase.HALF));
    }

    public static BlockPos lowerPosition(BlockPos pos, BlockState state) {
        return state.hasProperty(WaystoneBlockBase.HALF)
                && state.getValue(WaystoneBlockBase.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }
}
