package dev.mpthlee.allthetweaks.tweak.waystones;

import dev.mpthlee.allthetweaks.tweak.Tweak;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;

public final class WaystoneTweak implements Tweak {
    @Override
    public void register(IEventBus forgeBus) {
        forgeBus.addListener(this::onBreak);
    }

    @Override
    public void refresh() { WaystoneProtection.refresh(); }

    private void onBreak(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative()
                && WaystoneProtection.isProtected(event.getLevel(), event.getPos(), event.getState())) {
            event.setCanceled(true);
        }
    }
}
