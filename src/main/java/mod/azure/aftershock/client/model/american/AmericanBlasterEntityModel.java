package mod.azure.aftershock.client.model.american;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
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
public class AmericanBlasterEntityModel extends DefaultedEntityGeoModel<AmericanBlasterEntity> {

	public AmericanBlasterEntityModel() {
		super(AftershockMod.modResource("american_blaster/american_blaster"), false);
	}

	@Override
	public RenderType getRenderType(AmericanBlasterEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

	@Override
	public void setCustomAnimations(AmericanBlasterEntity animatable, long instanceId, AnimationState<AmericanBlasterEntity> animationState) {
		CoreGeoBone head = getAnimationProcessor().getBone("root");

		if (head != null)
			head.setRotY(animationState.getData(DataTickets.ENTITY_MODEL_DATA).netHeadYaw() * Mth.DEG_TO_RAD);

		super.setCustomAnimations(animatable, instanceId, animationState);
	}

}
