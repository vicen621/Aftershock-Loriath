package mod.azure.aftershock.common.entities.american;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.aftershock.common.AftershockMod.ModSounds;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.entities.sensors.ItemEntitySensor;
import mod.azure.aftershock.common.entities.tasks.EatFoodTask;
import mod.azure.aftershock.common.entities.tasks.KillLightsTask;
import mod.azure.aftershock.common.entities.tasks.StrafeScreamTarget;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
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
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;

public class AmericanShreikerEntity extends BaseEntity implements SmartBrainOwner<AmericanShreikerEntity> {

	public AmericanShreikerEntity(EntityType<? extends BaseEntity> entityType, Level level) {
		super(entityType, level);
		// Sets exp drop amount
		this.xpReward = AftershockMod.config.americanshreiker_exp;
	}

	// Animation logic
	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 0, event -> {
			var moltTimer = this.getGrowth() <= 42000;
			var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying() && moltTimer;
			var isHurt = this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead && moltTimer;
			var isEating = (this.isEating() && !isDead && !this.isPuking() && moltTimer);
			var isPuking = (this.isPuking() && !isDead && !this.isEating() && moltTimer);
			var isSearching = this.isSearching() && !this.isEating() && !this.isPuking() && moltTimer;
			var isScreaming = (this.isScreaming() && !isDead && !this.isEating() && !this.isPuking() && moltTimer);
			var isNewBorn = (this.isNewBorn() && !isDead && !this.isEating() && !this.isPuking() && !this.isScreaming() && moltTimer);
			var isAttacking = getCurrentAttackType() != AttackType.NONE && attackProgress > 0 && moltTimer && !isDead;
			var isMolting = this.getGrowth() >= 42000 && !isDead;
			var movingNoArggo = event.isMoving() && !this.isAggressive() && !isHurt && moltTimer;
			var movingArggo = event.isMoving() && this.isAggressive() && !isHurt && moltTimer;
			if (isAttacking)
				return event.setAndContinue(RawAnimation.begin().then(AttackType.animationMappings.get(getCurrentAttackType()), LoopType.PLAY_ONCE));
			if (movingNoArggo)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			if (movingArggo)
				return event.setAndContinue(AftershockAnimationsDefault.RUNSCREAM);
			return event.setAndContinue(isDead ? AftershockAnimationsDefault.DEATH : isHurt ? AftershockAnimationsDefault.HURT : isEating ? AftershockAnimationsDefault.GRAB : isPuking ? AftershockAnimationsDefault.PUKE : isScreaming ? AftershockAnimationsDefault.SCREAM : isNewBorn ? AftershockAnimationsDefault.SPAWN : isSearching ? AftershockAnimationsDefault.LOOK : isMolting ? AftershockAnimationsDefault.MOLT : AftershockAnimationsDefault.IDLE);
		}).setSoundKeyframeHandler(event -> {
			if (event.getKeyframeData().getSound().matches("puking"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ALLAY_HURT, SoundSource.HOSTILE, 0.75F, 0.1F, true);
			if (event.getKeyframeData().getSound().matches("screaming"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.HUSK_HURT, SoundSource.HOSTILE, 1.25F, 0.3F, true);
			if (event.getKeyframeData().getSound().matches("looking"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), ModSounds.SHREIKER_SEARCH, SoundSource.HOSTILE, 1.25F, 1.0F, true);
			if (event.getKeyframeData().getSound().matches("dying"))
				if (this.level().isClientSide)
					this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), ModSounds.SHREIKER_HURT, SoundSource.HOSTILE, 1.25F, 1.0F, true);
		}));
	}

	// Brain logic
	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<AmericanShreikerEntity>> getSensors() {
		return ObjectArrayList.of(
				// Checks living targets it can see is a heat giving entity via the tag or entities on fire.
				new NearbyLivingEntitySensor<AmericanShreikerEntity>().setPredicate((target, entity) -> target.isAlive() && entity.hasLineOfSight(target) && (!(target instanceof BaseEntity || (target.getMobType() == MobType.UNDEAD && !target.isOnFire()) || target instanceof EnderMan || target instanceof Endermite || target instanceof Creeper || target instanceof AbstractGolem) || target.getType().is(AftershockMod.HEAT_ENTITY) || target.isOnFire())),
				// Checks for what last hurt it
				new HurtBySensor<>(),
				// Checks for food items/blocks
				new ItemEntitySensor<AmericanShreikerEntity>(),
				// Checks if target is unreachable
				new UnreachableTargetSensor<AmericanShreikerEntity>());
	}

	@Override
	public BrainActivityGroup<AmericanShreikerEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
				// Breaks lights as they are heat sources
				new KillLightsTask<>().stopIf(target -> this.isAggressive()),
				// Looks at Target
				new LookAtTarget<>(), new LookAtTargetSink(40, 300),
				// Strafes players, also handles making sure the entity screams
				new StrafeScreamTarget<>().startCondition(entity -> !this.isPuking() || !this.isScreaming()).cooldownFor(entity -> 600),
				// Walks or runs to Target
				new MoveToWalkTarget<>().startCondition(entity -> !this.isPuking()));
	}

	@Override
	public BrainActivityGroup<AmericanShreikerEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				// Eats food items/blocks
				new EatFoodTask<AmericanShreikerEntity>(20), new FirstApplicableBehaviour<AmericanShreikerEntity>(
						// Target or attack/ alerts other entities of this type in range of target.
						new TargetOrRetaliate<>().alertAlliesWhen((mob, entity) -> this.isScreaming()),
						// Chooses random look target
						new SetRandomLookTarget<>()),
				new OneRandomBehaviour<>(
						// Radius it will walk around in
						new SetRandomWalkTarget<>().setRadius(20).speedModifier(1.1f),
						// Idles the mob so it doesn't do anything
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(300, 600))));
	}

	@Override
	public BrainActivityGroup<AmericanShreikerEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				// Removes entity from being a target.
				new InvalidateAttackTarget<>().invalidateIf((target, entity) -> !target.isAlive() || !entity.hasLineOfSight(target)),
				// Moves to traget to attack
				new SetWalkTargetToAttackTarget<>().speedMod(1.25F).startCondition(entity -> !this.isPuking()),
				// Attacks the target if in range and is grown enough
				new AnimatableMeleeAttack<>(5).startCondition(entity -> this.getGrowth() >= 1200));
	}

	@Override
	protected void customServerAiStep() {
		// Tick the brain
		tickBrain(this);
		super.customServerAiStep();
	}

	// Mob stats
	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 25.0D).add(Attributes.MAX_HEALTH, AftershockMod.config.americanshreiker_health).add(Attributes.ATTACK_DAMAGE, AftershockMod.config.americanshreiker_damage).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_KNOCKBACK, 0.0D);
	}

	// Mob Navigation
	@Override
	protected PathNavigation createNavigation(Level world) {
		return new AzureNavigation(this, world);
	}

	// Growth logic
	@Override
	public float getMaxGrowth() {
		// Max growth before turning into an American Blaster
		return 48000;
	}

	@Override
	public LivingEntity growInto() {
		// Grow into American Blaster
		var entity = ModMobs.AMERICAN_BLASTER.create(level());
		if (hasCustomName())
			entity.setCustomName(this.getCustomName());
		entity.setNewBornStatus(true);
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
		var areaEffectCloudEntity = new AreaEffectCloud(this.level(), this.getX(), this.getY() + 1, this.getZ());
		areaEffectCloudEntity.setRadius(1.0F);
		areaEffectCloudEntity.setDuration(20);
		areaEffectCloudEntity.setParticle(ParticleTypes.POOF);
		areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
		entity.level().addFreshEntity(areaEffectCloudEntity);
		return entity;
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
		if (this.getGrowth() >= 42000)
			this.removeFreeWill();

		// Naturally spawned logic
		if (this.isNewBorn()) {
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
			this.xxa = this.xRotO;
			this.zza = 0.0F;
			this.yHeadRot = 0.0f;
		}

		// Screaming logic when attacking
		if (this.isScreaming())
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 7, 100, false, false));
		if (this.isScreaming() && !this.isDeadOrDying() && !this.isSearching()) {
			screamingCounter++;
			if (screamingCounter >= 28) {
				screamingCounter = 0;
				this.setScreamingStatus(false);
			}
		}

		// Naturally spawning birth logic
		if (this.isNewBorn() && !this.isDeadOrDying() && !this.isSearching() && !this.isPuking() && !this.isScreaming()) {
			newbornCounter++;
			if (newbornCounter >= 800) {
				newbornCounter = 0;
				this.setNewBornStatus(false);
			}
		}

		// Puking/giving birth logic
		if (this.getDeltaMovement().horizontalDistance() > 0.1)
			this.setEatingStatus(false);
		if (this.isPuking())
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
		if (this.isPuking() && !this.isDeadOrDying() && !this.isNewBorn() && !this.isScreaming()) {
			pukingCounter++;
			this.setSearchingStatus(false);
			this.setEatingStatus(false);
			if (this.pukingCounter >= 120) {
				this.setPukingStatus(false);
				this.playSound(SoundEvents.DONKEY_EAT, 1.0f, 1.0f);
				var entity = new AmericanShreikerEntity(ModMobs.AMERICAN_SHREIKER, this.level());
				entity.moveTo(this.blockPosition(), this.getYRot(), this.getXRot());
				this.level().addFreshEntity(entity);
				entity.setNewBornStatus(true);
				entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
				var areaEffectCloudEntity = new AreaEffectCloud(this.level(), this.getX(), this.getY() + 1, this.getZ());
				areaEffectCloudEntity.setRadius(0.5F);
				areaEffectCloudEntity.setDuration(10);
				areaEffectCloudEntity.setParticle(ParticleTypes.POOF);
				areaEffectCloudEntity.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
				entity.level().addFreshEntity(areaEffectCloudEntity);
				pukingCounter = 0;
			}
		}

		// Attack animation logic
		if (attackProgress > 0) {
			attackProgress--;
			if (!level().isClientSide && attackProgress <= 0)
				setCurrentAttackType(AttackType.NONE);
		}
		if (attackProgress == 0 && swinging)
			attackProgress = 10;
		if (!level().isClientSide && getCurrentAttackType() == AttackType.NONE)
			setCurrentAttackType(switch (random.nextInt(5)) {
			case 0 -> AttackType.NORMAL;
			case 1 -> AttackType.BITE;
			case 2 -> AttackType.NORMAL;
			case 3 -> AttackType.BITE;
			default -> AttackType.NORMAL;
			});

		// Block breaking logic
		if (!this.isDeadOrDying() && this.isAggressive() && !this.isInWater() && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) == true) {
			breakingCounter++;
			if (breakingCounter > 10)
				for (BlockPos testPos : BlockPos.betweenClosed(blockPosition().above().relative(getDirection()), blockPosition().relative(getDirection()).above(1))) {
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
			if (searchingCooldown == 10)
				this.setSearchingStatus(true);
			if (searchingCooldown >= 68) {
				searchingCooldown = -60;
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
