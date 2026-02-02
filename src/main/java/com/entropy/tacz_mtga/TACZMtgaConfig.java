package com.entropy.tacz_mtga;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = TACZMtga.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TACZMtgaConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue CONSUME_AMMO = BUILDER
            .comment("Whether turrets need ammo")
            .define("consumeAmmo", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean consumeAmmo;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        consumeAmmo = CONSUME_AMMO.get();
    }
}