package mod.azure.aftershock.common.entities;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.entities.sensors.ItemEntitySensor;
import mod.azure.aftershock.common.entities.tasks.EatFoodTask;
import mod.azure.aftershock.common.entities.tasks.KillLightsTask;
import mod.azure.aftershock.common.entities.tasks.ShootFireTask;
import mod.azure.aftershock.common.entities.tasks.StrafeScreamTarget;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.helper.AzureVibrationListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
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
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;

public class AmericanBlasterEntity extends BaseEntity implements SmartBrainOwner<AmericanBlasterEntity> {

	public AmericanBlasterEntity(EntityType<? extends BaseEntity> entityType, Level level) {
		super(entityType, level);
		this.dynamicGameEventListener = new DynamicGameEventListener<AzureVibrationListener>(
				new AzureVibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 15, this));
		this.xpReward = AfterShocksConfig.americanblaster_exp;
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 5, event -> {
			var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
			var isNewBorn = (this.entityData.get(BIRTH) == true && !isDead && !this.isEating() && !this.isScreaming());
			var isSearching = this.entityData.get(SEARCHING) == true && !this.isEating();
			var isScreaming = (this.entityData.get(SCREAM) == true && !isDead && !this.isEating() && !this.isPuking());
			if (getCurrentAttackType() != AttackType.NONE && attackProgress > 0 && !isDead)
				return event.setAndContinue(RawAnimation.begin()
						.then(AttackType.animationMappings.get(getCurrentAttackType()), LoopType.PLAY_ONCE));
			if (event.isMoving() && !this.isAggressive() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			if (event.isMoving() && this.isAggressive() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.RUN);
			return event.setAndContinue(this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead
					? AftershockAnimationsDefault.HURT
					: isSearching ? AftershockAnimationsDefault.LOOK
							: isNewBorn ? AftershockAnimationsDefault.BIRTH
									: isScreaming ? AftershockAnimationsDefault.BLOW_TORCH
											: isDead ? AftershockAnimationsDefault.DEATH
													: AftershockAnimationsDefault.IDLE);
		}).setSoundKeyframeHandler(event -> {
			if (event.getKeyframeData().getSound().matches("fire"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(),
							SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 0.75F, 1.0F, true);
			if (event.getKeyframeData().getSound().matches("screaming"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(),
							SoundEvents.HUSK_HURT, SoundSource.HOSTILE, 1.25F, 0.5F, true);
			if (event.getKeyframeData().getSound().matches("looking"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(),
							SoundEvents.CHICKEN_AMBIENT, SoundSource.HOSTILE, 1.25F, 0.1F, true);
			if (event.getKeyframeData().getSound().matches("dying"))
				if (this.level.isClientSide)
					this.getCommandSenderWorld().playLocalSound(this.getX(), this.getY(), this.getZ(),
							SoundEvents.LLAMA_DEATH, SoundSource.HOSTILE, 1.25F, 0.1F, true);
		}));
	}

	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<AmericanBlasterEntity>> getSensors() {
		return ObjectArrayList.of(
				new NearbyLivingEntitySensor<AmericanBlasterEntity>()
						.setPredicate((target, entity) -> target.isAlive() && entity.hasLineOfSight(target)
								&& (!(target instanceof BaseEntity
										|| (target.getMobType() == MobType.UNDEAD && !target.isOnFire())
										|| target instanceof EnderMan || target instanceof Endermite
										|| target instanceof Creeper || target instanceof AbstractGolem)
										|| target.getType().is(AftershockMod.HEAT_ENTITY) || target.isOnFire())),
				new HurtBySensor<>(), new ItemEntitySensor<AmericanBlasterEntity>(),
				new UnreachableTargetSensor<AmericanBlasterEntity>());
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(new KillLightsTask<>().stopIf(target -> this.isAggressive()),
				new LookAtTarget<>(), new LookAtTargetSink(40, 300),
				new StrafeScreamTarget<>().startCondition(entity -> !this.isScreaming()),
				new MoveToWalkTarget<>().startCondition(entity -> !this.isPuking()));
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(new EatFoodTask<AmericanBlasterEntity>(0),
				new FirstApplicableBehaviour<AmericanBlasterEntity>(
						new TargetOrRetaliate<>().alertAlliesWhen((mob, entity) -> this.isScreaming()),
						new SetPlayerLookTarget<>().stopIf(target -> !target.isAlive()
								|| target instanceof Player && ((Player) target).isCreative()),
						new SetRandomLookTarget<>()),
				new OneRandomBehaviour<>(new SetRandomWalkTarget<>().setRadius(20).speedModifier(1.1f),
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(300, 600))));
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(
				new InvalidateAttackTarget<>()
						.invalidateIf((target, entity) -> !target.isAlive() || !entity.hasLineOfSight(target)),
				new SetWalkTargetToAttackTarget<>().speedMod(1.5F), new ShootFireTask<>(20)
//				, new AnimatableMeleeAttack<>(10)
		);
	}

	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 25.0D)
				.add(Attributes.MAX_HEALTH, AfterShocksConfig.americanblaster_health)
				.add(Attributes.ATTACK_DAMAGE, AfterShocksConfig.americanblaster_damage)
				.add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_KNOCKBACK, 0.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level world) {
		return new AzureNavigation(this, world);
	}

	@Override
	public float getMaxGrowth() {
		return 1200;
	}

	@Override
	public LivingEntity growInto() {
		return null;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	protected void customServerAiStep() {
		tickBrain(this);
		super.customServerAiStep();
	}

	@Override
	public void tick() {
		super.tick();
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
		if (this.isNewBorn() && !this.isDeadOrDying() && !this.isSearching() && !this.isScreaming()) {
			newbornCounter++;
			if (newbornCounter >= 60) {
				newbornCounter = 0;
				this.setNewBornStatus(false);
			}
		}
		if (this.isNewBorn())
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
		if (this.isScreaming() && !this.isDeadOrDying()) {
			screamingCounter++;
			if (screamingCounter > 10) 
				this.shootFlames(this.getTarget());
			if (screamingCounter > 20) {
				screamingCounter = -10;
				this.setScreamingStatus(false);
			}
		}
		if (this.isScreaming())
			this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 3, 100, false, false));
	}
}
