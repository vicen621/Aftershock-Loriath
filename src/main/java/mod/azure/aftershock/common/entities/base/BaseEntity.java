package mod.azure.aftershock.common.entities.base;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.helper.AzureVibrationListener;
import mod.azure.azurelib.helper.AzureVibrationListener.AzureVibrationListenerConfig;
import mod.azure.azurelib.helper.Growable;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.AngerManagement;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.tslat.smartbrainlib.util.BrainUtils;

public abstract class BaseEntity extends Monster implements GeoEntity, Growable, AzureVibrationListenerConfig {

	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	protected static final EntityDataAccessor<Float> GROWTH = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<AttackType> CURRENT_ATTACK_TYPE = SynchedEntityData.defineId(BaseEntity.class, AftershockMod.ALIEN_ATTACK_TYPE);
	protected static final EntityDataAccessor<Integer> CLIENT_ANGER_LEVEL = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Boolean> EAT = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> PUKE = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> SCREAM = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> BIRTH = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> SEARCHING = SynchedEntityData.defineId(BaseEntity.class, EntityDataSerializers.BOOLEAN);
	protected static final Logger LOGGER = LogUtils.getLogger();
	public static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = item -> {
		ItemStack itemStack = item.getItem();
		return itemStack.getItem().isEdible() && item.isAlive() && !item.hasPickUpDelay();
	};
	public DynamicGameEventListener<AzureVibrationListener> dynamicGameEventListener;
	private AngerManagement angerManagement = new AngerManagement(this::canTargetEntity, Collections.emptyList());
	protected int attackProgress = 0;
	protected long searchingProgress = 0L;
	protected long searchingCooldown = 0L;
	public int breakingCounter = 0;
	public int pukingCounter = 0;
	public int screamingCounter = 0;
	public int newbornCounter = 0;

	protected BaseEntity(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
		setMaxUpStep(1.5f);
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	public static boolean canSpawn(EntityType<? extends PathfinderMob> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
		if (world.getDifficulty() == Difficulty.PEACEFUL)
			return false;
		if ((reason != MobSpawnType.CHUNK_GENERATION && reason != MobSpawnType.NATURAL))
			return world.getBlockState(pos.below()).is(BlockTags.DIRT);
		return world.getBlockState(pos.below()).is(BlockTags.DIRT);
	}

	@Override
	protected void tickDeath() {
		++this.deathTime;
		if (this.deathTime == 120)
			this.remove(Entity.RemovalReason.KILLED);
	}

	@Override
	public MobType getMobType() {
		return MobType.UNDEFINED;
	}

	@Override
	public float getGrowth() {
		return entityData.get(GROWTH);
	}

	public AttackType getCurrentAttackType() {
		return entityData.get(CURRENT_ATTACK_TYPE);
	}

	public void setCurrentAttackType(AttackType value) {
		entityData.set(CURRENT_ATTACK_TYPE, value);
	}

	@Override
	public void setGrowth(float growth) {
		entityData.set(GROWTH, growth);
	}

	public boolean isEating() {
		return this.entityData.get(EAT);
	}

	public void setEatingStatus(boolean birth) {
		this.entityData.set(EAT, Boolean.valueOf(birth));
	}

	public boolean isPuking() {
		return this.entityData.get(PUKE);
	}

	public void setPukingStatus(boolean puking) {
		this.entityData.set(PUKE, Boolean.valueOf(puking));
	}

	public boolean isScreaming() {
		return this.entityData.get(SCREAM);
	}

	public void setScreamingStatus(boolean screaming) {
		this.entityData.set(SCREAM, Boolean.valueOf(screaming));
	}

	public boolean isNewBorn() {
		return this.entityData.get(BIRTH);
	}

	public void setNewBornStatus(boolean birthing) {
		this.entityData.set(BIRTH, Boolean.valueOf(birthing));
	}

	public boolean isSearching() {
		return this.entityData.get(SEARCHING);
	}

	public void setSearchingStatus(boolean searching) {
		this.entityData.set(SEARCHING, Boolean.valueOf(searching));
	}

	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(GROWTH, 0.0f);
		entityData.define(CURRENT_ATTACK_TYPE, AttackType.NONE);
		this.entityData.define(CLIENT_ANGER_LEVEL, 0);
		entityData.define(EAT, false);
		entityData.define(PUKE, false);
		entityData.define(SCREAM, false);
		entityData.define(BIRTH, false);
		entityData.define(SEARCHING, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putFloat("growth", getGrowth());
		AzureVibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener()).resultOrPartial(LOGGER::error).ifPresent(tag -> nbt.put("listener", (Tag) tag));
		AngerManagement.codec(this::canTargetEntity).encodeStart(NbtOps.INSTANCE, this.angerManagement).resultOrPartial(LOGGER::error).ifPresent(tag -> nbt.put("anger", (Tag) tag));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		if (nbt.contains("growth"))
			setGrowth(nbt.getFloat("growth"));
		if (nbt.contains("anger")) {
			AngerManagement.codec(this::canTargetEntity).parse(new Dynamic<Tag>(NbtOps.INSTANCE, nbt.get("anger"))).resultOrPartial(LOGGER::error).ifPresent(angerManagement -> {
				this.angerManagement = angerManagement;
			});
			this.syncClientAngerLevel();
		}
		if (nbt.contains("listener", 10))
			AzureVibrationListener.codec(this).parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("listener"))).resultOrPartial(LOGGER::error).ifPresent(vibrationListener -> this.dynamicGameEventListener.updateListener((AzureVibrationListener) vibrationListener, this.level));
	}

	public int getClientAngerLevel() {
		return this.entityData.get(CLIENT_ANGER_LEVEL);
	}

	private void syncClientAngerLevel() {
		this.entityData.set(CLIENT_ANGER_LEVEL, this.getActiveAnger());
	}

	public AngerLevel getAngerLevel() {
		return AngerLevel.byAnger(this.getActiveAnger());
	}

	private int getActiveAnger() {
		return this.angerManagement.getActiveAnger(this.getTarget());
	}

	public void clearAnger(Entity entity) {
		this.angerManagement.clearAnger(entity);
	}

	@VisibleForTesting
	public AngerManagement getAngerManagement() {
		return this.angerManagement;
	}

	public Optional<LivingEntity> getEntityAngryAt() {
		if (this.getAngerLevel().isAngry())
			return this.angerManagement.getActiveEntity();
		return Optional.empty();
	}

	@Override
	protected void customServerAiStep() {
		var serverLevel = (ServerLevel) this.level;
		super.customServerAiStep();
		if (this.tickCount % 20 == 0) {
			this.angerManagement.tick(serverLevel, this::canTargetEntity);
			this.syncClientAngerLevel();
		}
	}

	@Override
	public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> biConsumer) {
		var level = this.level;
		if (level instanceof ServerLevel) {
			ServerLevel serverLevel = (ServerLevel) level;
			biConsumer.accept(this.dynamicGameEventListener, serverLevel);
		}
	}

	@Override
	public boolean canTriggerAvoidVibration() {
		return true;
	}

	@Contract(value = "null->false")
	public boolean canTargetEntity(@Nullable Entity entity) {
		if (!(entity instanceof LivingEntity))
			return false;
		var livingEntity = (LivingEntity) entity;
		if (this.level != entity.level)
			return false;
		if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))
			return false;
		if (this.isVehicle())
			return false;
		if (this.isAlliedTo(entity))
			return false;
		if (livingEntity.getMobType() == MobType.UNDEAD)
			return false;
		if (livingEntity.getType() == EntityType.ARMOR_STAND)
			return false;
		if (livingEntity.getType() == EntityType.WARDEN)
			return false;
		if (livingEntity instanceof Bat)
			return false;
		if (entity instanceof Marker)
			return false;
		if (entity instanceof AreaEffectCloud)
			return false;
		if (livingEntity.isInvulnerable())
			return false;
		if (livingEntity.isDeadOrDying())
			return false;
		if (!this.level.getWorldBorder().isWithinBounds(livingEntity.getBoundingBox()))
			return false;
		return true;
	}

	@Override
	public TagKey<GameEvent> getListenableEvents() {
		return GameEventTags.WARDEN_CAN_LISTEN;
	}

	@Override
	public boolean shouldListen(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, Context var5) {
		if (this.isNoAi() || this.isDeadOrDying() || !level.getWorldBorder().isWithinBounds(var3) || this.isRemoved())
			return false;
		var entity = var5.sourceEntity();
		return !(entity instanceof LivingEntity) || this.canTargetEntity((LivingEntity) entity);
	}

	@Override
	public void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, Entity var5, Entity var6, float var7) {
		if (this.isDeadOrDying())
			return;
		if (this.isVehicle())
			return;
		if (var6 instanceof LivingEntity livingEntity)
			if (!(livingEntity instanceof BaseEntity))
				BrainUtils.setMemory(this, MemoryModuleType.WALK_TARGET, new WalkTarget(var3, 1.5F, 0));
	}

	public void shootFlames(Entity target) {
		if (!this.level.isClientSide) {
			if (this.getTarget() != null) {
				var world = this.getCommandSenderWorld();
				var vector3d = this.getViewVector(1.0F);
				var x = target.getX() - (this.getX() + vector3d.x * 2);
				var y = target.getY(0.5) - (this.getY(0.75));
				var z = target.getZ() - (this.getZ() + vector3d.z * 2);
				var smallFireball = new SmallFireball(this.level, this, x, y, z);
				smallFireball.setPos(smallFireball.getX(), this.getY(0.5) + 0.5, smallFireball.getZ());
				world.addFreshEntity(smallFireball);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (!level.isClientSide && this.isAlive())
			grow(this, 1 * getGrowthMultiplier());

		var level = this.level;
		if (level instanceof ServerLevel) {
			var serverLevel = (ServerLevel) level;
			this.dynamicGameEventListener.getListener().tick(serverLevel);
		}

		// Searching Logic
		var velocityLength = this.getDeltaMovement().horizontalDistance();
		if (!this.isNewBorn() && !this.isDeadOrDying() && !this.isPuking() && !this.isScreaming() && (velocityLength == 0 && this.getDeltaMovement().horizontalDistance() == 0.0 && !this.isAggressive())) {
			searchingCooldown++;
			if (searchingCooldown == 10)
				this.setSearchingStatus(true);
			if (searchingCooldown >= 68) {
				searchingCooldown = -60;
				this.setSearchingStatus(false);
			}
		}
		if (this.isAggressive()) {
			searchingCooldown = -60;
			this.setSearchingStatus(false);
		}
	}

}
