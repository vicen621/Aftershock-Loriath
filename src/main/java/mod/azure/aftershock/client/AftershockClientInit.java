package mod.azure.aftershock.client;

import mod.azure.aftershock.client.render.AmericanBlasterEntityRenderer;
import mod.azure.aftershock.client.render.AmericanGraboidEntityRenderer;
import mod.azure.aftershock.client.render.AmericanShreikerEntityRenderer;
import mod.azure.aftershock.client.render.SeismographBlockRenderer;
import mod.azure.aftershock.common.AftershockMod.ModMobs;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class AftershockClientInit implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.register(ModMobs.AMERICAN_BLASTER, AmericanBlasterEntityRenderer::new);
		EntityRendererRegistry.register(ModMobs.AMERICAN_SHREIKER, AmericanShreikerEntityRenderer::new);
		EntityRendererRegistry.register(ModMobs.AMERICAN_GRABOID, AmericanGraboidEntityRenderer::new);
//		EntityRendererRegistry.register(ModMobs.DIRT_DRAGON, DirtDragonEntityRenderer::new);
		BlockEntityRenderers.register(ModMobs.SEIMOGRAPH, (Context ctx) -> new SeismographBlockRenderer());
	}
}