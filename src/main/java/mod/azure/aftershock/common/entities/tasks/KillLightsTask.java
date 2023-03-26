package mod.azure.aftershock.common.entities.tasks;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.GameRules;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.util.BrainUtils;

public class KillLightsTask<E extends BaseEntity> extends ExtendedBehaviour<E> {

	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList
			.of(Pair.of(SBLMemoryTypes.NEARBY_BLOCKS.get(), MemoryStatus.VALUE_PRESENT));

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected void start(E entity) {
		entity.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return !entity.isAggressive();
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		var lightSourceLocation = entity.getBrain().getMemory(SBLMemoryTypes.NEARBY_BLOCKS.get()).orElse(null);
		var yDiff = Mth.abs(entity.getBlockY() - lightSourceLocation.stream().findFirst().get().getFirst().getY());
		var canGrief = entity.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
		return yDiff < 4 && !entity.isAggressive() && canGrief;
	}

	@Override
	protected void tick(ServerLevel level, E entity, long gameTime) {
		var lightSourceLocation = entity.getBrain().getMemory(SBLMemoryTypes.NEARBY_BLOCKS.get()).orElse(null);
		if (lightSourceLocation == null)
			return;
		if (!entity.isAggressive()) {
			if (!lightSourceLocation.stream().findFirst().get().getFirst().closerToCenterThan(entity.position(), 1.2))
				BrainUtils.setMemory(entity, MemoryModuleType.WALK_TARGET,
						new WalkTarget(lightSourceLocation.stream().findFirst().get().getFirst(), 1.5F, 0));
			if (lightSourceLocation.stream().findFirst().get().getFirst().closerToCenterThan(entity.position(), 2.6))
				entity.swing(InteractionHand.MAIN_HAND);
			if (lightSourceLocation.stream().findFirst().get().getFirst().closerToCenterThan(entity.position(), 1.2)) {
				var world = entity.level;
				var random = entity.getRandom().nextGaussian();
				var pos = lightSourceLocation.stream().findFirst().get().getFirst();
				world.destroyBlock(lightSourceLocation.stream().findFirst().get().getFirst(), true, null, 512);
				if (!world.isClientSide()) {
					for (int i = 0; i < 2; i++) {
						((ServerLevel) world).sendParticles(ParticleTypes.POOF, ((double) pos.getX()) + 0.5, pos.getY(),
								((double) pos.getZ()) + 0.5, 1, random * 0.02, random * 0.02, random * 0.02,
								0.15000000596046448);
					}
				}
			}
		}
	}

}
