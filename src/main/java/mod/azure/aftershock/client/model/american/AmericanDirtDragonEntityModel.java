package mod.azure.aftershock.client.model.american;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanDirtDragonEntity;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AmericanDirtDragonEntityModel extends DefaultedEntityGeoModel<AmericanDirtDragonEntity> {

	public AmericanDirtDragonEntityModel() {
		super(AftershockMod.modResource("american_dirt_dragon/american_dirt_dragon"), false);
	}

	@Override
	public ResourceLocation getTextureResource(AmericanDirtDragonEntity animatable) {
		return AftershockMod.modResource("textures/entity/american_dirt_dragon/american_dirt_dragon" + (animatable.isAlbino() ? "_albino.png" : ".png"));
	}

	@Override
	public RenderType getRenderType(AmericanDirtDragonEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

	@Override
	public void setCustomAnimations(AmericanDirtDragonEntity animatable, long instanceId, AnimationState<AmericanDirtDragonEntity> animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("body");

		if (head != null)
			head.setRotY(animationState.getData(DataTickets.ENTITY_MODEL_DATA).netHeadYaw() * Mth.DEG_TO_RAD);

		super.setCustomAnimations(animatable, instanceId, animationState);
	}

}
