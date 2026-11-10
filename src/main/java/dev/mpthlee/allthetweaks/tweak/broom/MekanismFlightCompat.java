package dev.mpthlee.allthetweaks.tweak.broom;

import mekanism.common.Mekanism;
import net.minecraft.server.level.ServerPlayer;

/** Loaded only when Mekanism is present; no extra player tick handler or global scan. */
final class MekanismFlightCompat {
    private MekanismFlightCompat() {}

    static void refresh(ServerPlayer player) {
        // Mekanism reapplies its last player-tick snapshot on dimension change. Refresh it
        // after releasing the broom's grant so it cannot restore that stale permission.
        // Its normal API also evaluates legitimate gravitational-modulator/creative flight.
        Mekanism.playerState.updateFlightInfo(player);
    }
}
