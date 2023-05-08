package mod.azure.aftershock.common.entities.american;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.AftershockMod.ModBlocks;
import mod.azure.aftershock.common.blocks.GraboidEggBlock;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.entities.sensors.ItemEntitySensor;
import mod.azure.aftershock.common.entities.tasks.EatFoodTask;
import mod.azure.aftershock.common.entities.tasks.KillLightsTask;
import mod.azure.aftershock.common.entities.tasks.ShootFireTask;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.helper.AzureVibrationListener;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
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

public class AmericanBlasterEntity extends BaseEntity implements SmartBrainOwner<AmericanBlasterEntity> {

	public int layEggCounter;
	public int eatingCounter;
	public int passoutCounter;
	public int wakeupCounter;
	public static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(AmericanBlasterEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> PASSED_OUT = SynchedEntityData.defineId(AmericanBlasterEntity.class, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Boolean> WAKING_UP = SynchedEntityData.defineId(AmericanBlasterEntity.class, EntityDataSerializers.BOOLEAN);

	public AmericanBlasterEntity(EntityType<? extends BaseEntity> entityType, Level level) {
		super(entityType, level);
		// Registers sound listening settings
		this.dynamicGameEventListener = new DynamicGameEventListener<AzureVibrationListener>(new AzureVibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 15, this));
		// Sets exp drop amount
		this.xpReward = AfterShocksConfig.americanblaster_exp;
//		moveControl = this.isOnGround() ? new MoveControl(this) : new BlasterFlyControl(this);
	}

	// Animation logic
	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 0, event -> {
			var isDead = event.getAnimatable().dead || event.getAnimatable().getHealth() < 0.01 || event.getAnimatable().isDeadOrDying();
			var isNewBorn = (event.getAnimatable().isNewBorn() && !isDead && !this.isEating() && !event.getAnimatable().isScreaming() && !event.getAnimatable().isPassedOut() && !event.getAnimatable().isWakingUp());
			var isSearching = event.getAnimatable().isSearching() && !event.getAnimatable().isEating() && !event.getAnimatable().isPassedOut() && !event.getAnimatable().isWakingUp();
			var isScreaming = (event.getAnimatable().getAttckingState() == 1 && !isDead && !event.getAnimatable().isEating() && !event.getAnimatable().isPuking() && !event.getAnimatable().isPassedOut() && !event.getAnimatable().isWakingUp());
			var isPassedout = (event.getAnimatable().isPassedOut() && !event.getAnimatable().isWakingUp() && !isDead && !event.getAnimatable().isEating() && !event.getAnimatable().isPuking());
			var isWakingup = (event.getAnimatable().isWakingUp() && !event.getAnimatable().isPassedOut() && !isDead && !event.getAnimatable().isEating() && !event.getAnimatable().isPuking());
			var isAttacking = getCurrentAttackType() != AttackType.NONE && attackProgress > 0 && !isDead;
			var movingArggo = event.isMoving() && event.getAnimatable().isAggressive();
			if (isAttacking)
				return event.setAndContinue(RawAnimation.begin().then(AttackType.animationMappings.get(getCurrentAttackType()), LoopType.PLAY_ONCE));
			if (event.isMoving() && !this.isAggressive() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			if (movingArggo && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.RUN);
			return event.setAndContinue(this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead ? AftershockAnimationsDefault.HURT : isSearching ? AftershockAnimationsDefault.LOOK : isPassedout ? AftershockAnimationsDefault.PASSOUT : isWakingup ? AftershockAnimationsDefault.WAKEUP : isNewBorn ? AftershockAnimationsDefault.BIRTH : isScreaming ? AftershockAnimationsDefault.BLOW_TORCH : isDead ? AftershockAnimationsDefault.DEATH : AftershockAnimationsDefault.IDLE);
		}).setSoundKeyframeHandler(event -> {
			if (event.getKeyframeData().getSound().matches("fire"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.75F, 1.0F, true);
			if (event.getKeyframeData().getSound().matches("screaming"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.HUSK_HURT, SoundSource.HOSTILE, 1.25F, 0.5F, true);
			if (event.getKeyframeData().getSound().matches("looking"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.CHICKEN_AMBIENT, SoundSource.HOSTILE, 1.25F, 0.1F, true);
			if (event.getKeyframeData().getSound().matches("fall"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SMALL_FALL, SoundSource.HOSTILE, 1.25F, 0.1F, true);
			if (event.getKeyframeData().getSound().matches("dying"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.LLAMA_DEATH, SoundSource.HOSTILE, 1.25F, 0.1F, true);
		}));
	}

	// Brain logic
	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<AmericanBlasterEntity>> getSensors() {
		return ObjectArrayList.of(
				// Checks living targets it can see is a heat giving entity via the tag or entities on fire.
				new NearbyLivingEntitySensor<AmericanBlasterEntity>().setPredicate((target, entity) -> target.isAlive() && entity.hasLineOfSight(target) && (!(target instanceof BaseEntity || (target.getMobType() == MobType.UNDEAD && !target.isOnFire()) || target instanceof EnderMan || target instanceof Endermite || target instanceof Creeper || target instanceof AbstractGolem) || target.getType().is(AftershockMod.HEAT_ENTITY) || target.isOnFire())),
				// Checks for what last hurt it
				new HurtBySensor<>(),
				// Checks for food items/blocks
				new ItemEntitySensor<AmericanBlasterEntity>(),
				// Checks if target is unreachable
				new UnreachableTargetSensor<AmericanBlasterEntity>());
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(
				// Breaks lights as they are heat sources
				new KillLightsTask<>().stopIf(target -> this.isAggressive()).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()),
				// Looks at Target
				new LookAtTarget<>().startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()), new LookAtTargetSink(40, 300),
				// Walks or runs to Target
				new MoveToWalkTarget<>().startCondition(entity -> !this.isPuking() || this.getAttckingState() == 0).stopIf(entity -> this.getAttckingState() == 1));
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				// Eats food items/blocks
				new EatFoodTask<AmericanBlasterEntity>(0), new FirstApplicableBehaviour<AmericanBlasterEntity>(
						// Target or attack/ alerts other entities of this type in range of target.
						new TargetOrRetaliate<>().alertAlliesWhen((mob, entity) -> this.isScreaming()).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()),
						// Chooses random look target
						new SetRandomLookTarget<>().startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp())),
				new OneRandomBehaviour<>(
						// Radius it will walk around in
						new SetRandomWalkTarget<>().setRadius(20).speedModifier(1.1f).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()),
						// Idles the mob so it doesn't do anything
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(300, 600)).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp())));
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				// Removes entity from being a target.
				new InvalidateAttackTarget<>().invalidateIf((target, entity) -> !target.isAlive() || !entity.hasLineOfSight(target) || this.isWakingUp() || this.isPassedOut()),
				// Moves to traget to attack
				new SetWalkTargetToAttackTarget<>().speedMod(1.5F).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()),
				// Attacks the target if in range and is grown enough
				new AnimatableMeleeAttack<>(5).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()),
				// Shoots fire at target, currently uses small fire. To be redone before first release.
				new ShootFireTask<>(20).cooldownFor(entity -> 200).startCondition(entity -> !this.isPassedOut() || !this.isWakingUp()).stopIf(entity -> this.isPassedOut() || this.isWakingUp()));
	}

	@Override
	protected void customServerAiStep() {
		// Tick the brain
		tickBrain(this);
		super.customServerAiStep();
	}

	// Mob stats
	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 25.0D).add(Attributes.MAX_HEALTH, AfterShocksConfig.americanblaster_health).add(Attributes.ATTACK_DAMAGE, AfterShocksConfig.americanblaster_damage).add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_KNOCKBACK, 0.0D);
	}

	// Mob Navigation
	@Override
	protected PathNavigation createNavigation(Level worldIn) {
		final var flyingpathnavigator = new FlyingPathNavigation(this, worldIn);
		flyingpathnavigator.setCanOpenDoors(false);
		flyingpathnavigator.setCanFloat(true);
		flyingpathnavigator.setCanPassDoors(true);
//		return this.isOnGround() ? new AzureNavigation(this, worldIn) : flyingpathnavigator;
		return new AzureNavigation(this, worldIn);
	}

	public boolean causeFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	@Override
	protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
	}

	// Growth logic
	@Override
	public float getMaxGrowth() {
		return 1200;
	}

	@Override
	public LivingEntity growInto() {
		// Grows into nothing, final life stage.
		return null;
	}

	// Checks if should be removed when far way.
	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	// Data Saving
	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_IGNITED, false);
		this.entityData.define(PASSED_OUT, false);
		this.entityData.define(WAKING_UP, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("ignited", this.isIgnited());
		compoundTag.putBoolean("passedout", this.isPassedOut());
		compoundTag.putBoolean("wakingup", this.isWakingUp());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.getBoolean("ignited"))
			this.ignite();
	}

	public void setWakingUpStatus(boolean passout) {
		this.entityData.set(WAKING_UP, Boolean.valueOf(passout));
	}

	public boolean isWakingUp() {
		return this.entityData.get(WAKING_UP);
	}

	public void setPassedOutStatus(boolean passout) {
		this.entityData.set(PASSED_OUT, Boolean.valueOf(passout));
	}

	public boolean isPassedOut() {
		return this.entityData.get(PASSED_OUT);
	}

	public boolean isIgnited() {
		return this.entityData.get(DATA_IS_IGNITED);
	}

	public void ignite() {
		this.entityData.set(DATA_IS_IGNITED, true);
	}

	// Exploding
	private void explodeBlaster() {
		// Handles exploding the mob and killing it.
		if (!this.level.isClientSide) {
			this.dead = true;
			this.level.explode(this, this.getX(), this.getY(), this.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
			this.discard();
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player2, InteractionHand interactionHand) {
		var itemStack = player2.getItemInHand(interactionHand);
		// Checks if item used on entity can cause it to explode. Uses the creeper igniters tag
		if (itemStack.is(ItemTags.CREEPER_IGNITERS)) {
			var soundEvent = itemStack.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
			this.level.playSound(player2, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0f, this.random.nextFloat() * 0.4f + 0.8f);
			if (!this.level.isClientSide) {
				this.ignite();
				if (!itemStack.isDamageableItem())
					itemStack.shrink(1);
				else
					itemStack.hurtAndBreak(1, player2, player -> player.broadcastBreakEvent(interactionHand));
			}
			return InteractionResult.sidedSuccess(this.level.isClientSide);
		}
		return super.mobInteract(player2, interactionHand);
	}

	@Override
	public void onSignalReceive(ServerLevel var1, GameEventListener var2, BlockPos var3, GameEvent var4, Entity var5, Entity var6, float var7) {
		return;
	}

	// Mob logic done each tick
	@Override
	public void tick() {
		super.tick();

		// Attack animation logic
		if (attackProgress > 0) {
			attackProgress--;
			if (!level.isClientSide && attackProgress <= 0)
				setCurrentAttackType(AttackType.NONE);
		}
		if (attackProgress == 0 && swinging)
			attackProgress = 10;
		if (!level.isClientSide && getCurrentAttackType() == AttackType.NONE)
			setCurrentAttackType(switch (random.nextInt(5)) {
			case 0 -> AttackType.NORMAL;
			case 1 -> AttackType.BITE;
			case 2 -> AttackType.NORMAL;
			case 3 -> AttackType.BITE;
			default -> AttackType.NORMAL;
			});

		// Naturally spawned logic
		if (this.isNewBorn() && !this.isDeadOrDying() && !this.isSearching() && !this.isScreaming()) {
			newbornCounter++;
			if (newbornCounter >= 60) {
				newbornCounter = 0;
				this.setNewBornStatus(false);
			}
		}
		if (this.isNewBorn())
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));

		// Egg laying logic
		if (this.isAlive() && !this.isAggressive() && !this.isPassedOut() && !this.isWakingUp())
			layEggCounter++;
		if (layEggCounter >= 2000) {
			this.level.playSound(null, this.blockPosition(), SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.1f, 0.9f + this.level.random.nextFloat() * 0.2f);
			var blockState = (BlockState) ModBlocks.GRABOID_EGG.defaultBlockState().setValue(GraboidEggBlock.EGGS, this.random.nextInt(4) + 1);
			this.level.setBlock(this.blockPosition(), blockState, 3);
			this.level.gameEvent(GameEvent.BLOCK_PLACE, this.blockPosition(), GameEvent.Context.of(this, blockState));
			this.layEggCounter = 0;
		}

		// Explode logic
		if (this.isAlive())
			if (this.isIgnited() || this.isOnFire()) {
				this.playSound(SoundEvents.TNT_PRIMED, 1.0f, 0.5f);
				this.gameEvent(GameEvent.PRIME_FUSE);
				this.explodeBlaster();
			}

		// Passing and waking up logic
		if (this.eatingCounter >= 3 && !this.isWakingUp()) {
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 100, false, false));
			this.setAggressive(false);
			this.setPassedOutStatus(true);
			this.passoutCounter++;
		}
		if (this.passoutCounter >= 600) {
			this.passoutCounter = -60;
			this.setPassedOutStatus(false);
			this.setWakingUpStatus(true);
			this.setAggressive(false);
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 100, false, false));
		}
		if (this.isWakingUp()) {
			this.wakeupCounter++;
			this.setAggressive(false);
		}
		if (this.wakeupCounter >= 100) {
			this.wakeupCounter = 0;
			this.eatingCounter = 0;
			this.setWakingUpStatus(false);
		}
		if (this.isPassedOut() || this.isWakingUp()) {
			this.zza = 0.0F;
			this.yHeadRot = 0.0f;
		}

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

		// Searching Logic
		var velocityLength = this.getDeltaMovement().horizontalDistance();
		if (!this.isDeadOrDying() && !this.isPassedOut() && !this.isWakingUp() && !this.isEating() && !this.isNewBorn() && !this.isDeadOrDying() && !this.isPuking() && !this.isScreaming() && (velocityLength == 0 && this.getDeltaMovement().horizontalDistance() == 0.0 && !this.isAggressive())) {
			searchingCooldown++;
			if (searchingCooldown == 10)
				this.setSearchingStatus(true);
			if (searchingCooldown >= 68) {
				searchingCooldown = -200;
				this.setSearchingStatus(false);
			}
		}
	}
}
