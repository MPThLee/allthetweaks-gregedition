package dev.mpthlee.allthetweaks;

import dev.mpthlee.allthetweaks.tweak.waystones.WaystoneProtection;
import dev.mpthlee.allthetweaks.tweak.broom.BroomFlightTweak;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(AllTheTweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class OptionalIntegrationTests {
    @GameTest(template = "empty")
    public static void worksWithoutOptionalMods(GameTestHelper helper) {
        helper.assertTrue(!ModList.get().isLoaded("waystones") && !ModList.get().isLoaded("balm")
                && !ModList.get().isLoaded("evilcraft") && !ModList.get().isLoaded("cyclopscore"),
                "Optional-dependency smoke test accidentally loaded an optional mod");
        helper.assertTrue(!WaystoneProtection.enabled() && !BroomFlightTweak.enabled(), "Absent integrations must stay disabled");
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(helper.getLevel().destroyBlock(pos, true), "Ordinary block destruction was changed");
        helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "Ordinary block was not removed");
        helper.succeed();
    }
}
