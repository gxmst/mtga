package com.entropy.tacz_mtga;

import com.entropy.tacz_mtga.common.registry.AttributeRegistry;
import com.entropy.tacz_mtga.common.registry.EntityTypeRegistry;
import com.entropy.tacz_mtga.common.registry.ItemRegistry;
import com.mojang.logging.LogUtils;
import com.tacz.guns.init.ModCreativeTabs;
import com.entropy.tacz_mtga.common.entity.TurretEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TACZMtga.MODID)
public class TACZMtga {
    public static final String MODID = "tacz_mtga";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TACZMtga(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, TACZMtgaConfig.SPEC);
        IEventBus modEventBus = context.getModEventBus();
        EntityTypeRegistry.TYPES.register(modEventBus);

        ItemRegistry.ITEMS.register(modEventBus);
        modEventBus.addListener(AttributeRegistry::register);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
    }

    public void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ModCreativeTabs.OTHER_TAB.getKey()) {
            event.accept(ItemRegistry.TURRET.get());
            event.accept(ItemRegistry.WOODEN_TURRET.get());

            // Add Veteran Turret with 300 kills pre-installed
            ItemStack veteranTurret = new ItemStack(ItemRegistry.VETERAN_TURRET.get());
            veteranTurret.getOrCreateTag().putInt("KillCount", 300);
            event.accept(veteranTurret);

            event.accept(ItemRegistry.UPGRADE_RANGE.get());
            event.accept(ItemRegistry.UPGRADE_SPEED.get());
            event.accept(ItemRegistry.UPGRADE_AMMO.get());
        }
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getEntity() instanceof TurretEntity turret) {
            if (turret.owner != null && event.getEntity().getUUID().equals(turret.owner)) {
                event.setCanceled(true);
            }
            // Kill Count Bonus:
            // Level 1 (100 kills) -> 1.5x Damage
            // Veteran (300 kills) -> 2.0x Damage (Overwrites Level 1)
            int killCount = turret.getEntityData().get(TurretEntity.KILL_COUNT);
            if (killCount >= 300) {
                event.setAmount(event.getAmount() * 2.0f);
            } else if (killCount >= 100) {
                event.setAmount(event.getAmount() * 1.5f);
            }

            // Balance: Infinite Ammo Upgrade reduces damage to 0.15x
            if (turret.getEntityData().get(TurretEntity.HAS_AMMO_UPGRADE)) {
                event.setAmount(event.getAmount() * 0.15f);
            }
        }

        if (event.getEntity() instanceof TurretEntity turret
                && event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            if (turret.owner != null && turret.owner.equals(player.getUUID())) {
                // Remove shift+attack dismantle logic to avoid conflict
                event.setCanceled(true);
            }
        }
    }
}