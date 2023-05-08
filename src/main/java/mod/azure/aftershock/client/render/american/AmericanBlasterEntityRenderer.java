package mod.azure.aftershock.client.render.american;

import mod.azure.aftershock.client.model.american.AmericanBlasterEntityModel;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class AmericanBlasterEntityRenderer extends GeoEntityRenderer<AmericanBlasterEntity> {

	public AmericanBlasterEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new AmericanBlasterEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanBlasterEntity entityLivingBaseIn) {
		return 0;
	}
}
