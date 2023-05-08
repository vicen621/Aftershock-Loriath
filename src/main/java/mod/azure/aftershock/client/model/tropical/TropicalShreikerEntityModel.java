package mod.azure.aftershock.client.model.tropical;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanShreikerEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class TropicalShreikerEntityModel extends DefaultedEntityGeoModel<AmericanShreikerEntity> {

	public TropicalShreikerEntityModel() {
		super(AftershockMod.modResource("tropical_dirt_dragon/tropical_dirt_dragon"), false);
	}

	@Override
	public ResourceLocation getTextureResource(AmericanShreikerEntity animatable) {
		return AftershockMod.modResource("textures/entity/tropical_dirt_dragon/tropical_dirt_dragon" + (animatable.getGrowth() >= 42000 ? "_molt.png" : ".png"));
	}

	@Override
	public RenderType getRenderType(AmericanShreikerEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
