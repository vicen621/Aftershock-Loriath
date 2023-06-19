package mod.azure.aftershock.common.entities.tasks;

import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
import mod.azure.aftershock.common.entities.american.AmericanShreikerEntity;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import mod.azure.aftershock.common.entities.sensors.AftershockMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.tslat.smartbrainlib.util.BrainUtils;

public class EatFoodTask<E extends BaseEntity> extends DelayedFoodBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(AftershockMemoryTypes.FOOD_ITEMS.get(), MemoryStatus.VALUE_PRESENT), Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));

	protected Function<E, Integer> attackIntervalSupplier = entity -> 20;

	@Nullable
	protected LivingEntity target = null;

	public EatFoodTask(int delayTicks) {
		super(delayTicks);
	}

	public EatFoodTask<E> attackInterval(Function<E, Integer> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {

		return entity.getGrowth() >= (entity instanceof AmericanShreikerEntity ? 1200 : 0) && !entity.isPuking();
	}

	@Override
	protected void start(E entity) {
	}

	@Override
	protected void stop(E entity) {
		this.target = null;
	}

	@Override
	protected void doDelayedAction(E entity) {
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));
		var itemLocation = entity.getBrain().getMemory(AftershockMemoryTypes.FOOD_ITEMS.get()).orElse(null);

		if (itemLocation.stream().findFirst().get() == null)
			return;

		if (!itemLocation.stream().findFirst().get().blockPosition().closerToCenterThan(entity.position(), 1.2)) {
			BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET, new WalkTarget(itemLocation.stream().findFirst().get().blockPosition(), 1.5F, 0));
			entity.setEatingStatus(false);
		}
		if (itemLocation.stream().findFirst().get().blockPosition().closerToCenterThan(entity.position(), 1.5)) {
			entity.setEatingStatus(true);
		}
		if (itemLocation.stream().findFirst().get().blockPosition().closerToCenterThan(entity.position(), 1.2)) {
			if (entity instanceof AmericanShreikerEntity shriker)
				shriker.setPukingStatus(true);
			entity.heal(2.5F);
			if (entity instanceof AmericanBlasterEntity blaster)
				blaster.eatingCounter++;
			entity.getNavigation().stop();
			itemLocation.stream().findFirst().get().getItem().finishUsingItem(entity.level(), entity);
			itemLocation.stream().findFirst().get().getItem().shrink(1);
		}
	}
}