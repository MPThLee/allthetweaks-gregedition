package dev.mpthlee.allthetweaks.tweak.broom;

import dev.mpthlee.allthetweaks.AllTheTweaks;
import dev.mpthlee.allthetweaks.TweaksConfig;

import com.mojang.authlib.GameProfile;
import dev.mpthlee.allthetweaks.tweak.Tweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.cyclops.evilcraft.entity.item.EntityBroom;

import java.util.UUID;

@GameTestHolder(AllTheTweaks.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BroomFlightTests {
    @GameTest(template = "empty")
    public static void dimensionTeleportImmediatelyAfterMountRevokesFlight(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.teleport();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dimensionTeleportAfterBroomTickRevokesFlight(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.broom.tick();
            helper.assertTrue(fixture.broom.lastMounted == fixture.player, "Tick did not establish the rider reference");
            fixture.teleport();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dimensionCommandRevokesBroomFlight(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            int result = helper.getLevel().getServer().getCommands().performPrefixedCommand(
                    fixture.player.createCommandSourceStack().withPermission(4),
                    "execute in minecraft:the_nether run tp @s 0 100 0");
            helper.assertTrue(result > 0 && fixture.player.level() == fixture.destination(), "Cross-dimension command failed");
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void forgeDimensionTravelRevokesFlight(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.player.changeDimension(fixture.destination(), new net.minecraftforge.common.util.ITeleporter() {
                @Override
                public net.minecraft.world.entity.Entity placeEntity(net.minecraft.world.entity.Entity entity,
                        ServerLevel source, ServerLevel destination, float yaw,
                        java.util.function.Function<Boolean, net.minecraft.world.entity.Entity> reposition) {
                    return reposition.apply(false);
                }
            });
            helper.assertTrue(fixture.player.level() == fixture.destination(), "Forge dimension travel failed");
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void removingBroomRevokesItsFlight(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.broom.discard();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryDismountRevokesFlightBeforeFirstTick(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.player.stopRiding();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryDismountAfterTickSynchronizesAbilities(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.broom.tick();
            fixture.player.stopRiding();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void preexistingSurvivalFlightSurvivesDismount(GameTestHelper helper) {
        try (var fixture = new Ride(helper, true, GameType.SURVIVAL)) {
            fixture.broom.tick();
            fixture.teleport();
            helper.assertTrue(fixture.player.getAbilities().mayfly && fixture.player.getAbilities().flying,
                    "Cleanup revoked flight owned before the broom ride");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativeFlightSurvivesDismount(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.CREATIVE)) {
            fixture.broom.tick();
            fixture.teleport();
            helper.assertTrue(fixture.player.getAbilities().mayfly && fixture.player.getAbilities().flying,
                    "Cleanup revoked creative flight");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void switchingToSpectatorPreservesFlight(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.player.setGameMode(GameType.SPECTATOR);
            fixture.player.stopRiding();
            helper.assertTrue(fixture.player.getAbilities().mayfly && fixture.player.getAbilities().flying,
                    "Cleanup revoked spectator flight");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void formerCreativeFlightDoesNotLeakIntoSurvival(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.CREATIVE)) {
            fixture.player.setGameMode(GameType.SURVIVAL);
            fixture.teleport();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void repeatedFlightGrantsKeepOriginalSnapshot(GameTestHelper helper) {
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            fixture.broom.startAllowFlying(fixture.player);
            fixture.teleport();
            fixture.assertFlightRevoked();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disablingTweakDuringRideStillReleasesOwnedFlight(GameTestHelper helper) {
        boolean original = TweaksConfig.BROOM_FLIGHT_CLEANUP.get();
        try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
            TweaksConfig.BROOM_FLIGHT_CLEANUP.set(false);
            Tweaks.refresh();
            fixture.teleport();
            fixture.assertFlightRevoked();
        } finally {
            TweaksConfig.BROOM_FLIGHT_CLEANUP.set(original);
            Tweaks.refresh();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void disabledTweakRetainsUpstreamBehavior(GameTestHelper helper) {
        boolean original = TweaksConfig.BROOM_FLIGHT_CLEANUP.get();
        try {
            TweaksConfig.BROOM_FLIGHT_CLEANUP.set(false);
            Tweaks.refresh();
            try (var fixture = new Ride(helper, false, GameType.SURVIVAL)) {
                fixture.teleport();
                helper.assertTrue(fixture.player.getAbilities().mayfly, "Disabled tweak still changed broom behavior");
            }
        } finally {
            TweaksConfig.BROOM_FLIGHT_CLEANUP.set(original);
            Tweaks.refresh();
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void teleportWithoutBroomLeavesOtherFlightAlone(GameTestHelper helper) {
        var player = new RecordingPlayer(helper.getLevel());
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        try {
            player.teleportTo(helper.getLevel().getServer().getLevel(Level.NETHER), 0, 100, 0, 0, 0);
            helper.assertTrue(player.getAbilities().mayfly && player.getAbilities().flying,
                    "Non-broom flight was changed during teleport");
        } finally {
            player.discard();
        }
        helper.succeed();
    }

    private static final class RecordingPlayer extends FakePlayer {
        private int abilityUpdates;
        private boolean lastMayfly;
        private boolean lastFlying;

        RecordingPlayer(ServerLevel world) {
            super(world, new GameProfile(UUID.randomUUID(), "broom-test"));
        }

        @Override
        public void onUpdateAbilities() {
            abilityUpdates++;
            lastMayfly = getAbilities().mayfly;
            lastFlying = getAbilities().flying;
            super.onUpdateAbilities();
        }
    }

    private static final class Ride implements AutoCloseable {
        private final GameTestHelper helper;
        private final RecordingPlayer player;
        private final EntityBroom broom;

        Ride(GameTestHelper helper, boolean existingFlight, GameType mode) {
            this.helper = helper;
            var world = helper.getLevel();
            player = new RecordingPlayer(world);
            player.setGameMode(mode);
            if (existingFlight) player.getAbilities().mayfly = true;
            BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
            broom = new EntityBroom(world, pos.getX(), pos.getY(), pos.getZ());
            world.addFreshEntity(broom);
            helper.assertTrue(player.startRiding(broom, true), "Fixture failed to mount actual EvilCraft broom");
            helper.assertTrue(player.getAbilities().mayfly, "Broom did not grant flight in the fixture");
            player.getAbilities().flying = true;
            player.abilityUpdates = 0;
        }

        ServerLevel destination() { return helper.getLevel().getServer().getLevel(Level.NETHER); }

        void teleport() {
            player.teleportTo(destination(), 0, 100, 0, 0, 0);
            helper.assertTrue(player.level() == destination() && !player.isPassenger(),
                    "Teleport did not move and dismount player");
        }

        void assertFlightRevoked() {
            helper.assertTrue(!player.isPassenger() && !player.getAbilities().mayfly && !player.getAbilities().flying,
                    "Dismounted broom rider retained flight after dimension teleport");
            helper.assertTrue(player.abilityUpdates > 0 && !player.lastMayfly && !player.lastFlying,
                    "Flight cleanup was not synchronized through onUpdateAbilities");
        }

        @Override
        public void close() {
            player.stopRiding();
            player.discard();
            broom.discard();
        }
    }
}
