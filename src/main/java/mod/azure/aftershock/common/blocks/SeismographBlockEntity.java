package mod.azure.aftershock.common.blocks;

import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SeismographBlockEntity extends BlockEntity implements GeoBlockEntity {

	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	
	public SeismographBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ModMobs.SEIMOGRAPH, blockPos, blockState);
	}

	@Override
	public void registerControllers(ControllerRegistrar controllers) {
		controllers.add(new AnimationController<>(this, "popup_controller", 0, state -> PlayState.CONTINUE));
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return cache;
	}

}
