package dev.mpthlee.allthetweaks.tweak.waystones;

import dev.mpthlee.allthetweaks.TweaksConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

import java.util.function.Supplier;

/** Optional integration boundary. No Waystones types are resolved on the disabled path. */
public final class WaystoneProtection {
    private static volatile boolean enabled;
    private static final ThreadLocal<Bypass> CREATIVE = new ThreadLocal<>();

    private WaystoneProtection() {}

    public static void refresh() {
        enabled = ModList.get().isLoaded("waystones")
                && TweaksConfig.SPEC.isLoaded() && TweaksConfig.WAYSTONE_PROTECTION.get();
    }

    public static boolean enabled() { return enabled; }

    public static boolean isCandidate(BlockState state) {
        return enabled && WaystoneRules.isCandidate(state);
    }

    public static boolean isProtected(BlockGetter world, BlockPos pos, BlockState state) {
        if (!enabled || !WaystoneRules.isProtected(world, pos, state)) return false;
        Bypass bypass = CREATIVE.get();
        return bypass == null || bypass.world != world
                || (!bypass.lower.equals(pos) && !bypass.upper.equals(pos));
    }

    public static boolean blocksReplacement(BlockGetter world, BlockPos pos, BlockState oldState, BlockState newState) {
        return isProtected(world, pos, oldState) && !WaystoneRules.sameStructure(oldState, newState);
    }

    /** Creative exemption is limited to this Waystone's two positions and always unwinds. */
    public static <T> T creativeBreak(Level world, BlockPos pos, BlockState state, Supplier<T> action) {
        BlockPos lower = WaystoneRules.lowerPosition(pos, state);
        Bypass previous = CREATIVE.get();
        CREATIVE.set(new Bypass(world, lower.immutable(), lower.above()));
        try {
            return action.get();
        } finally {
            if (previous == null) CREATIVE.remove();
            else CREATIVE.set(previous);
        }
    }

    private record Bypass(Level world, BlockPos lower, BlockPos upper) {}
}
