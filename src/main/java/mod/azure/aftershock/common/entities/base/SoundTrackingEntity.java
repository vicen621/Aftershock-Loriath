package mod.azure.aftershock.common.entities.base;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class SoundTrackingEntity extends BaseEntity {

	public static final EntityDataAccessor<Boolean> IN_SAND = SynchedEntityData.defineId(SoundTrackingEntity.class, EntityDataSerializers.BOOLEAN);

	protected SoundTrackingEntity(EntityType<? extends Monster> entityType, Level level) {
		super(entityType, level);
	}

	// Data Saving
	@Override
	public void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(IN_SAND, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean("insand", this.isInSand());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		if (compoundTag.contains("passedout"))
			setInSand(compoundTag.getBoolean("passedout"));
	}

	public boolean isInSand() {
		return this.entityData.get(IN_SAND);
	}

	public void setInSand(boolean sand) {
		this.entityData.set(IN_SAND, Boolean.valueOf(sand));
	}

	@Override
	public boolean hurt(DamageSource damageSource, float f) {
		return damageSource == damageSources().inWall() ? false : super.hurt(damageSource, f);
	}

	@Override
	public void tick() {
		super.tick();
		var pos = BlockPos.containing(this.getX(), this.getSurface((int) Math.floor(this.getX()), (int) Math.floor(this.getY()), (int) Math.floor(this.getZ())), this.getZ()).below();
		this.setInSand(((this.level().getBlockState(pos).is(BlockTags.SAND) || this.level().getBlockState(pos.below()).is(BlockTags.SAND)) || (this.level().getBlockState(pos).is(BlockTags.DIRT) || this.level().getBlockState(pos.below()).is(BlockTags.DIRT))) && this.deathTime < 5);
	}

	public int getSurface(int x, int y, int z) {
		var pos = new BlockPos(x, y, z);
		while (!level().isEmptyBlock(pos))
			pos = pos.above();
		return pos.getY();
	}

}
