package dev.mpthlee.allthetweaks;

import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.blay09.mods.waystones.block.ModBlocks;
import net.blay09.mods.waystones.block.WaystoneBlockBase;
import net.blay09.mods.waystones.block.entity.WaystoneBlockEntityBase;
import net.blay09.mods.waystones.config.WaystonesConfig;
import net.blay09.mods.waystones.core.WaystoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Regression contract. Protection cases must fail on the unpatched baseline. */
@GameTestHolder(AllTheTweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WaystoneProtectionTests {
    @GameTest(template = "forge:empty")
    public static void generatedLowerSurvivesDestroy(GameTestHelper helper) {
        checkProtected(helper, false, false);
    }

    @GameTest(template = "forge:empty")
    public static void generatedUpperSurvivesDestroy(GameTestHelper helper) {
        checkProtected(helper, true, false);
    }

    @GameTest(template = "forge:empty")
    public static void generatedLowerSurvivesReplacement(GameTestHelper helper) {
        checkProtected(helper, false, true);
    }

    @GameTest(template = "forge:empty")
    public static void generatedUpperSurvivesReplacement(GameTestHelper helper) {
        checkProtected(helper, true, true);
    }

    @GameTest(template = "forge:empty")
    public static void generatedRemainsBreakableWhenWaystonesProtectionDisabled(GameTestHelper helper) {
        var restrictions = WaystonesConfig.getActive().restrictions;
        boolean original = restrictions.generatedWaystonesUnbreakable;
        try {
            restrictions.generatedWaystonesUnbreakable = false;
            BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
            helper.assertTrue(helper.getLevel().destroyBlock(lower, false),
                    "Generated Waystones must remain breakable when their protection setting is off");
            helper.assertTrue(helper.getLevel().getBlockState(lower).isAir(),
                    "Generated Waystone was not removed with protection disabled");
        } finally {
            restrictions.generatedWaystonesUnbreakable = original;
        }
        helper.succeed();
    }

    @GameTest(template = "forge:empty")
    public static void playerPlacedRemainsBreakable(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.PLAYER);
        helper.assertTrue(helper.getLevel().destroyBlock(lower, false),
                "Player-placed Waystones must remain breakable");
        helper.assertTrue(helper.getLevel().getBlockState(lower).isAir(),
                "Player-placed Waystone was not removed");
        helper.succeed();
    }

    private static void checkProtected(GameTestHelper helper, boolean upper, boolean replace) {
        helper.assertTrue(WaystonesConfig.getActive().restrictions.generatedWaystonesUnbreakable,
                "Test setup requires generatedWaystonesUnbreakable=true");
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        var lowerState = helper.getLevel().getBlockState(lower);
        var upperState = helper.getLevel().getBlockState(lower.above());
        var lowerEntity = (WaystoneBlockEntityBase) helper.getLevel().getBlockEntity(lower);
        var upperEntity = helper.getLevel().getBlockEntity(lower.above());
        var destinationId = lowerEntity.getWaystone().getWaystoneUid();
        BlockPos target = upper ? lower.above() : lower;
        boolean changed = replace
                ? helper.getLevel().setBlock(target, Blocks.AIR.defaultBlockState(), 3)
                : helper.getLevel().destroyBlock(target, true);
        helper.assertTrue(!changed, "Protected removal must report failure");
        helper.assertTrue(helper.getLevel().getBlockState(lower).equals(lowerState),
                "Generated Waystone lower half was changed");
        helper.assertTrue(helper.getLevel().getBlockState(lower.above()).equals(upperState),
                "Generated Waystone upper half was changed");
        helper.assertTrue(helper.getLevel().getBlockEntity(lower) == lowerEntity
                        && helper.getLevel().getBlockEntity(lower.above()) == upperEntity,
                "Rejected destruction must preserve both block entities");
        helper.assertTrue(WaystoneManager.get(helper.getLevel().getServer())
                        .getWaystoneById(destinationId).isPresent(),
                "Rejected destruction must preserve the registered teleport destination");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(lower).expandTowards(0, 1, 0).inflate(1)).isEmpty(),
                "Rejected destruction must not produce item drops");
        helper.succeed();
    }

    private static BlockPos place(GameTestHelper helper, WaystoneOrigin origin) {
        BlockPos lower = helper.absolutePos(new BlockPos(1, 1, 1));
        var state = ModBlocks.waystone.defaultBlockState().setValue(WaystoneBlockBase.ORIGIN, origin);
        helper.getLevel().setBlock(lower, state.setValue(WaystoneBlockBase.HALF, DoubleBlockHalf.LOWER), 2);
        helper.getLevel().setBlock(lower.above(), state.setValue(WaystoneBlockBase.HALF, DoubleBlockHalf.UPPER), 2);
        var base = (WaystoneBlockEntityBase) helper.getLevel().getBlockEntity(lower);
        var top = (WaystoneBlockEntityBase) helper.getLevel().getBlockEntity(lower.above());
        helper.assertTrue(base != null && top != null, "Waystone fixture is incomplete");
        base.initializeWaystone(helper.getLevel(), null, origin);
        top.initializeFromBase(base);
        helper.assertTrue(base.getWaystone().isValid(), "Waystone fixture has no valid destination");
        helper.assertTrue(base.getWaystone().wasGenerated() == (origin != WaystoneOrigin.PLAYER),
                "Waystone fixture has incorrect generated status");
        helper.assertTrue(WaystoneManager.get(helper.getLevel().getServer())
                        .getWaystoneById(base.getWaystone().getWaystoneUid()).isPresent(),
                "Waystone fixture destination is not registered");
        return lower;
    }
}
