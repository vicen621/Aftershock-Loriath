package mod.azure.aftershock.client.model.american;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AmericanBlasterEntityModel extends DefaultedEntityGeoModel<AmericanBlasterEntity> {

	public AmericanBlasterEntityModel() {
		super(AftershockMod.modResource("american_blaster/american_blaster"), false);
	}

	@Override
	public RenderType getRenderType(AmericanBlasterEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
