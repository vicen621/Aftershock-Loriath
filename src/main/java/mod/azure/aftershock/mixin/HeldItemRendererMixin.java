package mod.azure.aftershock.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.aftershock.common.items.PortableSeismographItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;

@Mixin(value = ItemInHandRenderer.class)
public abstract class HeldItemRendererMixin {

	@Shadow
	@Final
	private final Minecraft minecraft;
	@Shadow
	private float mainHandHeight;
	@Shadow
	private float offHandHeight;
	@Shadow
	private ItemStack mainHandItem;
	@Shadow
	private ItemStack offHandItem;

	public HeldItemRendererMixin(Minecraft client) {
		this.minecraft = client;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	public void portable$cancelAnimation(CallbackInfo ci) {
		var clientPlayerEntity = this.minecraft.player;
		var itemStack = clientPlayerEntity.getMainHandItem();
		var itemStack2 = clientPlayerEntity.getOffhandItem();
		if (this.mainHandItem.getItem() instanceof PortableSeismographItem && itemStack.getItem() instanceof PortableSeismographItem && ItemStack.isSame(mainHandItem, itemStack)) {
			this.mainHandHeight = 1;
			this.mainHandItem = itemStack;
		}
		if (this.offHandItem.getItem() instanceof PortableSeismographItem && itemStack2.getItem() instanceof PortableSeismographItem && ItemStack.isSame(offHandItem, itemStack2)) {
			this.offHandHeight = 1;
			this.offHandItem = itemStack2;
		}
	}
}
