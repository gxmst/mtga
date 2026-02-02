package com.entropy.tacz_mtga.common.registry;

import com.entropy.tacz_mtga.common.entity.TurretEntity;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class AttributeRegistry {
    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(EntityTypeRegistry.TURRET.get(), TurretEntity.createLivingAttributes().build());
        event.put(EntityTypeRegistry.WOODEN_TURRET.get(),
                TurretEntity.createLivingAttributes()
                        .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 20.0D).build());
    }
}
