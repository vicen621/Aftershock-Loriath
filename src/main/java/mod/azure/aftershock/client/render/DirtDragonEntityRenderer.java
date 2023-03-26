package mod.azure.aftershock.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.aftershock.client.model.AmericaShreikerEntityModel;
import mod.azure.aftershock.common.entities.AmericanShreikerEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class DirtDragonEntityRenderer extends GeoEntityRenderer<AmericanShreikerEntity> {

	public DirtDragonEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new AmericaShreikerEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanShreikerEntity entityLivingBaseIn) {
		return 0;
	}

	@Override
	public void render(AmericanShreikerEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
		float scaleFactor = 0.8f + ((entity.getGrowth() / 1200) / 4.0f);
		stack.scale(scaleFactor, scaleFactor, scaleFactor);
		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}
}
