package mod.azure.aftershock.client.model.american;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanGraboidEntity;
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
public class AmericanGraboidEntityModel extends DefaultedEntityGeoModel<AmericanGraboidEntity> {

	public AmericanGraboidEntityModel() {
		super(AftershockMod.modResource("american_graboid/american_graboid"), false);
	}

	@Override
	public ResourceLocation getTextureResource(AmericanGraboidEntity animatable) {
		return AftershockMod.modResource("textures/entity/american_graboid/american_graboid" + (animatable.isAlbino() ? "_albino.png" : ".png"));
	}

	@Override
	public RenderType getRenderType(AmericanGraboidEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

	@Override
	public void setCustomAnimations(AmericanGraboidEntity animatable, long instanceId, AnimationState<AmericanGraboidEntity> animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("body00");

		if (head != null)
			head.setRotY(animationState.getData(DataTickets.ENTITY_MODEL_DATA).netHeadYaw() * Mth.DEG_TO_RAD);

		super.setCustomAnimations(animatable, instanceId, animationState);
	}

}
