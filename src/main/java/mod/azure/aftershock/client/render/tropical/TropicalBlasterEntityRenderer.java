package mod.azure.aftershock.client.render.tropical;

import mod.azure.aftershock.client.model.tropical.TropicalBlasterEntityModel;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@Environment(EnvType.CLIENT)
public class TropicalBlasterEntityRenderer extends GeoEntityRenderer<AmericanBlasterEntity> {

	public TropicalBlasterEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new TropicalBlasterEntityModel());
		this.shadowRadius = 0.5f;
	}

	@Override
	protected float getDeathMaxRotation(AmericanBlasterEntity entityLivingBaseIn) {
		return 0;
	}
}
