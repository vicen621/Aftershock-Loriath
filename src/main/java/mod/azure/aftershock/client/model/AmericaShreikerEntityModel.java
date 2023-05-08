package mod.azure.aftershock.client.model;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanShreikerEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class AmericaShreikerEntityModel extends DefaultedEntityGeoModel<AmericanShreikerEntity> {

	public AmericaShreikerEntityModel() {
		super(AftershockMod.modResource("american_shreiker/american_shreiker"), false);
	}

	@Override
	public ResourceLocation getTextureResource(AmericanShreikerEntity animatable) {
		return AftershockMod.modResource("textures/entity/american_shreiker/american_shreiker" + (animatable.getGrowth() >= 42000 ? "_molt.png" : ".png"));
	}

	@Override
	public RenderType getRenderType(AmericanShreikerEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
