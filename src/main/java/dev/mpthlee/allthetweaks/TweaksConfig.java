package dev.mpthlee.allthetweaks;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TweaksConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue WAYSTONE_PROTECTION;
    public static final ForgeConfigSpec.BooleanValue BROOM_FLIGHT_CLEANUP;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Independent compatibility fixes.")
                .push("tweaks");
        WAYSTONE_PROTECTION = builder
                .comment("Enable the Waystones protection fix. Requires Waystones.",
                        "Honors Waystones' generatedWaystonesUnbreakable setting.")
                .define("waystoneProtection", true);
        BROOM_FLIGHT_CLEANUP = builder
                .comment("Restore flight permissions when leaving an EvilCraft broom, including dimension teleports.",
                        "Preserves flight that existed before mounting and creative/spectator flight.")
                .define("broomFlightCleanup", true);
        builder.pop();
        SPEC = builder.build();
    }

    private TweaksConfig() {}
}
