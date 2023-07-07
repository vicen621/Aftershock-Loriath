package mod.azure.aftershock.common.entities.american;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.aftershock.common.AftershockMod.ModSounds;
import mod.azure.aftershock.common.entities.base.AfterShockVibrationUser;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.entities.base.SoundTrackingEntity;
import mod.azure.aftershock.common.entities.tasks.GraboidAttackTask;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;

public class AmericanGraboidEntity extends SoundTrackingEntity implements SmartBrainOwner<AmericanGraboidEntity> {

	public AmericanGraboidEntity(EntityType<? extends BaseEntity> entityType, Level level) {
		super(entityType, level);
		// Stops it from culling/derendering when it's moving off screen, needed due to size.
		this.noCulling = true;
		// Sets the speed and range of vibrations
		this.vibrationUser = new AfterShockVibrationUser(this, 1.5F, 48);
		// Sets exp drop amount
		this.xpReward = AftershockMod.config.americangraboid_exp;
	}

	// Animation logic
	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 5, event -> {
			var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
			var isSearching = this.isSearching() && !this.isEating() && !this.isPuking();
			// Play animation attacking
			if (event.getAnimatable().getAttckingState() == 2 && !isDead)
				return event.setAndContinue(RawAnimation.begin().then("bite", LoopType.HOLD_ON_LAST_FRAME));
			// Play animation attacking
			if (event.getAnimatable().getAttckingState() == 5 && !isDead)
				return event.setAndContinue(RawAnimation.begin().then("digin", LoopType.HOLD_ON_LAST_FRAME));
			// Play animation when moving
			if (event.isMoving() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			// Play animation when dead
			if (isDead)
				return event.setAndContinue(AftershockAnimationsDefault.DEATH);
			return event.setAndContinue(isSearching ? AftershockAnimationsDefault.DIGOUT : this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead ? AftershockAnimationsDefault.HURT : AftershockAnimationsDefault.IDLE);
		}).setSoundKeyframeHandler(event -> {
			if (event.getKeyframeData().getSound().matches("attacking"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), ModSounds.GRABOID_ATTACK, SoundSource.HOSTILE, 1.25F, 1.0F, true);
			if (event.getKeyframeData().getSound().matches("dying"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), ModSounds.GRABOID_DYING, SoundSource.HOSTILE, 1.25F, 1.0F, true);
			if (event.getKeyframeData().getSound().matches("digging"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.RESPAWN_ANCHOR_SET_SPAWN, SoundSource.HOSTILE, 1.25F, 1.0F, true);
		}));
	}

	// Brain logic
	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<AmericanGraboidEntity>> getSensors() {
		return ObjectArrayList.of(
				// Checks living targets it can see is a heat giving entity via the tag or entities on fire.
				new NearbyLivingEntitySensor<AmericanGraboidEntity>().setPredicate((target, entity) -> target.isAlive() && entity.hasLineOfSight(target) && (!(target instanceof BaseEntity || (target.getMobType() == MobType.UNDEAD && !target.isOnFire()) || target instanceof EnderMan || target instanceof Endermite || target instanceof Creeper || target instanceof AbstractGolem) || target.getType().is(AftershockMod.HEAT_ENTITY) || target.isOnFire())),
				// Checks for what last hurt it
				new HurtBySensor<>(),
				// Checks if target is unreachable
				new UnreachableTargetSensor<AmericanGraboidEntity>());
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
				// Looks at Target
				new LookAtTarget<>(), new LookAtTargetSink(40, 300),
				// Walks or runs to Target
				new MoveToWalkTarget<>());
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(new FirstApplicableBehaviour<AmericanGraboidEntity>(
				// Target or attack/ alerts other entities of this type in range of target.
				new TargetOrRetaliate<>(),
				// Chooses random look target
				new SetRandomLookTarget<>()),
				new OneRandomBehaviour<>(
						// Radius it will walk around in
						new SetRandomWalkTarget<>().setRadius(20).speedModifier(1.1f),
						// Idles the mob so it doesn't do anything
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(30, 60))));
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				// Removes entity from being a target.
				new InvalidateAttackTarget<>().invalidateIf((target, entity) -> !target.isAlive() || !entity.hasLineOfSight(target)),
				// Moves to traget to attack
				new SetWalkTargetToAttackTarget<>().speedMod(1.3F),
				// Attacks the target if in range and is grown enough
				new GraboidAttackTask<>(25));
	}

	@Override
	protected void customServerAiStep() {
		// Tick the brain
		tickBrain(this);
		super.customServerAiStep();
	}

	// Mob stats
	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 25.0D).add(Attributes.MAX_HEALTH, AftershockMod.config.americangraboid_health).add(Attributes.ATTACK_DAMAGE, AftershockMod.config.americangraboid_damage).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_KNOCKBACK, 0.0D);
	}

	// Mob Navigation
	@Override
	protected PathNavigation createNavigation(Level world) {
		return new AzureNavigation(this, world);
	}

	// Growth logic
	@Override
	public float getMaxGrowth() {
		return 336000 + 6000;
	}

	@Override
	public void growUp(LivingEntity entity) {
		var world = entity.level();
		if (!world.isClientSide() && !this.isAlbino()) {
			var newEntity = growInto();
			var newEntity1 = growInto();
			var newEntity2 = growInto();
			if (newEntity == null || newEntity1 == null || newEntity2 == null)
				return;
			newEntity.moveTo(entity.blockPosition().east(), entity.getYRot(), entity.getXRot());
			newEntity1.moveTo(entity.blockPosition().west(), entity.getYRot(), entity.getXRot());
			newEntity2.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
			world.addFreshEntity(newEntity);
			world.addFreshEntity(newEntity1);
			world.addFreshEntity(newEntity2);
			entity.kill();
		}
	}

	@Override
	public LivingEntity growInto() {
		// Grow into 3 American Shreikers
		var entity = ModMobs.AMERICAN_SHREIKER.create(level());
		if (hasCustomName())
			entity.setCustomName(this.getCustomName());
		entity.setNewBornStatus(true);
		entity.setGrowth(0);
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
		var areaEffectCloudEntity = new AreaEffectCloud(this.level(), this.getX(), this.getY() + 1, this.getZ());
		areaEffectCloudEntity.setRadius(1.0F);
		areaEffectCloudEntity.setDuration(20);
		areaEffectCloudEntity.setParticle(ParticleTypes.POOF);
		areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
		entity.level().addFreshEntity(areaEffectCloudEntity);
		return this.isAlbino() ? null : entity;
	}

	@Override
	protected AABB makeBoundingBox() {
		return super.makeBoundingBox();
	}

	@Override
	public AABB getLocalBoundsForPose(Pose pose) {
		return this.getBoundingBox().deflate(1.2);
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return EntityDimensions.scalable(2.0f, 1.8f);
	}

	@Override
	public void travel(Vec3 vec3) {
		if (this.tickCount % 10 == 0)
			this.refreshDimensions();
		super.travel(vec3);
	}

	@Override
	public int getArmorValue() {
		return AftershockMod.config.americangraboid_armor;
	}

	/**
	 * Prevents entity collisions from moving the egg.
	 */
	@Override
	public void doPush(Entity entity) {
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	/**
	 * Prevents the egg from being pushed.
	 */
	@Override
	public boolean isPushable() {
		return false;
	}

	/**
	 * Prevents fluids from moving the egg.
	 */
	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	// Checks if should be removed when far way.
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	// Checks if it should spawn as an adult
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType spawnReason, SpawnGroupData entityData, CompoundTag entityNbt) {
		// Spawn grown if used with summon command or egg.
		if (spawnReason == MobSpawnType.COMMAND || spawnReason == MobSpawnType.SPAWN_EGG)
			setGrowth(1250);
		return super.finalizeSpawn(world, difficulty, spawnReason, entityData, entityNbt);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return ModSounds.GRABOID_IDLE;
	}
	
	@Override
	protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(ModSounds.GRABOID_MOVING, 1.0F * 0.15f, 1.0F);
	}

//	@Override
//	protected SoundEvent getHurtSound(DamageSource source) {
//		return ModSounds.ABBERATION_HURT;
//	}

	// Mob logic done each tick
	@Override
	public void tick() {
		var pos = BlockPos.containing(this.getX(), this.getSurface((int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ())), this.getZ()).below();
		super.tick();

		if (this.getAttckingState() == 5) {
			this.breakingCounter++;
			this.level().addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getSurface((int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ())) - 0.35F, this.getZ(), 0, 0, 0);
		}
		if (this.breakingCounter >= 30)
			this.setAttackingState(0);

		if (this.getAttckingState() == 2)
			this.attackProgress++;
		if (this.attackProgress >= 50) {
			this.setAttackingState(5);
			this.attackProgress = 0;
		}
		if (this.getAttckingState() == 0) {
			this.attackProgress = 0;
			this.breakingCounter = 0;
		}

		// Adds particle effect to surface when moving so you can track it
		if (level().getBlockState(pos).isSolidRender(level(), pos) && !this.isDeadOrDying() && this.isInSand())
			if (level().isClientSide && this.getDeltaMovement().horizontalDistance() != 0.0) {
				this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, level().getBlockState(pos)), this.getX(), this.getSurface((int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ())) + 0.5F, this.getZ(), this.random.nextGaussian() * 0.02D, this.random.nextGaussian() * 0.02D, this.random.nextGaussian() * 0.02D);
				this.level().addParticle(ParticleTypes.POOF, this.getX(), this.getSurface((int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ())) - 0.35F, this.getZ(), 0, 0, 0);
			}
		// Sets the Graboid in the blocks below if it they match the tag checks and only if it's not in the part of life it's not beached to give birth
		this.setInSand(this.getGrowth() < 336000 && ((this.level().getBlockState(pos).is(BlockTags.SAND) || this.level().getBlockState(pos.below()).is(BlockTags.SAND)) || (this.level().getBlockState(pos).is(BlockTags.DIRT) || this.level().getBlockState(pos.below()).is(BlockTags.DIRT))) && this.deathTime < 5);

		// Turning into Blaster logic
		if (this.getGrowth() >= 336000)
			this.removeFreeWill();

		// Block breaking logic
		if (!this.isDeadOrDying() && this.isAggressive() && !this.isInWater() && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) == true) {
			breakingCounter++;
			if (breakingCounter > 10)
				for (var testPos : BlockPos.betweenClosed(blockPosition().above().relative(getDirection()), blockPosition().relative(getDirection()).above(1))) {
					if (level().getBlockState(testPos).is(AftershockMod.WEAK_BLOCKS) && !level().getBlockState(testPos).isAir()) {
						if (!level().isClientSide)
							this.level().removeBlock(testPos, false);
						if (this.swingingArm != null)
							this.swing(swingingArm);
						breakingCounter = -90;
						if (level().isClientSide())
							this.playSound(SoundEvents.ARMOR_STAND_BREAK, 0.2f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.15f);
					}
				}
			if (breakingCounter >= 25)
				breakingCounter = 0;
		}

		// Searching Logic
		var velocityLength = this.getDeltaMovement().horizontalDistance();
		if (!this.isDeadOrDying() && !this.isNewBorn() && !this.isDeadOrDying() && !this.isPuking() && !this.isScreaming() && (velocityLength == 0 && this.getDeltaMovement().horizontalDistance() == 0.0 && !this.isAggressive())) {
			searchingCooldown++;
			if (searchingCooldown == 50) {
				this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 100, false, false));
				this.getNavigation().stop();
				this.setSearchingStatus(true);
			}
			if (searchingCooldown >= 208) {
				searchingCooldown = -600;
				this.setAttackingState(5);
				this.setSearchingStatus(false);
			}
		}
		if (this.isDeadOrDying()) {
			this.setSearchingStatus(false);
			this.searchingCooldown = 0;
			this.breakingCounter = 0;
		}
	}

}
