package mod.azure.aftershock.client.model.american;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanDirtDragonEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AmericanDirtDragonEntityModel extends DefaultedEntityGeoModel<AmericanDirtDragonEntity> {

	public AmericanDirtDragonEntityModel() {
		super(AftershockMod.modResource("american_dirt_dragon/american_dirt_dragon"), false);
	}

	@Override
	public RenderType getRenderType(AmericanDirtDragonEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
