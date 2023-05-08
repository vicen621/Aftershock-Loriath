package mod.azure.aftershock.client.model.tropical;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TropicalBlasterEntityModel extends DefaultedEntityGeoModel<AmericanBlasterEntity> {

	public TropicalBlasterEntityModel() {
		super(AftershockMod.modResource("tropical_blaster/tropical_blaster"), false);
	}

	@Override
	public RenderType getRenderType(AmericanBlasterEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
