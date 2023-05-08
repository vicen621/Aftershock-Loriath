package mod.azure.aftershock.client.render.american;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.aftershock.client.model.american.AmericaShreikerEntityModel;
import mod.azure.aftershock.common.entities.american.AmericanShreikerEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class AmericanShreikerEntityRenderer extends GeoEntityRenderer<AmericanShreikerEntity> {

	public AmericanShreikerEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new AmericaShreikerEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanShreikerEntity entityLivingBaseIn) {
		return 0;
	}

	@Override
	public void render(AmericanShreikerEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
		var scaleFactor = 0.5f + ((entity.getGrowth() / 1200) / 2.0f);
		stack.scale(entity.getGrowth() > 1200 ? 1.35F : scaleFactor, entity.getGrowth() > 1200 ? 1.35F : scaleFactor, entity.getGrowth() > 1200 ? 1.35F : scaleFactor);
		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}
}
