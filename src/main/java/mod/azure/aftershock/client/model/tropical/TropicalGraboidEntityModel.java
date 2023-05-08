package mod.azure.aftershock.client.model.tropical;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanGraboidEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TropicalGraboidEntityModel extends DefaultedEntityGeoModel<AmericanGraboidEntity> {

	public TropicalGraboidEntityModel() {
		super(AftershockMod.modResource("tropical_graboid/tropical_graboid"), false);
	}

	@Override
	public RenderType getRenderType(AmericanGraboidEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
