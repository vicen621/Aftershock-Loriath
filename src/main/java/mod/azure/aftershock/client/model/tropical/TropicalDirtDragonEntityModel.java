package mod.azure.aftershock.client.model.tropical;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanDirtDragonEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TropicalDirtDragonEntityModel extends DefaultedEntityGeoModel<AmericanDirtDragonEntity> {

	public TropicalDirtDragonEntityModel() {
		super(AftershockMod.modResource("tropical_shreiker/tropical_shreiker"), false);
	}

	@Override
	public RenderType getRenderType(AmericanDirtDragonEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
