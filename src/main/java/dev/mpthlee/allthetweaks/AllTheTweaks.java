package dev.mpthlee.allthetweaks;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

@Mod(AllTheTweaks.MOD_ID)
public final class AllTheTweaks {
    public static final String MOD_ID = "allthetweaks_gregedition";

    public AllTheTweaks() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TweaksConfig.SPEC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configChanged);
        MinecraftForge.EVENT_BUS.addListener(this::onBreak);
    }

    private void configChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() == TweaksConfig.SPEC) Protection.refresh();
    }

    private void onBreak(BlockEvent.BreakEvent event) {
        if (!event.getPlayer().isCreative()
                && Protection.isProtected(event.getLevel(), event.getPos(), event.getState())) {
            event.setCanceled(true);
        }
    }
}
