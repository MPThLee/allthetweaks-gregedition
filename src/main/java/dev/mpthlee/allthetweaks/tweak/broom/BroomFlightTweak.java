package dev.mpthlee.allthetweaks.tweak.broom;

import dev.mpthlee.allthetweaks.TweaksConfig;
import dev.mpthlee.allthetweaks.tweak.Tweak;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraft.server.level.ServerPlayer;

public final class BroomFlightTweak implements Tweak {
    private static volatile boolean enabled;

    @Override
    public void register(IEventBus forgeBus) {
        forgeBus.addListener(this::onDimensionChanged);
    }

    private void onDimensionChanged(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        var vehicle = player.getVehicle();
        // Forge's ITeleporter path can retain a vehicle in the old dimension.
        // Only detach a broom whose flight this tweak owns; its removal hook restores abilities.
        if (vehicle instanceof BroomFlightLease.Owner owner && vehicle.level() != player.level()
                && owner.hasFlightLease(player)) player.stopRiding();
    }

    @Override
    public void refresh() {
        enabled = ModList.get().isLoaded("evilcraft") && TweaksConfig.SPEC.isLoaded()
                && TweaksConfig.BROOM_FLIGHT_CLEANUP.get();
    }

    public static boolean enabled() { return enabled; }
}
