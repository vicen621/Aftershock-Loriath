package mod.azure.aftershock.client.render.tropical;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.aftershock.client.model.tropical.TropicalGraboidEntityModel;
import mod.azure.aftershock.common.entities.american.AmericanGraboidEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class TropicalGraboidEntityRenderer extends GeoEntityRenderer<AmericanGraboidEntity> {

	public TropicalGraboidEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new TropicalGraboidEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanGraboidEntity entityLivingBaseIn) {
		return 0;
	}

	@Override
	public void render(AmericanGraboidEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
		var scaleFactor = 0.3f + ((entity.getGrowth() / 1200) / 2.0f);
		stack.scale(entity.getGrowth() > 1200 ? 1.1F : scaleFactor, entity.getGrowth() > 1200 ? 1.1F : scaleFactor, entity.getGrowth() > 1200 ? 1.1F : scaleFactor);
		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}
}
