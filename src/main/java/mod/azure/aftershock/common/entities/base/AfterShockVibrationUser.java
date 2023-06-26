package mod.azure.aftershock.common.entities.base;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.helper.AzureVibrationUser;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.gameevent.GameEvent;
import net.tslat.smartbrainlib.util.BrainUtils;

public class AfterShockVibrationUser extends AzureVibrationUser {

	public AfterShockVibrationUser(Mob entity, float speed, int range) {
		super(entity, speed, range);
	}

	@Override
	public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity2, float f) {
		if (this.mob.isDeadOrDying())
			return;
		if (this.mob.isVehicle())
			return;
		if (entity != null)
			if (entity instanceof LivingEntity lEntity)
				this.mob.setTarget(lEntity);
		BrainUtils.setMemory(this.mob, MemoryModuleType.WALK_TARGET, new WalkTarget(blockPos, this.moveSpeed, 0));
	}
}