package mod.azure.aftershock.client.render.american;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.aftershock.client.model.american.AmericanDirtDragonEntityModel;
import mod.azure.aftershock.common.entities.american.AmericanDirtDragonEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class AmericanDirtDragonEntityRenderer extends GeoEntityRenderer<AmericanDirtDragonEntity> {

	public AmericanDirtDragonEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new AmericanDirtDragonEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanDirtDragonEntity entityLivingBaseIn) {
		return 0;
	}

	@Override
	public void render(AmericanDirtDragonEntity entity, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource bufferIn, int packedLightIn) {
		var scaleFactor = 0.3f + ((entity.getGrowth() / 1200) / 2.0f);
		stack.scale(entity.getGrowth() > 1200 ? 1.0F : scaleFactor, entity.getGrowth() > 1200 ? 1.0F : scaleFactor, entity.getGrowth() > 1200 ? 1.0F : scaleFactor);
		if (entity.isInSand())
			stack.translate(0, -0.4, 0);
		super.render(entity, entityYaw, partialTicks, stack, bufferIn, packedLightIn);
	}
}
