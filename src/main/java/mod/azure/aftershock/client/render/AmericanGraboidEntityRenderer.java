package mod.azure.aftershock.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.aftershock.client.model.AmericanGraboidEntityModel;
import mod.azure.aftershock.common.entities.AmericanGraboidEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class AmericanGraboidEntityRenderer extends GeoEntityRenderer<AmericanGraboidEntity> {

	public AmericanGraboidEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new AmericanGraboidEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanGraboidEntity entityLivingBaseIn) {
		return 0;
	}

	@Override
	public void render(AmericanGraboidEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		poseStack.scale(1.1F, 1.1F, 1.1F);
		super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
	}
}
