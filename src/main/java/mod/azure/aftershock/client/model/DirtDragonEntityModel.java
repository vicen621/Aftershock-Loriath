package mod.azure.aftershock.client.model;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.DirtDragonEntity;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class DirtDragonEntityModel extends DefaultedEntityGeoModel<DirtDragonEntity> {

	public DirtDragonEntityModel() {
		super(AftershockMod.modResource("dirt_dragon/dirt_dragon"), false);
	}

	@Override
	public RenderType getRenderType(DirtDragonEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}
