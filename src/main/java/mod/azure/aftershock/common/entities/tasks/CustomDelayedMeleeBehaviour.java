package mod.azure.aftershock.common.entities.tasks;

import java.util.function.Consumer;

import mod.azure.aftershock.common.entities.american.AmericanDirtDragonEntity;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;

public abstract class CustomDelayedMeleeBehaviour<E extends BaseEntity> extends ExtendedBehaviour<E> {
	protected final int delayTime;
	protected long delayFinishedAt = 0;
	protected Consumer<E> delayedCallback = entity -> {
	};

	public CustomDelayedMeleeBehaviour(int delayTicks) {
		this.delayTime = delayTicks;

		runFor(entity -> Math.max(delayTicks, 60));
	}

	/**
	 * A callback for when the delayed action is called.
	 * 
	 * @param callback The callback
	 * @return this
	 */
	public final CustomDelayedMeleeBehaviour<E> whenActivating(Consumer<E> callback) {
		this.delayedCallback = callback;

		return this;
	}

	@Override
	protected final void start(ServerLevel level, E entity, long gameTime) {
		if (this.delayTime > 0) {
			this.delayFinishedAt = gameTime + this.delayTime;
			super.start(level, entity, gameTime);
		} else {
			super.start(level, entity, gameTime);
			doDelayedAction(entity);
		}
		if (entity.getAttckingState() != 0)
			entity.setAttackingState(0);
		if (entity.getAttckingState() == 0)
			entity.setAttackingState(2);
		if (entity instanceof AmericanDirtDragonEntity dirt)
			if (dirt.getTarget() != null && !dirt.level().isClientSide()) {
				var vec3d2 = new Vec3(dirt.getTarget().getX() - dirt.getX(), 0.0, dirt.getTarget().getZ() - dirt.getZ());
				vec3d2 = vec3d2.normalize().scale(0.8).add(dirt.getDeltaMovement().scale(0.4));
				dirt.setDeltaMovement(vec3d2.x, 0.3F, vec3d2.z);
			}
		entity.triggerAnim("livingController", "attack");
	}

	@Override
	protected final void stop(ServerLevel level, E entity, long gameTime) {
		super.stop(level, entity, gameTime);

		this.delayFinishedAt = 0;
		entity.setAttackingState(0);
	}

	@Override
	protected boolean shouldKeepRunning(E entity) {
		return this.delayFinishedAt >= entity.level().getGameTime();
	}

	@Override
	protected final void tick(ServerLevel level, E entity, long gameTime) {
		super.tick(level, entity, gameTime);

		if (this.delayFinishedAt <= gameTime) {
			doDelayedAction(entity);
			this.delayedCallback.accept(entity);
		}
	}

	/**
	 * The action to take once the delay period has elapsed.
	 *
	 * @param entity The owner of the brain
	 */
	protected void doDelayedAction(E entity) {
	}
}
