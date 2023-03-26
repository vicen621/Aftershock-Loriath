package mod.azure.aftershock.common.entities;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.helpers.AftershockAnimationsDefault;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.azurelib.ai.pathing.AzureNavigation;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.Animation.LoopType;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.helper.AzureVibrationListener;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

public class AmericanGraboidEntity extends BaseEntity implements SmartBrainOwner<AmericanGraboidEntity> {

	public AmericanGraboidEntity(EntityType<? extends BaseEntity> entityType, Level level) {
		super(entityType, level);
		this.dynamicGameEventListener = new DynamicGameEventListener<AzureVibrationListener>(
				new AzureVibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 15, this));
		this.xpReward = AfterShocksConfig.americangraboid_exp;
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "livingController", 5, event -> {
			var isDead = this.dead || this.getHealth() < 0.01 || this.isDeadOrDying();
			if (getCurrentAttackType() != AttackType.NONE && attackProgress > 0 && !isDead)
				return event.setAndContinue(RawAnimation.begin()
						.then(AttackType.animationMappings.get(getCurrentAttackType()), LoopType.PLAY_ONCE));
			if (event.isMoving() && this.getLastDamageSource() == null)
				return event.setAndContinue(AftershockAnimationsDefault.WALK);
			if (isDead)
				return event.setAndContinue(AftershockAnimationsDefault.DEATH);
			return event.setAndContinue(this.getLastDamageSource() != null && this.hurtDuration > 0 && !isDead
					? AftershockAnimationsDefault.HURT
					: AftershockAnimationsDefault.IDLE);
		}));
	}

	@Override
	protected Brain.Provider<?> brainProvider() {
		return new SmartBrainProvider<>(this);
	}

	@Override
	public List<ExtendedSensor<AmericanGraboidEntity>> getSensors() {
		return ObjectArrayList.of(new HurtBySensor<>(), new UnreachableTargetSensor<AmericanGraboidEntity>());
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getCoreTasks() {
		return BrainActivityGroup.coreTasks(new LookAtTarget<>(), new LookAtTargetSink(40, 300), new StrafeTarget<>(),
				new MoveToWalkTarget<>());
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getIdleTasks() {
		return BrainActivityGroup.idleTasks(
				new FirstApplicableBehaviour<AmericanGraboidEntity>(new TargetOrRetaliate<>(),
						new SetPlayerLookTarget<>().stopIf(target -> !target.isAlive()
								|| target instanceof Player && ((Player) target).isCreative()),
						new SetRandomLookTarget<>()),
				new OneRandomBehaviour<>(new SetRandomWalkTarget<>().setRadius(20).speedModifier(1.1f),
						new Idle<>().runFor(entity -> entity.getRandom().nextInt(300, 600))));
	}

	@Override
	public BrainActivityGroup<AmericanGraboidEntity> getFightTasks() {
		return BrainActivityGroup.fightTasks(new InvalidateAttackTarget<>().invalidateIf(
				(entity, target) -> !target.isAlive() || target instanceof Player && ((Player) target).isCreative()),
				new SetWalkTargetToAttackTarget<>().speedMod(1.5F), new AnimatableMeleeAttack<>(10));
	}

	public static AttributeSupplier.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 25.0D)
				.add(Attributes.MAX_HEALTH, AfterShocksConfig.americangraboid_health)
				.add(Attributes.ATTACK_DAMAGE, AfterShocksConfig.americangraboid_damage)
				.add(Attributes.MOVEMENT_SPEED, 0.25D).add(Attributes.ATTACK_KNOCKBACK, 0.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level world) {
		return new AzureNavigation(this, world);
	}

	@Override
	public float getMaxGrowth() {
		return 336000 + 6000;
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

	@Override
	public void growUp(LivingEntity entity) {
		var world = entity.level;
		if (!world.isClientSide()) {
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
		var entity = new AmericanShreikerEntity(ModMobs.AMERICAN_SHREIKER, level);
		if (hasCustomName())
			entity.setCustomName(this.getCustomName());
		entity.setNewBornStatus(true);
		entity.setGrowth(0);
		entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 100, false, false));
		var areaEffectCloudEntity = new AreaEffectCloud(this.level, this.getX(), this.getY() + 1, this.getZ());
		areaEffectCloudEntity.setRadius(1.0F);
		areaEffectCloudEntity.setDuration(20);
		areaEffectCloudEntity.setParticle(ParticleTypes.POOF);
		areaEffectCloudEntity
				.setRadiusPerTick(-areaEffectCloudEntity.getRadius() / (float) areaEffectCloudEntity.getDuration());
		entity.level.addFreshEntity(areaEffectCloudEntity);
		return entity;
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
			setCurrentAttackType(switch (random.nextInt(2)) {
			case 0 -> AttackType.BITE;
			default -> AttackType.BITE;
			});
	}

}
