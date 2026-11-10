package dev.mpthlee.allthetweaks;

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
    public static void worksWithoutWaystonesOrBalm(GameTestHelper helper) {
        helper.assertTrue(!ModList.get().isLoaded("waystones") && !ModList.get().isLoaded("balm"),
                "Optional-dependency smoke test accidentally loaded Waystones or Balm");
        helper.assertTrue(!Protection.enabled(), "Absent integration must stay disabled");
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        helper.assertTrue(helper.getLevel().destroyBlock(pos, true), "Ordinary block destruction was changed");
        helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), "Ordinary block was not removed");
        helper.succeed();
    }
}
