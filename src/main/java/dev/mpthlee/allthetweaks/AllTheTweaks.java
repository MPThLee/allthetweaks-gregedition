package dev.mpthlee.allthetweaks;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

@Mod(AllTheTweaks.MOD_ID)
public final class AllTheTweaks {
    public static final String MOD_ID = "allthetweaks_gregedition";

    public AllTheTweaks() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TweaksConfig.SPEC);
    }
}
