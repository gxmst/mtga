package com.entropy.tacz_mtga.common.registry;

import com.entropy.tacz_mtga.common.entity.TurretEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

import static com.entropy.tacz_mtga.TACZMtga.MODID;

@Mod.EventBusSubscriber
public class EntityTypeRegistry {
        public static final DeferredRegister<EntityType<?>> TYPES = DeferredRegister.create(
                        ForgeRegistries.ENTITY_TYPES,
                        MODID);

        public static final RegistryObject<EntityType<TurretEntity>> TURRET = TYPES.register("turret",
                        () -> TurretEntity.TYPE);

        public static final RegistryObject<EntityType<TurretEntity>> WOODEN_TURRET = TYPES.register("wooden_turret",
                        () -> EntityType.Builder
                                        .<TurretEntity>of(TurretEntity::new,
                                                        net.minecraft.world.entity.MobCategory.MISC)
                                        .sized(1f, 1f).build("wooden_turret"));

        @SubscribeEvent
        public static void register(RegisterEvent event) {

        }
}
