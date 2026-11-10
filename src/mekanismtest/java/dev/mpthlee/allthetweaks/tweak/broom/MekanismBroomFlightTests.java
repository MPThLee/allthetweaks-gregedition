package dev.mpthlee.allthetweaks.tweak.broom;

import com.mojang.authlib.GameProfile;
import dev.mpthlee.allthetweaks.AllTheTweaks;
import mekanism.common.Mekanism;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.cyclops.evilcraft.entity.item.EntityBroom;

import java.util.UUID;

/** Optional integration profile: exercises Mekanism's real flight cache without forcing flying=true. */
@GameTestHolder(AllTheTweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class MekanismBroomFlightTests {
    @GameTest(template = "empty")
    public static void dimensionTeleportDoesNotRestoreCachedBroomFlight(GameTestHelper helper) {
        verify(helper, false, true);
    }

    @GameTest(template = "empty")
    public static void dismountUpdatesCachedFlightBeforeLaterTeleport(GameTestHelper helper) {
        verify(helper, false, false);
    }

    @GameTest(template = "empty")
    public static void preexistingFlightSurvivesCacheRefresh(GameTestHelper helper) {
        verify(helper, true, true);
    }

    private static void verify(GameTestHelper helper, boolean existingFlight, boolean mountedTeleport) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "broom-mekanism"));
        player.setGameMode(GameType.SURVIVAL);
        player.getAbilities().mayfly = existingFlight;
        var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
        var broom = new EntityBroom(helper.getLevel(), pos.getX(), pos.getY(), pos.getZ());
        helper.getLevel().addFreshEntity(broom);
        try {
            helper.assertTrue(player.startRiding(broom, true), "Failed to mount real broom");
            broom.tick();
            helper.assertTrue(player.getAbilities().mayfly, "Broom did not grant temporary flight");
            // Same public cache update used by Mekanism's player tick handler.
            Mekanism.playerState.updateFlightInfo(player);
            if (!mountedTeleport) player.stopRiding();
            player.teleportTo(helper.getLevel().getServer().getLevel(Level.NETHER), 0, 140, 0, 0, 0);
            // Exercise the real cache consumer too, independent of FakePlayer event filtering.
            Mekanism.playerState.reapplyServerSideOnly(player);
            helper.assertTrue(!player.isPassenger(), "Teleport did not dismount the broom");
            helper.assertTrue(player.getAbilities().mayfly == existingFlight,
                    "Mekanism restored stale broom flight or removed preexisting flight");
            helper.assertTrue(!player.getAbilities().flying, "Test unexpectedly acquired active flight");
        } finally {
            player.stopRiding();
            Mekanism.playerState.clearPlayerServerSideOnly(player.getUUID());
            player.discard();
            broom.discard();
        }
        helper.succeed();
    }
}
