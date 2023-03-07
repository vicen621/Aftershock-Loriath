package mod.azure.aftershock.common.entities;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.aftershock.common.helpers.AzureVibrationListener;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.tslat.smartbrainlib.api.SmartBrainOwner;
import net.tslat.smartbrainlib.api.core.BrainActivityGroup;
import net.tslat.smartbrainlib.api.core.SmartBrainProvider;
import net.tslat.smartbrainlib.api.core.behaviour.FirstApplicableBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.OneRandomBehaviour;
import net.tslat.smartbrainlib.api.core.behaviour.custom.attack.AnimatableMeleeAttack;
import net.tslat.smartbrainlib.api.core.behaviour.custom.look.LookAtTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.misc.Idle;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.MoveToWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.move.StrafeTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetRandomWalkTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.path.SetWalkTargetToAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.InvalidateAttackTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetPlayerLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.SetRandomLookTarget;
import net.tslat.smartbrainlib.api.core.behaviour.custom.target.TargetOrRetaliate;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.custom.UnreachableTargetSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.HurtBySensor;

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
			if (getCurrentAttackType() != AttackType.NONE && attackProgress > 0 && !isDead)
				return event.setAndContinue(RawAnimation.begin()
						.then(AttackType.animationMappings.get(getCurrentAttackType()), LoopType.PLAY_ONCE));
			if (event.isMoving() && !this.isAggressive() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			if (event.isMoving() && this.isAggressive() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.RUN);
			if (isDead)
				return event.setAndContinue(AftershockAnimationsDefault.DEATH);
			return event.setAndContinue(this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead
					? AftershockAnimationsDefault.HURT
					: this.isSearching == true ? AftershockAnimationsDefault.LOOK : AftershockAnimationsDefault.IDLE);
		}));
	}

	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<AmericanBlasterEntity>> getSensors() {
		return ObjectArrayList.of(new HurtBySensor<>(), new UnreachableTargetSensor<AmericanBlasterEntity>());
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(new LookAtTarget<>(), new LookAtTargetSink(40, 300), new StrafeTarget<>(),
				new MoveToWalkTarget<>());
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				new FirstApplicableBehaviour<AmericanBlasterEntity>(new TargetOrRetaliate<>(),
						new SetPlayerLookTarget<>().stopIf(target -> !target.isAlive()
								|| target instanceof Player && ((Player) target).isCreative()),
						new SetRandomLookTarget<>()),
				new OneRandomBehaviour<>(new SetRandomWalkTarget<>().setRadius(20).speedModifier(1.1f),
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(300, 600))));
	}

	@Override
	public BrainActivityGroup<AmericanBlasterEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(new InvalidateAttackTarget<>().invalidateIf(
				(entity, target) -> !target.isAlive() || target instanceof Player && ((Player) target).isCreative()),
				new SetWalkTargetToAttackTarget<>().speedMod(1.5F), new AnimatableMeleeAttack<>(10));
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
	}
}
