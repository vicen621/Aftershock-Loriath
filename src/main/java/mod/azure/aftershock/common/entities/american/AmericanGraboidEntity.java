package mod.azure.aftershock.common.entities.american;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.entities.base.SoundTrackingEntity;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.helper.AzureVibrationListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
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

public class AmericanGraboidEntity extends SoundTrackingEntity implements SmartBrainOwner<AmericanGraboidEntity> {

	public AmericanGraboidEntity(EntityType<? extends BaseEntity> entityType, Level level) {
		super(entityType, level);
		this.noCulling = true;
		// Registers sound listening settings
		this.dynamicGameEventListener = new DynamicGameEventListener<AzureVibrationListener>(new AzureVibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 64, this));
		// Sets exp drop amount
		this.xpReward = AfterShocksConfig.americangraboid_exp;
	}

	// Animation logic
	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 5, event -> {
			var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
			if (getCurrentAttackType() != AttackType.NONE && attackProgress > 0 && !isDead)
				return event.setAndContinue(RawAnimation.begin().then(AttackType.animationMappings.get(getCurrentAttackType()), LoopType.PLAY_ONCE));
			if (event.isMoving() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			if (isDead)
				return event.setAndContinue(AftershockAnimationsDefault.DEATH);
			return event.setAndContinue(this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead ? AftershockAnimationsDefault.HURT : AftershockAnimationsDefault.IDLE);
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
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(300, 600))));
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				// Removes entity from being a target.
				new InvalidateAttackTarget<>().invalidateIf((entity, target) -> !target.isAlive() || target instanceof Player && ((Player) target).isCreative()),
				// Moves to traget to attack
				new SetWalkTargetToAttackTarget<>().speedMod(1.5F),
				// Attacks the target if in range and is grown enough
				new AnimatableMeleeAttack<>(10));
	}

	@Override
	protected void customServerAiStep() {
		// Tick the brain
		tickBrain(this);
		super.customServerAiStep();
	}

	// Mob stats
	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 25.0D).add(Attributes.MAX_HEALTH, AfterShocksConfig.americangraboid_health).add(Attributes.ATTACK_DAMAGE, AfterShocksConfig.americangraboid_damage).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_KNOCKBACK, 0.0D);
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
		var world = entity.level;
		if (!world.isClientSide() && !this.isAlbino()) {
			var newEntity = growInto();
			var newEntity1 = growInto();
			var newEntity2 = growInto();
			if (newEntity == null || newEntity1 == null || newEntity2 == null)
				return;
			newEntity.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
			newEntity1.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
			newEntity2.moveTo(entity.blockPosition(), entity.getYRot(), entity.getXRot());
			world.addFreshEntity(newEntity);
			world.addFreshEntity(newEntity1);
			world.addFreshEntity(newEntity2);
			entity.remove(Entity.RemovalReason.DISCARDED);
		}
	}

	@Override
	public LivingEntity growInto() {
		// Grow into 3 American Shreikers
		var entity = ModMobs.AMERICAN_SHREIKER.create(level);
		if (hasCustomName())
			entity.setCustomName(this.getCustomName());
		entity.setNewBornStatus(true);
		entity.setGrowth(0);
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
		var areaEffectCloudEntity = new AreaEffectCloud(this.level, this.getX(), this.getY() + 1, this.getZ());
		areaEffectCloudEntity.setRadius(1.0F);
		areaEffectCloudEntity.setDuration(20);
		areaEffectCloudEntity.setParticle(ParticleTypes.POOF);
		areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
		entity.level.addFreshEntity(areaEffectCloudEntity);
		return this.isAlbino() ? null : entity;
	}

	@Override
	protected AABB makeBoundingBox() {
		return super.makeBoundingBox();
	}

	@Override
	public AABB getLocalBoundsForPose(Pose pose) {
		return this.getBoundingBox();
	}

	@Override
	public void travel(Vec3 vec3) {
		super.travel(vec3);
		if (this.tickCount % 10 == 0)
			this.refreshDimensions();
	}

	@Override
	public int getArmorValue() {
		return AfterShocksConfig.americangraboid_armor;
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

	// Mob logic done each tick
	@Override
	public void tick() {
		super.tick();

		// Turning into Blaster logic
		if (this.getGrowth() >= 336000)
			this.removeFreeWill();

		// Block breaking logic
		if (!this.isDeadOrDying() && this.isAggressive() && !this.isInWater() && this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) == true) {
			breakingCounter++;
			if (breakingCounter > 10)
				for (BlockPos testPos : BlockPos.betweenClosed(blockPosition().above().relative(getDirection()), blockPosition().relative(getDirection()).above(1))) {
					if (level.getBlockState(testPos).is(AftershockMod.WEAK_BLOCKS) && !level.getBlockState(testPos).isAir()) {
						if (!level.isClientSide)
							this.level.removeBlock(testPos, false);
						if (this.swingingArm != null)
							this.swing(swingingArm);
						breakingCounter = -90;
						if (level.isClientSide())
							this.playSound(SoundEvents.ARMOR_STAND_BREAK, 0.2f + random.nextFloat() * 0.2f, 0.9f + random.nextFloat() * 0.15f);
					}
				}
			if (breakingCounter >= 25)
				breakingCounter = 0;
		}

		// Attack animation logic
		if (attackProgress > 0) {
			attackProgress--;
			if (!level.isClientSide && attackProgress <= 0)
				setCurrentAttackType(AttackType.NONE);
		}
		if (attackProgress == 0 && swinging)
			attackProgress = 10;
		if (!level.isClientSide && getCurrentAttackType() == AttackType.NONE)
			setCurrentAttackType(switch (random.nextInt(2)) {
			case 0 -> AttackType.BITE;
			default -> AttackType.BITE;
			});
	}

}
