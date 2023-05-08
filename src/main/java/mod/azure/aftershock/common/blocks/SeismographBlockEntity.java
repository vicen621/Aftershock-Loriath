package mod.azure.aftershock.common.blocks;

import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.aftershock.common.entities.base.SoundTrackingEntity;
import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;

public class SeismographBlockEntity extends BlockEntity implements GeoBlockEntity {

	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	public static final EnumProperty<SeismographStates> CHEST_STATE = SeismographProperties.STORAGE_STATE;
	protected static int soundcounter;

	public SeismographBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModMobs.SEIMOGRAPH, blockPos, blockState);
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, event -> {
			if (getChestState().equals(SeismographStates.OPENED))
				return event.setAndContinue(RawAnimation.begin().thenLoop("moving"));
			else if (getChestState().equals(SeismographStates.CLOSED))
				return event.setAndContinue(RawAnimation.begin().thenLoop("idle"));
			return PlayState.CONTINUE;
		}));
		controllers.add(new AnimationController<>(this, "popup_controller", 0, state -> PlayState.CONTINUE));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

	public SeismographStates getChestState() {
		return this.getBlockState().getValue(SeismographBlockEntity.CHEST_STATE);
	}

	public void setChestState(SeismographStates state) {
		this.getLevel().setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(CHEST_STATE, state));
	}

	public static void tick(Level world, BlockPos pos, BlockState state, SeismographBlockEntity blockEntity) {
		final var aabb = new AABB(pos).inflate(64D, 64D, 64D);
		if (world != null)
			world.getEntitiesOfClass(SoundTrackingEntity.class, aabb).forEach(e -> {
				if (e.walkAnimation.speed() >= 0.15F) {
					SeismographBlockEntity.soundcounter++;
					blockEntity.setChestState(SeismographStates.OPENED);
					if (SeismographBlockEntity.soundcounter >= 5) {
//						if (blockEntity.getLevel().isClientSide)
							blockEntity.getLevel().playSound(e, pos, SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0f, 3.3f);
						SeismographBlockEntity.soundcounter = 0;
					}
				} else {
					blockEntity.setChestState(SeismographStates.CLOSED);
					SeismographBlockEntity.soundcounter = 0;
				}
			});
	}

}
