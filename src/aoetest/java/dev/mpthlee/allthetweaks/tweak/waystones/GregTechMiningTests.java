package dev.mpthlee.allthetweaks.tweak.waystones;

import com.mojang.authlib.GameProfile;
import dev.mpthlee.allthetweaks.AllTheTweaks;
import net.blay09.mods.waystones.api.WaystoneOrigin;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.UUID;

@GameTestHolder(AllTheTweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GregTechMiningTests {
    @GameTest(template = "empty")
    public static void hammerSkipsProtectedWaystoneAndFinishesAllStone(GameTestHelper helper) {
        checkHammer(helper, false);
    }

    @GameTest(template = "empty")
    public static void hammerSkipsUpperHalfAndFinishesAllStone(GameTestHelper helper) {
        checkHammer(helper, true);
    }

    private static void checkHammer(GameTestHelper helper, boolean upper) {
        BlockPos lower = WaystoneProtectionTests.place(helper, WaystoneOrigin.WILDERNESS);
        BlockPos target = upper ? lower.above() : lower;
        BlockPos center = target.west();
        var world = helper.getLevel();
        var original = world.getBlockEntity(lower);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
            if (!pos.equals(target)) world.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
        }
        var player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "hammer-test"));
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(center.getX() + 0.5, center.getY() + 2, center.getZ() + 0.5);
        player.setXRot(90);
        player.setYRot(0);
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("gtceu", "diamond_mining_hammer"));
        helper.assertTrue(item != null && item != net.minecraft.world.item.Items.AIR, "GregTech hammer missing");
        var stack = item.getDefaultInstance();
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        try {
            if (!item.onBlockStartBreak(stack, center, player)) player.gameMode.destroyBlock(center);
            helper.assertTrue(world.getBlockEntity(lower) == original, "Hammer removed the protected Waystone");
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, 0, -1), center.offset(1, 0, 1))) {
                if (!pos.equals(target)) helper.assertTrue(world.getBlockState(pos).isAir(),
                        "Hammer aborted before mining ordinary stone at " + pos);
            }
        } finally {
            player.discard();
        }
        helper.succeed();
    }
}
