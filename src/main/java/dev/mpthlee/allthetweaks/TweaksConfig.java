package dev.mpthlee.allthetweaks;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TweaksConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue WAYSTONE_PROTECTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Independent compatibility fixes.")
                .push("tweaks");
        WAYSTONE_PROTECTION = builder
                .comment("Enable the Waystones protection fix. Requires Waystones.",
                        "Honors Waystones' generatedWaystonesUnbreakable setting.")
                .define("waystoneProtection", true);
        builder.pop();
        SPEC = builder.build();
    }

    private TweaksConfig() {}
}
