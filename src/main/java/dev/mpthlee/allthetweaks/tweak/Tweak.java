package dev.mpthlee.allthetweaks.tweak;

import net.minecraftforge.eventbus.api.IEventBus;

/** One independently configurable fix. Register hooks once; refresh cached config on load/reload. */
public interface Tweak {
    default void register(IEventBus forgeBus) {}
    void refresh();
}
