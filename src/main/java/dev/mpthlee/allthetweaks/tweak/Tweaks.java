package dev.mpthlee.allthetweaks.tweak;

import dev.mpthlee.allthetweaks.tweak.waystones.WaystoneTweak;
import dev.mpthlee.allthetweaks.tweak.broom.BroomFlightTweak;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;

/** Explicit registration keeps adding a tweak predictable and avoids classpath scanning. */
public final class Tweaks {
    private static final List<Tweak> ALL = List.of(new WaystoneTweak(), new BroomFlightTweak());

    private Tweaks() {}

    public static void register(IEventBus forgeBus) {
        ALL.forEach(tweak -> tweak.register(forgeBus));
    }

    public static void refresh() {
        ALL.forEach(Tweak::refresh);
    }
}
