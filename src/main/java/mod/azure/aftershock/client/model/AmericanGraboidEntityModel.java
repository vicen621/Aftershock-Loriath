package mod.azure.aftershock.client.model;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.AmericanGraboidEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AmericanGraboidEntityModel extends DefaultedEntityGeoModel<AmericanGraboidEntity> {

	public AmericanGraboidEntityModel() {
		super(AftershockMod.modResource("american_graboid/american_graboid"), false);
	}

	@Override
	public RenderType getRenderType(AmericanGraboidEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
