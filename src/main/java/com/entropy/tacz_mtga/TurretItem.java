package com.entropy.tacz_mtga;

import com.entropy.tacz_mtga.client.renderer.TurretItemRenderer;
import com.entropy.tacz_mtga.common.entity.TurretEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TurretItem extends Item implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final Supplier<EntityType<TurretEntity>> typeSupplier;

    public TurretItem(Supplier<EntityType<TurretEntity>> typeSupplier) {
        super(new Item.Properties());
        this.typeSupplier = typeSupplier;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private TurretItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null)
                    renderer = new TurretItemRenderer();
                return renderer;
            }
        });
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide() || context.getPlayer() == null) {
            return InteractionResult.CONSUME;
        }
        TurretEntity turret = new TurretEntity(typeSupplier.get(), context.getLevel(),
                context.getClickedPos().relative(context.getClickedFace()), context.getPlayer());

        CompoundTag tag = context.getItemInHand().getTag();
        if (tag != null) {
            if (tag.contains("HasRangeUpgrade")) {
                turret.getEntityData().set(TurretEntity.HAS_RANGE_UPGRADE, tag.getBoolean("HasRangeUpgrade"));
            }
            if (tag.contains("HasSpeedUpgrade")) {
                turret.getEntityData().set(TurretEntity.HAS_SPEED_UPGRADE, tag.getBoolean("HasSpeedUpgrade"));
            }
            if (tag.contains("HasAmmoUpgrade")) {
                turret.getEntityData().set(TurretEntity.HAS_AMMO_UPGRADE, tag.getBoolean("HasAmmoUpgrade"));
            }
            if (tag.contains("KillCount")) {
                turret.getEntityData().set(TurretEntity.KILL_COUNT, tag.getInt("KillCount"));
            }
        }

        if (context.getItemInHand().hasCustomHoverName()) {
            turret.setCustomName(context.getItemInHand().getHoverName());
        }

        context.getLevel().addFreshEntity(turret);
        if (!context.getPlayer().isCreative())
            context.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
            TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        tooltipComponents.add(Component.translatable(this.getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));

        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (tag.getBoolean("HasRangeUpgrade")) {
                tooltipComponents.add(Component.literal("Fire Control Radar Installed").withStyle(ChatFormatting.RED));
            }
            if (tag.getBoolean("HasSpeedUpgrade")) {
                tooltipComponents
                        .add(Component.literal("Water Cooling System Installed").withStyle(ChatFormatting.AQUA));
            }
            if (tag.getBoolean("HasAmmoUpgrade")) {
                tooltipComponents
                        .add(Component.literal("Ammo Manufacturing Factory Installed").withStyle(ChatFormatting.GOLD));
            }
            if (tag.contains("KillCount")) {
                int kills = tag.getInt("KillCount");
                tooltipComponents.add(Component.literal("Kills: " + kills).withStyle(ChatFormatting.GRAY));
                if (kills >= 300) {
                    tooltipComponents.add(Component.literal("Rank: Elite").withStyle(ChatFormatting.GOLD));
                } else if (kills >= 100) {
                    tooltipComponents.add(Component.literal("Rank: Veteran").withStyle(ChatFormatting.YELLOW));
                } else {
                    tooltipComponents.add(Component.literal("Rank: Recruit").withStyle(ChatFormatting.WHITE));
                }
            }
        }
    }
}
