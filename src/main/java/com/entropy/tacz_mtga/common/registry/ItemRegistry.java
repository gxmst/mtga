package com.entropy.tacz_mtga.common.registry;

import com.entropy.tacz_mtga.TurretItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.entropy.tacz_mtga.TACZMtga.MODID;

public class ItemRegistry {
        public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

        public static final RegistryObject<TurretItem> TURRET = ITEMS.register("turret",
                        () -> new TurretItem(EntityTypeRegistry.TURRET));
        public static final RegistryObject<TurretItem> WOODEN_TURRET = ITEMS.register("wooden_turret",
                        () -> new TurretItem(EntityTypeRegistry.WOODEN_TURRET));
        public static final RegistryObject<TurretItem> VETERAN_TURRET = ITEMS.register("veteran_turret",
                        () -> new TurretItem(EntityTypeRegistry.TURRET));

        public static final RegistryObject<Item> UPGRADE_RANGE = ITEMS.register("upgrade_range",
                        () -> new UpgradeItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<Item> UPGRADE_SPEED = ITEMS.register("upgrade_speed",
                        () -> new UpgradeItem(new Item.Properties().stacksTo(64)));
        public static final RegistryObject<Item> UPGRADE_AMMO = ITEMS.register("upgrade_ammo",
                        () -> new UpgradeItem(new Item.Properties().stacksTo(64)));

        public static final RegistryObject<Item> KEY_BINDING_ITEM = ITEMS.register("key_binding_item",
                        () -> new Item(new Item.Properties()));

        private static class UpgradeItem extends Item {
                public UpgradeItem(Properties properties) {
                        super(properties);
                }

                @Override
                public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
                        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
                        tooltipComponents
                                        .add(Component.translatable(this.getDescriptionId() + ".desc")
                                                        .withStyle(ChatFormatting.GRAY));
                }
        }
}
