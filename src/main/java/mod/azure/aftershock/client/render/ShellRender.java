package mod.azure.aftershock.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.aftershock.common.entities.projectiles.ShellEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class ShellRender extends EntityRenderer<ShellEntity> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/item/fire_charge.png");

	public ShellRender(EntityRendererProvider.Context renderManagerIn) {
		super(renderManagerIn);
	}

	@Override
	protected int getBlockLightLevel(ShellEntity FireballEntity, BlockPos blockPos) {
		return 15;
	}

	@Override
	public void render(ShellEntity FireballEntity, float f, float g, PoseStack matrixStack, MultiBufferSource vertexConsumerProvider, int i) {
		matrixStack.pushPose();
		matrixStack.scale(0.0F, 0.0F, 0.0F);
		matrixStack.popPose();
		super.render(FireballEntity, f, g, matrixStack, vertexConsumerProvider, i);
	}

	@Override
	public ResourceLocation getTextureLocation(ShellEntity FireballEntity) {
		return TEXTURE;
	}
}