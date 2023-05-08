package mod.azure.aftershock.common.entities.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class SoundTrackingEntity extends BaseEntity {

	protected SoundTrackingEntity(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

}
