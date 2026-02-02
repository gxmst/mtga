package com.entropy.tacz_mtga.common.entity;

import com.entropy.tacz_mtga.TACZMtga;
import com.entropy.tacz_mtga.TACZMtgaConfig;
import com.entropy.tacz_mtga.common.registry.ItemRegistry;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.GunTabType;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.*;
import com.tacz.guns.entity.sync.ModSyncedEntityData;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.item.AmmoItem;
import com.tacz.guns.item.ModernKineticGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.*;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyPlayersSensor;
import net.tslat.smartbrainlib.util.BrainUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Unique;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class TurretEntity extends Mob
        implements SmartBrainOwner<TurretEntity>, IGunOperator, IItemHandler, GeoEntity, ICapabilityProvider {
    public static final EntityType<TurretEntity> TYPE = EntityType.Builder
            .<TurretEntity>of(TurretEntity::new, MobCategory.MISC).sized(1f, 1f).build("turret");
    private LivingEntity currentAngerTarget;
    private static final UniformInt ALERT_INTERVAL = TimeUtil.rangeOfSeconds(4, 6);
    private boolean angry = false;
    public int rangedCooldown = 0;
    public boolean firing = true;
    public int collectiveShots = 0;
    public boolean isReloading = false;
    public long shootTimestamp = 0L;
    public List<LivingEntity> attackers = new ArrayList<>();
    private final LivingEntity tacz$shooter = this;
    public final ShooterDataHolder tacz$data = new ShooterDataHolder();
    private final LivingEntityDrawGun tacz$draw;
    private final LivingEntityAim tacz$aim;
    private final LivingEntityCrawl tacz$crawl;
    private final LivingEntityAmmoCheck tacz$ammoCheck;
    private final LivingEntityFireSelect tacz$fireSelect;
    private final LivingEntityMelee tacz$melee;
    private final LivingEntityShoot tacz$shoot;
    private final LivingEntityBolt tacz$bolt;
    private final LivingEntityReload tacz$reload;
    private final LivingEntitySpeedModifier tacz$speed;
    private final LivingEntitySprint tacz$sprint;
    private boolean drawn = false;
    private final ItemStackHandler inventory = new ItemStackHandler(5);
    private final LazyOptional<ItemStackHandler> optional = LazyOptional.of(() -> inventory);
    public UUID owner;
    public static final EntityDataAccessor<String> stateName = SynchedEntityData.defineId(TurretEntity.class,
            EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Boolean> HAS_RANGE_UPGRADE = SynchedEntityData.defineId(TurretEntity.class,
            EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> HAS_SPEED_UPGRADE = SynchedEntityData.defineId(TurretEntity.class,
            EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> HAS_AMMO_UPGRADE = SynchedEntityData.defineId(TurretEntity.class,
            EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> KILL_COUNT = SynchedEntityData.defineId(TurretEntity.class,
            EntityDataSerializers.INT);
    // public TurretState state = TurretState.NO_GUN;
    private boolean isRedstonePowered = false;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public TurretEntity(Level level, BlockPos pos, Player player) {
        this(TYPE, level, pos, player);
    }

    public TurretEntity(EntityType<? extends TurretEntity> type, Level level, BlockPos pos, Player player) {
        this(type, level);
        setPos(pos.getCenter());
        owner = player.getUUID();
    }

    public TurretEntity(EntityType<? extends TurretEntity> type, Level level) {
        super(type, level);
        initialData();
        this.tacz$draw = new LivingEntityDrawGun(this.tacz$shooter, this.tacz$data);
        this.tacz$aim = new LivingEntityAim(this.tacz$shooter, this.tacz$data);
        this.tacz$crawl = new LivingEntityCrawl(this.tacz$shooter, this.tacz$data);
        this.tacz$ammoCheck = new LivingEntityAmmoCheck(this.tacz$shooter);
        this.tacz$fireSelect = new LivingEntityFireSelect(this.tacz$shooter, this.tacz$data);
        this.tacz$melee = new LivingEntityMelee(this.tacz$shooter, this.tacz$data, this.tacz$draw);
        this.tacz$shoot = new LivingEntityShoot(this.tacz$shooter, this.tacz$data, this.tacz$draw);
        this.tacz$bolt = new LivingEntityBolt(this.tacz$data, this.tacz$shooter, this.tacz$draw, this.tacz$shoot);
        this.tacz$reload = new LivingEntityReload(this.tacz$shooter, this.tacz$data, this.tacz$draw, this.tacz$shoot);
        this.tacz$speed = new LivingEntitySpeedModifier(this.tacz$shooter, this.tacz$data);
        this.tacz$sprint = new LivingEntitySprint(this.tacz$shooter, this.tacz$data);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(stateName, "No Gun");
        entityData.define(HAS_RANGE_UPGRADE, false);
        entityData.define(HAS_SPEED_UPGRADE, false);
        entityData.define(HAS_AMMO_UPGRADE, false);
        entityData.define(KILL_COUNT, 0);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return optional.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        optional.invalidate();
    }

    public static AttributeSupplier.@NotNull Builder createLivingAttributes() {
        // Increase FOLLOW_RANGE to support long-range weapons (e.g. Sniper)
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 128).add(Attributes.ARMOR, 6.0D)
                .add(Attributes.MAX_HEALTH, 200);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.getEntity() instanceof TurretEntity) {
            return false;
        }

        if (pSource.getEntity() instanceof Player player) {
            // Shift + Left Click = Dismantle
            if (player.isShiftKeyDown() && player.getUUID().equals(owner)) {
                if (!level().isClientSide) {
                    dismantle();
                }
                return false;
            }

            // Normal Left Click + Upgrade = Install
            ItemStack stack = player.getMainHandItem();
            if (stack.is(ItemRegistry.UPGRADE_RANGE.get()) || stack.is(ItemRegistry.UPGRADE_SPEED.get())
                    || stack.is(ItemRegistry.UPGRADE_AMMO.get())) {
                if (!level().isClientSide) {
                    installUpgrade(player, stack);
                }
                return false; // Prevent damage
            }
        }

        if (pSource.getEntity() instanceof LivingEntity entity) {
            this.currentAngerTarget = entity;
        }
        // Note: Friendly fire is handled in TACZMtga.java via Event Bus to allow
        // interaction events
        return super.hurt(pSource, pAmount);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void awardKillScore(Entity pKilled, int pScoreValue, DamageSource pSource) {
        super.awardKillScore(pKilled, pScoreValue, pSource);
        if (!level().isClientSide) {
            int kills = entityData.get(KILL_COUNT) + 1;
            entityData.set(KILL_COUNT, kills);

            // Level 3 Upgrade check (Hero Name)
            if (kills == 300) {
                this.setCustomName(net.minecraft.network.chat.Component.literal("Ace Sentry")
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
                this.setCustomNameVisible(true);
                this.playSound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, 1.0f, 0.5f);
            }
        }
    }

    @Override
    public float getVoicePitch() {
        return entityData.get(KILL_COUNT) >= 300 ? 0.7f : super.getVoicePitch();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put("Inventory", inventory.serializeNBT());
        pCompound.putUUID("Owner", owner);
        pCompound.putBoolean("HasRangeUpgrade", entityData.get(HAS_RANGE_UPGRADE));
        pCompound.putBoolean("HasSpeedUpgrade", entityData.get(HAS_SPEED_UPGRADE));
        pCompound.putBoolean("HasAmmoUpgrade", entityData.get(HAS_AMMO_UPGRADE));
        pCompound.putInt("KillCount", entityData.get(KILL_COUNT));
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        inventory.deserializeNBT(pCompound.getCompound("Inventory"));
        owner = pCompound.getUUID("Owner");
        if (pCompound.contains("HasRangeUpgrade"))
            entityData.set(HAS_RANGE_UPGRADE, pCompound.getBoolean("HasRangeUpgrade"));
        if (pCompound.contains("HasSpeedUpgrade"))
            entityData.set(HAS_SPEED_UPGRADE, pCompound.getBoolean("HasSpeedUpgrade"));
        if (pCompound.contains("HasAmmoUpgrade"))
            entityData.set(HAS_AMMO_UPGRADE, pCompound.getBoolean("HasAmmoUpgrade"));
        if (pCompound.contains("KillCount"))
            entityData.set(KILL_COUNT, pCompound.getInt("KillCount"));
    }

    @Override
    protected void dropCustomDeathLoot(@NotNull DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        for (int i = 0; i < inventory.getSlots() - 1; i++) {
            this.spawnAtLocation(inventory.extractItem(i, inventory.getStackInSlot(i).getCount(), false));
        }
        for (EquipmentSlot equipmentslot : EquipmentSlot.values()) {
            ItemStack itemstack = this.getItemBySlot(equipmentslot);
            if (!itemstack.isEmpty()) {
                if (itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(itemstack.getMaxDamage()
                            - this.random.nextInt(1 + this.random.nextInt(Math.max(itemstack.getMaxDamage() - 3, 1))));
                }

                this.spawnAtLocation(itemstack);
                this.setItemSlot(equipmentslot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        // 强制返回 false，告诉系统：无论玩家跑多远，永远不要删除我！
        return false;
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        // 1. 如果原本就是队友（比如在同一个记分板队伍里），直接返回 true
        if (super.isAlliedTo(entity)) {
            return true;
        }

        // 2. 【核心逻辑】如果这个实体是我的主人，那他就是我的死党！
        if (entity instanceof LivingEntity && this.owner != null) {
            return entity.getUUID().equals(this.owner);
        }

        return false;
    }

    public boolean isRedstonePowered() {
        return isRedstonePowered;
    }

    public BrainActivityGroup<? extends TurretEntity> getIdleTasks() {
        return BrainActivityGroup.idleTasks(new Behavior[] {
                new FirstApplicableBehaviour<TurretEntity>(new TargetOrRetaliate<>(), new SetPlayerLookTarget<>(),
                        new SetRandomLookTarget<>()),
                (new Idle<>()).runFor((entity) -> RandomSource.create().nextInt(30, 60)) });
    }

    public BrainActivityGroup<? extends TurretEntity> getFightTasks() {
        return BrainActivityGroup
                .fightTasks(new Behavior[] { new InvalidateAttackTarget<>(), new SetRetaliateTarget<>(),
                        (new TaczShootAttack<>(32)
                                .startCondition((x$0) -> this.getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get())
                                        && this.collectiveShots <= this.getStateBurst())) });
    }

    @Override
    public List<? extends ExtendedSensor<? extends TurretEntity>> getSensors() {
        return ObjectArrayList.of(
                new NearbyPlayersSensor<TurretEntity>().setPredicate((p, e) -> e.attackers.contains(p)),
                new HurtBySensor<>(),
                new NearbyLivingEntitySensor<TurretEntity>()
                        .setPredicate((target, entity) -> target == this.currentAngerTarget || target instanceof Monster
                                || target.getType().getCategory() == MobCategory.MONSTER));
    }

    public GunTabType heldGunType() {
        if (this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if (TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).isPresent()) {
                return switch (TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).get().getType()) {
                    case "pistol" -> GunTabType.PISTOL;
                    case "rifle" -> GunTabType.RIFLE;
                    case "sniper" -> GunTabType.SNIPER;
                    case "smg" -> GunTabType.SMG;
                    case "rpg" -> GunTabType.RPG;
                    case "shotgun" -> GunTabType.SHOTGUN;
                    case "mg" -> GunTabType.MG;
                    default ->
                        throw new IllegalStateException("Unexpected value: "
                                + TimelessAPI.getCommonGunIndex(gun.getGunId(this.getMainHandItem())).get().getType());
                };
            }
        }
        return GunTabType.PISTOL;
    }

    public double getGunRange() {
        double range = 16;
        if (heldGunType() != null) {
            range = switch (heldGunType()) {
                case SNIPER -> 50; // Sniper range
                case RIFLE, MG -> 30; // Rifle/MG range
                case PISTOL, SMG -> 17; // Close-mid range
                case SHOTGUN -> 10; // Close range
                case RPG -> 30;
                default -> 16;
            };
        }
        // Fire Control Radar (Range Upgrade) now doubles the range
        if (entityData.get(HAS_RANGE_UPGRADE)) {
            range *= 2;
        }
        return range;
    }

    public int getStateRangedCooldown() {
        int cooldown = 60;
        if (heldGunType() != null) {
            cooldown = switch (heldGunType()) {
                case RIFLE -> 10;
                case PISTOL -> 8;
                case SNIPER -> 30;
                case SHOTGUN -> 20;
                case SMG, MG -> 5;
                case RPG -> 100;
            };
        }
        // Balance: Water Cooling (Speed Upgrade) increases fire rate by 1.2x (0.83x
        // cooldown)
        if (entityData.get(HAS_SPEED_UPGRADE)) {
            cooldown = (int) (cooldown * 0.833f);
        }
        if (entityData.get(KILL_COUNT) >= 300) {
            cooldown /= 2;
        }
        return cooldown;
    }

    int getStateBurst() {
        if (heldGunType() != null) {
            return switch (heldGunType()) {
                case RIFLE -> 2;
                case PISTOL -> 3;
                case SNIPER, SHOTGUN, RPG -> 1;
                case SMG, MG -> 4;
            };
        }
        return 1;
    }

    protected Brain.@NotNull Provider<?> brainProvider() {
        return new SmartBrainProvider<>(this);
    }

    @Override
    protected void customServerAiStep() {
        this.tickBrain(this);
        if (this.level().getGameTime() % 10 == 0) {
            this.isRedstonePowered = this.level().hasNeighborSignal(this.blockPosition());
            updateTargetLogic();
        }
    }

    private void updateTargetLogic() {
        if (this.isRedstonePowered) {
            this.setTarget(null);
            BrainUtils.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
            return;
        }

        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null) {
            if (!currentTarget.isAlive() || currentTarget.isRemoved() || !this.hasLineOfSight(currentTarget)) {
                this.setTarget(null);
                BrainUtils.clearMemory(this, MemoryModuleType.ATTACK_TARGET);
                currentTarget = null;
            }
        }

        // Priority 1: Self Defense (Anger Target)
        if (this.currentAngerTarget != null && this.currentAngerTarget.isAlive()
                && !this.currentAngerTarget.isRemoved()) {
            if (this.hasLineOfSight(this.currentAngerTarget)
                    && this.distanceToSqr(this.currentAngerTarget) <= this.getGunRange() * this.getGunRange()) {
                if (currentTarget != this.currentAngerTarget) {
                    this.setTarget(this.currentAngerTarget);
                    BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, this.currentAngerTarget);
                }
                return; // Attack the one hitting us!
            }
        }

        // Priority 2: Closest Visible Enemy
        NearestVisibleLivingEntities visibleEntitiesWrapper = BrainUtils.getMemory(this,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        if (visibleEntitiesWrapper != null) {
            Iterable<LivingEntity> visibleEntities = visibleEntitiesWrapper.findAll(e -> true);
            LivingEntity closest = null;
            double closestDistSqr = Double.MAX_VALUE;
            double maxRange = this.getGunRange();
            double maxRangeSqr = maxRange * maxRange;

            for (LivingEntity entity : visibleEntities) {
                boolean isEnemy = entity == this.currentAngerTarget || entity instanceof Monster
                        || entity.getType().getCategory() == MobCategory.MONSTER;
                if (!isEnemy)
                    continue;

                if (this.isAlliedTo(entity))
                    continue;

                double distSqr = this.distanceToSqr(entity);
                if (distSqr > maxRangeSqr)
                    continue;

                if (distSqr < closestDistSqr) {
                    closestDistSqr = distSqr;
                    closest = entity;
                }
            }

            if (closest != null && closest != currentTarget) {
                this.setTarget(closest);
                BrainUtils.setMemory(this, MemoryModuleType.ATTACK_TARGET, closest);
            }
        }
    }

    public void pickUpItem(@NotNull ItemEntity pItemEntity) {
        if (!this.isDeadOrDying()) {
            ItemStack itemstack = pItemEntity.getItem();
            if (this.wantsToPickUp(itemstack)) {
                for (int slot = 0; slot < inventory.getSlots(); slot++) {
                    if (inventory.getStackInSlot(slot).isEmpty()
                            || (inventory.getStackInSlot(slot).is(itemstack.getItem())
                                    && itemstack.is(ModItems.AMMO.get()))) {
                        if (slot == 0) {
                            if (itemstack.is(ModItems.MODERN_KINETIC_GUN.get())) {
                                this.onItemPickup(pItemEntity);
                                setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemstack);
                                this.take(pItemEntity, itemstack.getCount());
                                pItemEntity.discard();
                                return;
                            }
                        } else {
                            this.onItemPickup(pItemEntity);
                            int count = Math.min(itemstack.getCount(), inventory.getStackInSlot(slot).getMaxStackSize()
                                    - inventory.getStackInSlot(slot).getCount());
                            inventory.setStackInSlot(slot,
                                    itemstack.copyWithCount(count + inventory.getStackInSlot(slot).getCount()));
                            if (count >= itemstack.getCount()) {
                                this.take(pItemEntity, count);
                                pItemEntity.discard();
                                return;
                            } else if (count > 0) {
                                this.take(pItemEntity, count);
                                pItemEntity.setItem(itemstack.copyWithCount(itemstack.getCount() - count));
                                itemstack = pItemEntity.getItem();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.getTarget() == null && this.angry) {
            angry = false;
        }
        if (this.currentAngerTarget != null && !this.currentAngerTarget.isAlive()) {
            this.currentAngerTarget = null;
        }
        onTickServerSide();

        // Level 3 Effects: Light and Auto-Repair
        int kills = entityData.get(KILL_COUNT);
        if (kills >= 300) {
            if (!level().isClientSide) {
                if (tickCount % 20 == 0) {
                    BlockPos pos = blockPosition();
                    if (level().getBlockState(pos).isAir()) {
                        level().setBlock(pos, net.minecraft.world.level.block.Blocks.LIGHT.defaultBlockState()
                                .setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 15), 3);
                    }
                }
                if (tickCount % 40 == 0) {
                    this.heal(1.0f);
                }
            }
        }

        if (getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
            if (this.getMainHandItem().getOrCreateTag().getInt("GunCurrentAmmoCount") == 0
                    && !this.getMainHandItem().getOrCreateTag().getBoolean("HasBulletInBarrel")) {
                this.reload();
            }
            if (gun.getCurrentAmmoCount(getMainHandItem()) > 0) {
                this.isReloading = false;
            } else {
                if (!this.isReloading) {
                    this.reload();
                }
                this.isReloading = true;

            }
        }

        if (firing && shootTimestamp != 0) {
            if ((System.currentTimeMillis() - shootTimestamp) / 100 > getStateRangedCooldown()) {
                collectiveShots = 0;
                shootTimestamp = 0;
                firing = false;
                aim(false);
            }
        }
        if (rangedCooldown != 0) {
            rangedCooldown--;
        }
        // Optimization: Only scan for items every 20 ticks (1 second) instead of every
        // tick
        if (this.tickCount % 20 == 0 && !level().isClientSide()) {
            List<ItemEntity> items = this.level().getEntitiesOfClass(ItemEntity.class,
                    this.getBoundingBox().inflate(1.1));
            if (!items.isEmpty()) {
                for (ItemEntity item : items) {
                    if ((item.getItem().getItem() instanceof ModernKineticGunItem && getMainHandItem().isEmpty())
                            || (item.getItem().getItem() instanceof AmmoItem ammo
                                    && ammo.isAmmoOfGun(getMainHandItem(), item.getItem())
                                    && TACZMtgaConfig.consumeAmmo)) {
                        pickUpItem(item);
                    }
                }
            }
        }

        // Auto-mount minecarts/boats logic
        if (!this.isPassenger() && this.tickCount % 5 == 0 && !level().isClientSide()) {
            List<Entity> vehicles = this.level().getEntities(this, this.getBoundingBox().inflate(0.2),
                    e -> e instanceof net.minecraft.world.entity.vehicle.AbstractMinecart
                            || e instanceof net.minecraft.world.entity.vehicle.Boat);
            if (!vehicles.isEmpty()) {
                // Check if vehicle has space
                Entity vehicle = vehicles.get(0);
                if (!vehicle.isVehicle() || vehicle.getPassengers().isEmpty()) {
                    this.startRiding(vehicle);
                }
            }
        }

        super.tick();
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (player.getUUID() != owner) {
            return super.mobInteract(player, hand);
        }

        ItemStack stack = player.getItemInHand(hand);

        // 1. Equip Gun (Right Click with Gun when empty)
        if (!player.isShiftKeyDown() && getMainHandItem().isEmpty() && stack.is(ModItems.MODERN_KINETIC_GUN.get())) {
            setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
            if (!player.isCreative())
                player.setItemInHand(hand, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }

        // 3. Take Gun (Right Click when armed) - NO SHIFT
        if (!getMainHandItem().isEmpty() && !player.isShiftKeyDown()) {
            player.getInventory().add(getMainHandItem());
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (!inventory.getStackInSlot(slot).isEmpty()) {
                    player.getInventory()
                            .add(inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false));
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void draw(Supplier<ItemStack> gunItemSupplier) {
        this.tacz$draw.draw(gunItemSupplier);
        this.drawn = true;
    }

    public void reload() {
        this.tacz$reload.reload();
        this.isReloading = true;
    }

    public void aim(boolean isAim) {
        this.tacz$aim.aim(isAim);
    }

    public void crawl(boolean isCrawl) {
        this.tacz$crawl.crawl(isCrawl);
    }

    public void updateCacheProperty(AttachmentCacheProperty cacheProperty) {
        this.tacz$data.cacheProperty = cacheProperty;
    }

    @Nullable
    public AttachmentCacheProperty getCacheProperty() {
        return this.tacz$data.cacheProperty;
    }

    public void fireSelect() {
        this.tacz$fireSelect.fireSelect();
    }

    public void zoom() {
        this.tacz$aim.zoom();
    }

    public long getSynShootCoolDown() {
        return ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public long getSynMeleeCoolDown() {
        return ModSyncedEntityData.MELEE_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public long getSynDrawCoolDown() {
        return ModSyncedEntityData.DRAW_COOL_DOWN_KEY.getValue(this.tacz$shooter);
    }

    public boolean getSynIsBolting() {
        return ModSyncedEntityData.IS_BOLTING_KEY.getValue(this.tacz$shooter);
    }

    public ReloadState getSynReloadState() {
        return ModSyncedEntityData.RELOAD_STATE_KEY.getValue(this.tacz$shooter);
    }

    public float getSynAimingProgress() {
        return ModSyncedEntityData.AIMING_PROGRESS_KEY.getValue(this.tacz$shooter);
    }

    public float getSynSprintTime() {
        return ModSyncedEntityData.SPRINT_TIME_KEY.getValue(this.tacz$shooter);
    }

    public boolean getSynIsAiming() {
        return ModSyncedEntityData.IS_AIMING_KEY.getValue(this.tacz$shooter);
    }

    public void initialData() {
        this.tacz$data.initialData();
        AttachmentPropertyManager.postChangeEvent(this.tacz$shooter, this.tacz$shooter.getMainHandItem());
    }

    public void dismantle() {
        if (!getMainHandItem().isEmpty()) {
            this.spawnAtLocation(getMainHandItem());
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            if (!inventory.getStackInSlot(slot).isEmpty()) {
                this.spawnAtLocation(inventory.extractItem(slot, inventory.getStackInSlot(slot).getCount(), false));
            }
        }
        if (this.getType() == com.entropy.tacz_mtga.common.registry.EntityTypeRegistry.WOODEN_TURRET.get()) {
            ItemStack turretStack = new ItemStack(ItemRegistry.WOODEN_TURRET.get());
            CompoundTag tag = turretStack.getOrCreateTag();
            if (entityData.get(HAS_RANGE_UPGRADE))
                tag.putBoolean("HasRangeUpgrade", true);
            if (entityData.get(HAS_SPEED_UPGRADE))
                tag.putBoolean("HasSpeedUpgrade", true);
            if (entityData.get(HAS_AMMO_UPGRADE))
                tag.putBoolean("HasAmmoUpgrade", true);
            tag.putInt("KillCount", entityData.get(KILL_COUNT));
            if (this.hasCustomName()) {
                turretStack.setHoverName(this.getCustomName());
            }
            this.spawnAtLocation(turretStack);
        } else {
            ItemStack turretStack = new ItemStack(ItemRegistry.TURRET.get());
            CompoundTag tag = turretStack.getOrCreateTag();
            if (entityData.get(HAS_RANGE_UPGRADE))
                tag.putBoolean("HasRangeUpgrade", true);
            if (entityData.get(HAS_SPEED_UPGRADE))
                tag.putBoolean("HasSpeedUpgrade", true);
            if (entityData.get(HAS_AMMO_UPGRADE))
                tag.putBoolean("HasAmmoUpgrade", true);
            tag.putInt("KillCount", entityData.get(KILL_COUNT));
            if (this.hasCustomName()) {
                turretStack.setHoverName(this.getCustomName());
            }
            this.spawnAtLocation(turretStack);
        }
        this.discard();
    }

    public void installUpgrade(Player player, ItemStack stack) {
        if (stack.is(ItemRegistry.UPGRADE_RANGE.get())) {
            if (!entityData.get(HAS_RANGE_UPGRADE)) {
                if (!player.isCreative())
                    stack.shrink(1);
                entityData.set(HAS_RANGE_UPGRADE, true);
                playSound(net.minecraft.sounds.SoundEvents.ANVIL_USE, 1.0f, 1.0f);
                if (level().isClientSide) {
                    for (int i = 0; i < 5; i++) {
                        level().addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                this.getX() + (random.nextDouble() - 0.5D) * this.getBbWidth(),
                                this.getEyeY() + random.nextDouble() * 0.5D,
                                this.getZ() + (random.nextDouble() - 0.5D) * this.getBbWidth(), 0, 0, 0);
                    }
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("\uD83D\uDD25 Fire control radar installed!"),
                            true);
                }
            }
        } else if (stack.is(ItemRegistry.UPGRADE_SPEED.get())) {
            if (!entityData.get(HAS_SPEED_UPGRADE)) {
                if (!player.isCreative())
                    stack.shrink(1);
                entityData.set(HAS_SPEED_UPGRADE, true);
                playSound(net.minecraft.sounds.SoundEvents.ANVIL_USE, 1.0f, 1.0f);
                if (level().isClientSide) {
                    for (int i = 0; i < 5; i++) {
                        level().addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                this.getX() + (random.nextDouble() - 0.5D) * this.getBbWidth(),
                                this.getEyeY() + random.nextDouble() * 0.5D,
                                this.getZ() + (random.nextDouble() - 0.5D) * this.getBbWidth(), 0, 0, 0);
                    }
                    player.displayClientMessage(net.minecraft.network.chat.Component
                            .literal("\u2744\uFE0F Water cooling system installed!"), true);
                }
            }
        } else if (stack.is(ItemRegistry.UPGRADE_AMMO.get())) {
            if (!entityData.get(HAS_AMMO_UPGRADE)) {
                if (!player.isCreative())
                    stack.shrink(1);
                entityData.set(HAS_AMMO_UPGRADE, true);
                playSound(net.minecraft.sounds.SoundEvents.ANVIL_USE, 1.0f, 1.0f);
                if (level().isClientSide) {
                    for (int i = 0; i < 5; i++) {
                        level().addParticle(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                this.getX() + (random.nextDouble() - 0.5D) * this.getBbWidth(),
                                this.getEyeY() + random.nextDouble() * 0.5D,
                                this.getZ() + (random.nextDouble() - 0.5D) * this.getBbWidth(), 0, 0, 0);
                    }
                    player.displayClientMessage(net.minecraft.network.chat.Component
                            .literal("\u26A1 Ammo manufacturing factory installed!"), true);
                }
            }
        }
    }

    public void bolt() {
        this.tacz$bolt.bolt();
    }

    public void cancelReload() {
        this.tacz$reload.cancelReload();
    }

    public void melee() {
        this.tacz$melee.melee();
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return this.shoot(pitch, yaw, System.currentTimeMillis() - this.tacz$data.baseTimestamp);
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        this.shootTimestamp = System.currentTimeMillis();
        return this.tacz$shoot.shoot(pitch, yaw, timestamp);
    }

    public boolean needCheckAmmo() {
        return TACZMtgaConfig.consumeAmmo && !entityData.get(HAS_AMMO_UPGRADE);
    }

    public boolean consumesAmmoOrNot() {
        return this.tacz$ammoCheck.consumesAmmoOrNot();
    }

    @Unique
    public boolean getProcessedSprintStatus(boolean sprint) {
        return this.tacz$sprint.getProcessedSprintStatus(sprint);
    }

    public ShooterDataHolder getDataHolder() {
        return this.tacz$data;
    }

    public boolean nextBulletIsTracer(int tracerCountInterval) {
        ++this.tacz$data.shootCount;
        return true;
    }

    private void onTickServerSide() {
        if (!this.level().isClientSide()) {
            if (this.getMainHandItem().getItem() instanceof ModernKineticGunItem gun) {
                if (!drawn) {
                    this.draw(this::getMainHandItem);
                }
                ItemStack gunItem = this.getMainHandItem();
                ResourceLocation gunId = gun.getGunId(gunItem);
                IGun iGun = IGun.getIGunOrNull(gunItem);
                if (iGun != null) {
                    Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
                    if (gunIndexOptional.isPresent()) {
                        CommonGunIndex gunIndex = gunIndexOptional.get();
                        GunData gunData = gunIndex.getGunData();
                        AttachmentCacheProperty property = new AttachmentCacheProperty();
                        property.eval(this.getMainHandItem(), gunData);

                        updateCacheProperty(property);
                    }
                }
            }
            this.bolt();
            ReloadState reloadState = this.tacz$reload.tickReloadState();
            // Elite Reload Speed: Tick reload state multiple times to significantly
            // increase reload speed
            if (entityData.get(KILL_COUNT) >= 300 && reloadState.getStateType().isReloading()) {
                for (int i = 0; i < 4; i++) {
                    reloadState = this.tacz$reload.tickReloadState();
                    if (!reloadState.getStateType().isReloading())
                        break;
                }
            }
            this.tacz$aim.tickAimingProgress();
            this.tacz$aim.tickSprint();
            this.tacz$crawl.tickCrawling();
            this.tacz$bolt.tickBolt();
            this.tacz$melee.scheduleTickMelee();
            this.tacz$speed.updateSpeedModifier();
            this.tacz$shooter.setSprinting(this.getProcessedSprintStatus(this.tacz$shooter.isSprinting()));
            ModSyncedEntityData.SHOOT_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$shoot.getShootCoolDown());
            ModSyncedEntityData.MELEE_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$melee.getMeleeCoolDown());
            ModSyncedEntityData.DRAW_COOL_DOWN_KEY.setValue(this.tacz$shooter, this.tacz$draw.getDrawCoolDown());
            ModSyncedEntityData.IS_BOLTING_KEY.setValue(this.tacz$shooter, this.tacz$data.isBolting);
            ModSyncedEntityData.RELOAD_STATE_KEY.setValue(this.tacz$shooter, reloadState);
            ModSyncedEntityData.AIMING_PROGRESS_KEY.setValue(this.tacz$shooter, this.tacz$data.aimingProgress);
            ModSyncedEntityData.IS_AIMING_KEY.setValue(this.tacz$shooter, this.tacz$data.isAiming);
            ModSyncedEntityData.SPRINT_TIME_KEY.setValue(this.tacz$shooter, this.tacz$data.sprintTimeS);
            boolean hasAmmo = !TACZMtgaConfig.consumeAmmo;
            if (TACZMtgaConfig.consumeAmmo) {
                BlockEntity blockEntity = level().getBlockEntity(blockPosition()) == null
                        ? level().getBlockEntity(blockPosition().below())
                        : level().getBlockEntity(blockPosition());
                if (blockEntity != null
                        && blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().isPresent()) {
                    IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).resolve().get();
                    for (int slot = 0; slot < getSlots(); slot++) {
                        for (int s = 0; s < handler.getSlots(); s++) {
                            ItemStack stack = handler.getStackInSlot(s);
                            if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(getMainHandItem(), stack)
                                    && getStackInSlot(slot).getCount() < getStackInSlot(slot).getMaxStackSize()) {
                                ItemStack remainder = insertItem(slot, handler.extractItem(s, stack.getCount(), false),
                                        false);
                                if (!remainder.isEmpty()) {
                                    handler.insertItem(s, remainder, false);
                                }
                            }
                        }
                        if (getStackInSlot(slot).getItem() instanceof IAmmo ammo
                                && ammo.isAmmoOfGun(getMainHandItem(), getStackInSlot(slot))) {
                            hasAmmo = true;
                        }
                    }
                }
            }
            if (this.getMainHandItem().getOrCreateTag().getInt("GunCurrentAmmoCount") > 0
                    || this.getMainHandItem().getOrCreateTag().getBoolean("HasBulletInBarrel")) {
                hasAmmo = true;
            }
            if (getMainHandItem().is(ModItems.MODERN_KINETIC_GUN.get())) {
                if (reloadState.getStateType() == ReloadState.StateType.NOT_RELOADING) {
                    if (hasAmmo) {
                        TurretState.ACTIVE.setState(this);
                    } else {
                        TurretState.NO_AMMO.setState(this);
                    }
                } else {
                    TurretState.RELOADING.setState(this);
                }
            } else {
                TurretState.NO_GUN.setState(this);
            }
        }
    }

    public void setTarget(@Nullable LivingEntity pLivingEntity) {
        if (this.getTarget() == null && pLivingEntity != null) {
            ALERT_INTERVAL.sample(this.random);
        }

        if (pLivingEntity instanceof Player) {
            this.setLastHurtByPlayer((Player) pLivingEntity);
        }

        super.setTarget(pLivingEntity);
    }

    @Override
    public BrainActivityGroup<? extends TurretEntity> getCoreTasks() {
        return BrainActivityGroup.coreTasks(new Behavior[] {
                new TargetOrRetaliate<TurretEntity>().isAllyIf((e, l) -> l instanceof TurretEntity)
                        .attackablePredicate(l -> l != null && this.hasLineOfSight(l))
                        .alertAlliesWhen((m, e) -> e != null && m.hasLineOfSight(e)).runFor((e) -> 999),
                (new LookAtTarget<>()).runFor((entity) -> RandomSource.create().nextInt(40, 300)) });
    }

    @Override
    public int getSlots() {
        return inventory.getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return inventory.insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return inventory.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return inventory.isItemValid(slot, stack);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    @Override
    public boolean isPushable() {
        return true; // Allow pushing into minecarts/boats
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() != null) {
            // Allow independent rotation when riding
            this.setYBodyRot(this.getYHeadRot());
        }
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {

    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    public enum TurretState {
        ACTIVE("Active"), RELOADING("Reloading"), NO_AMMO("No Ammo"), NO_GUN("No Gun");

        private final String name;
        private final String texture;

        TurretState(String name) {
            this.name = name;
            this.texture = name.toLowerCase(Locale.ROOT).replaceAll(" ", "_");
        }

        public void setState(TurretEntity turret) {
            turret.entityData.set(stateName, name);
        }

        public ResourceLocation getPath() {
            return new ResourceLocation(TACZMtga.MODID, "textures/entity/turret_" + texture + ".png");
        }

        public static TurretState getState(TurretEntity turret) {
            for (TurretState state : values()) {
                if (turret.getEntityData().get(stateName).equals(state.name))
                    return state;
            }
            return NO_GUN;
        }
    }
}