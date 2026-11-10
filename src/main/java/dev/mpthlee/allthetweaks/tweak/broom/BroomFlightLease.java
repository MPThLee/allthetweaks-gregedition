package dev.mpthlee.allthetweaks.tweak.broom;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-broom ownership of temporary flight, with no global player scan or tick handler. */
public final class BroomFlightLease {
    public interface Owner {
        boolean hasFlightLease(ServerPlayer player);
    }

    private final Map<UUID, Boolean> priorFlight = new HashMap<>();

    public boolean contains(ServerPlayer player) {
        return priorFlight.containsKey(player.getUUID());
    }

    public void acquire(ServerPlayer player) {
        // Repeated grants must not replace the original snapshot with the broom's own grant.
        priorFlight.putIfAbsent(player.getUUID(), player.getAbilities().mayfly
                && !player.isCreative() && !player.isSpectator());
    }

    public boolean release(ServerPlayer player) {
        Boolean previous = priorFlight.remove(player.getUUID());
        if (previous == null) return false;
        boolean allowed = previous || player.isCreative() || player.isSpectator();
        player.getAbilities().mayfly = allowed;
        player.getAbilities().flying = allowed && (player.isSpectator() || player.getAbilities().flying);
        BroomFlightTweak.refreshFlightCache(player);
        player.onUpdateAbilities();
        return true;
    }
}
