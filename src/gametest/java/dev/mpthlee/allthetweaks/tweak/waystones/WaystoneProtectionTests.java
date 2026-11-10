package dev.mpthlee.allthetweaks.tweak.waystones;

import dev.mpthlee.allthetweaks.AllTheTweaks;
import dev.mpthlee.allthetweaks.TweaksConfig;

import dev.mpthlee.allthetweaks.tweak.waystones.WaystoneProtection;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.core.Direction;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Regression contract. Protection cases must fail on the unpatched baseline. */
@GameTestHolder(AllTheTweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WaystoneProtectionTests {
    @GameTest(template = "empty")
    public static void generatedWaystoneReportsUnbreakableHardness(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        for (BlockPos pos : new BlockPos[]{lower, lower.above()}) {
            helper.assertTrue(helper.getLevel().getBlockState(pos).getDestroySpeed(helper.getLevel(), pos) < 0,
                    "Protected Waystone must be excluded by miner hardness checks");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void playerWaystoneKeepsNormalHardness(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.PLAYER);
        helper.assertTrue(helper.getLevel().getBlockState(lower).getDestroySpeed(helper.getLevel(), lower) >= 0,
                "Player Waystone was marked unbreakable");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void generatedLowerSurvivesDestroy(GameTestHelper helper) {
        checkProtected(helper, false, false);
    }

    @GameTest(template = "empty")
    public static void generatedUpperSurvivesDestroy(GameTestHelper helper) {
        checkProtected(helper, true, false);
    }

    @GameTest(template = "empty")
    public static void generatedLowerSurvivesReplacement(GameTestHelper helper) {
        checkProtected(helper, false, true);
    }

    @GameTest(template = "empty")
    public static void generatedUpperSurvivesReplacement(GameTestHelper helper) {
        checkProtected(helper, true, true);
    }

    @GameTest(template = "empty")
    public static void directRemovalIsRejected(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.REMOVE);
    }

    @GameTest(template = "empty")
    public static void directChunkMutationIsRejected(GameTestHelper helper) {
        checkProtected(helper, true, Attempt.CHUNK);
    }

    @GameTest(template = "empty")
    public static void forgeBreakEventIsCanceled(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.FORGE_EVENT);
    }

    @GameTest(template = "empty")
    public static void earlyDestructionCallbackCannotDropOrRemoveHalves(GameTestHelper helper) {
        checkProtected(helper, true, Attempt.CALLBACK);
    }

    @GameTest(template = "empty")
    public static void forgePlayerDestructionIsRejected(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.PLAYER_API);
    }

    @GameTest(template = "empty")
    public static void lootBeforeRemovalCannotDuplicateWaystones(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.LOOT);
    }

    @GameTest(template = "empty")
    public static void villageMossyWaystoneIsProtected(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.DESTROY, WaystoneOrigin.VILLAGE, ModBlocks.mossyWaystone);
    }

    @GameTest(template = "empty")
    public static void dungeonSandyWaystoneIsProtected(GameTestHelper helper) {
        checkProtected(helper, true, Attempt.CALLBACK, WaystoneOrigin.DUNGEON, ModBlocks.sandyWaystone);
    }

    @GameTest(template = "empty")
    public static void blackstoneWaystoneIsProtected(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.CHUNK, WaystoneOrigin.WILDERNESS, ModBlocks.blackstoneWaystone);
    }

    @GameTest(template = "empty")
    public static void deepslateWaystoneIsProtected(GameTestHelper helper) {
        checkProtected(helper, true, Attempt.PLAYER_API, WaystoneOrigin.WILDERNESS, ModBlocks.deepslateWaystone);
    }

    @GameTest(template = "empty")
    public static void endStoneWaystoneIsProtected(GameTestHelper helper) {
        checkProtected(helper, false, Attempt.REPLACE, WaystoneOrigin.WILDERNESS, ModBlocks.endStoneWaystone);
    }

    @GameTest(template = "empty")
    public static void legacyGeneratedDestinationIsProtected(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        boolean original = TweaksConfig.WAYSTONE_PROTECTION.get();
        try {
            TweaksConfig.WAYSTONE_PROTECTION.set(false);
            WaystoneProtection.refresh();
            for (BlockPos pos : new BlockPos[]{lower, lower.above()}) {
                helper.getLevel().setBlock(pos, helper.getLevel().getBlockState(pos)
                        .setValue(WaystoneBlockBase.ORIGIN, WaystoneOrigin.UNKNOWN), 2);
            }
        } finally {
            TweaksConfig.WAYSTONE_PROTECTION.set(original);
            WaystoneProtection.refresh();
        }
        helper.assertTrue(!helper.getLevel().destroyBlock(lower, true), "Legacy generated Waystone was removed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void originCannotBeErasedToBypassProtection(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        var state = helper.getLevel().getBlockState(lower);
        helper.assertTrue(!helper.getLevel().setBlock(lower, state.setValue(WaystoneBlockBase.ORIGIN, WaystoneOrigin.PLAYER), 3),
                "Generated origin was erased");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeBypassIsScopedAndExceptionSafe(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        var state = helper.getLevel().getBlockState(lower);
        RuntimeException sentinel = new RuntimeException("test sentinel");
        try {
            WaystoneProtection.creativeBreak(helper.getLevel(), lower, state, () -> { throw sentinel; });
        } catch (RuntimeException expected) {
            if (expected != sentinel) throw expected;
        }
        helper.assertTrue(!helper.getLevel().destroyBlock(lower, false), "Creative bypass leaked after exception");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void ordinaryMutationTiming(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 1, 1));
        var chunk = helper.getLevel().getChunkAt(pos);
        var stone = Blocks.STONE.defaultBlockState();
        var dirt = Blocks.DIRT.defaultBlockState();
        chunk.setBlockState(pos, stone, false);
        boolean original = TweaksConfig.WAYSTONE_PROTECTION.get();
        long[] disabled = new long[5];
        long[] enabled = new long[5];
        try {
            for (int round = -2; round < 5; round++) {
                for (int phase = 0; phase < 2; phase++) {
                    boolean active = (round & 1) == phase;
                    TweaksConfig.WAYSTONE_PROTECTION.set(active);
                    WaystoneProtection.refresh();
                    long start = System.nanoTime();
                    for (int i = 0; i < 20000; i++) chunk.setBlockState(pos, (i & 1) == 0 ? dirt : stone, false);
                    long elapsed = System.nanoTime() - start;
                    helper.assertTrue(chunk.getBlockState(pos).is(Blocks.STONE), "Ordinary mutation changed behavior");
                    if (round >= 0) (active ? enabled : disabled)[round] = elapsed;
                }
            }
        } finally {
            TweaksConfig.WAYSTONE_PROTECTION.set(original);
            WaystoneProtection.refresh();
        }
        java.util.Arrays.sort(disabled);
        java.util.Arrays.sort(enabled);
        System.out.printf("ATT ordinary chunk mutation median: disabled=%.1f ns/op, enabled=%.1f ns/op%n",
                disabled[2] / 20000.0, enabled[2] / 20000.0);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aoeStillRemovesAdjacentOrdinaryBlocks(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        BlockPos adjacent = lower.east();
        helper.getLevel().setBlock(adjacent, Blocks.STONE.defaultBlockState(), 3);
        for (BlockPos target : BlockPos.betweenClosed(lower.offset(-1, 0, -1), lower.offset(1, 0, 1))) {
            helper.getLevel().destroyBlock(target, false);
        }
        helper.assertTrue(helper.getLevel().getBlockState(adjacent).isAir(), "AoE must still mine adjacent stone");
        helper.assertTrue(helper.getLevel().getBlockState(lower).is(ModBlocks.waystone), "AoE removed protected Waystone");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryWaystoneStateUpdatesStillWork(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        var rotated = helper.getLevel().getBlockState(lower).setValue(WaystoneBlockBase.FACING, Direction.EAST);
        helper.assertTrue(helper.getLevel().setBlock(lower, rotated, 2), "Rotation was incorrectly rejected");
        helper.assertTrue(helper.getLevel().getBlockState(lower).equals(rotated), "Rotation was not applied");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeMiningStillWorks(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.setGameMode(GameType.CREATIVE);
        try {
            helper.assertTrue(player.gameMode.destroyBlock(lower.above()), "Creative removal reported failure");
            helper.assertTrue(helper.getLevel().getBlockState(lower).isAir()
                    && helper.getLevel().getBlockState(lower.above()).isAir(), "Creative removal left a Waystone half");
        } finally {
            player.setGameMode(GameType.SURVIVAL);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tweakSwitchDisablesProtection(GameTestHelper helper) {
        boolean original = TweaksConfig.WAYSTONE_PROTECTION.get();
        try {
            TweaksConfig.WAYSTONE_PROTECTION.set(false);
            WaystoneProtection.refresh();
            BlockPos lower = place(helper, WaystoneOrigin.WILDERNESS);
            helper.assertTrue(helper.getLevel().destroyBlock(lower, false), "Tweak switch did not disable protection");
        } finally {
            TweaksConfig.WAYSTONE_PROTECTION.set(original);
            WaystoneProtection.refresh();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
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

    @GameTest(template = "empty")
    public static void playerPlacedRemainsBreakable(GameTestHelper helper) {
        BlockPos lower = place(helper, WaystoneOrigin.PLAYER);
        helper.assertTrue(helper.getLevel().destroyBlock(lower, false),
                "Player-placed Waystones must remain breakable");
        helper.assertTrue(helper.getLevel().getBlockState(lower).isAir(),
                "Player-placed Waystone was not removed");
        helper.succeed();
    }

    private static void checkProtected(GameTestHelper helper, boolean upper, boolean replace) {
        checkProtected(helper, upper, replace ? Attempt.REPLACE : Attempt.DESTROY);
    }

    private enum Attempt { DESTROY, REPLACE, REMOVE, CHUNK, FORGE_EVENT, CALLBACK, PLAYER_API, LOOT }

    private static void checkProtected(GameTestHelper helper, boolean upper, Attempt attempt) {
        checkProtected(helper, upper, attempt, WaystoneOrigin.WILDERNESS, ModBlocks.waystone);
    }

    private static void checkProtected(GameTestHelper helper, boolean upper, Attempt attempt, WaystoneOrigin origin, Block block) {
        helper.assertTrue(WaystonesConfig.getActive().restrictions.generatedWaystonesUnbreakable,
                "Test setup requires generatedWaystonesUnbreakable=true");
        BlockPos lower = place(helper, origin, block);
        var lowerState = helper.getLevel().getBlockState(lower);
        var upperState = helper.getLevel().getBlockState(lower.above());
        var lowerEntity = (WaystoneBlockEntityBase) helper.getLevel().getBlockEntity(lower);
        var upperEntity = helper.getLevel().getBlockEntity(lower.above());
        var destinationId = lowerEntity.getWaystone().getWaystoneUid();
        BlockPos target = upper ? lower.above() : lower;
        var world = helper.getLevel();
        var targetState = world.getBlockState(target);
        var player = FakePlayerFactory.getMinecraft(world);
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_PICKAXE));
        boolean changed = switch (attempt) {
            case DESTROY -> world.destroyBlock(target, true);
            case REPLACE -> world.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
            case REMOVE -> world.removeBlock(target, false);
            case CHUNK -> world.getChunkAt(target).setBlockState(target, Blocks.AIR.defaultBlockState(), false) != null;
            case FORGE_EVENT -> ForgeHooks.onBlockBreakEvent(world, GameType.SURVIVAL, player, target) != -1;
            case CALLBACK -> {
                targetState.getBlock().playerWillDestroy(world, target, targetState, player);
                yield world.removeBlock(target, false);
            }
            case PLAYER_API -> targetState.onDestroyedByPlayer(world, target, player, true, world.getFluidState(target));
            case LOOT -> !Block.getDrops(targetState, world, target, world.getBlockEntity(target), player,
                    player.getMainHandItem()).isEmpty();
        };
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

    static BlockPos place(GameTestHelper helper, WaystoneOrigin origin) {
        return place(helper, origin, ModBlocks.waystone);
    }

    private static BlockPos place(GameTestHelper helper, WaystoneOrigin origin, Block block) {
        BlockPos lower = helper.absolutePos(new BlockPos(1, 1, 1));
        var state = block.defaultBlockState().setValue(WaystoneBlockBase.ORIGIN, origin);
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
