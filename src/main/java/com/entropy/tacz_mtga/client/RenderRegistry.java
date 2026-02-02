package com.entropy.tacz_mtga.client;

import com.entropy.tacz_mtga.client.renderer.TurretRenderer;
import com.entropy.tacz_mtga.common.registry.EntityTypeRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.entropy.tacz_mtga.TACZMtga.MODID;

@Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderRegistry {
    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        EntityRenderers.register(EntityTypeRegistry.TURRET.get(), TurretRenderer::new);
        EntityRenderers.register(EntityTypeRegistry.WOODEN_TURRET.get(), TurretRenderer::new);
    }
}
