package dev.mpthlee.allthetweaks;

import net.minecraftforge.common.ForgeConfigSpec;

public final class TweaksConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue WAYSTONE_PROTECTION;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Independent compatibility fixes. See README for implementation status.")
                .push("tweaks");
        WAYSTONE_PROTECTION = builder
                .comment("Enable the Waystones protection fix. Requires Waystones.",
                        "Reserved for the first fix; protection is not implemented yet.")
                .define("waystoneProtection", true);
        builder.pop();
        SPEC = builder.build();
    }

    private TweaksConfig() {}
}
