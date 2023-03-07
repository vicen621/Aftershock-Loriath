package mod.azure.aftershock.common.items;

import java.util.function.Consumer;
import java.util.function.Supplier;

import mod.azure.aftershock.client.render.SeismographItemRender;
import mod.azure.aftershock.common.AftershockMod.ModBlocks;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager.ControllerRegistrar;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class SeismographBlockItem extends BlockItem implements GeoItem {

	private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);

	public SeismographBlockItem() {
		super(ModBlocks.SEIMOGRAPH, new Item.Properties());
	}

	@Override
	public void createRenderer(Consumer<Object> consumer) {
		consumer.accept(new RenderProvider() {
			private SeismographItemRender renderer;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (this.renderer == null)
					this.renderer = new SeismographItemRender();

				return this.renderer;
			}
		});
	}

	@Override
	public Supplier<Object> getRenderProvider() {
		return renderProvider;
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
