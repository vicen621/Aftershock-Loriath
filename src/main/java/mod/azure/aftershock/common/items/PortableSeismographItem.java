package mod.azure.aftershock.common.items;

import java.util.List;

import mod.azure.aftershock.common.entities.base.SoundTrackingEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class PortableSeismographItem extends Item {

	protected int soundcounter;

	public PortableSeismographItem() {
		super(new Item.Properties().durability(600));
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level level, List<Component> list, TooltipFlag tooltipFlag) {
		list.add(Component.translatable("aftershock.portable.text").withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.ITALIC));
		super.appendHoverText(itemStack, level, list, tooltipFlag);
	}

	@Override
	public boolean isValidRepairItem(ItemStack itemStack, ItemStack itemStack2) {
		return itemStack2.is(Items.IRON_INGOT);
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
		if (level != null)
			if (entity != null)
				if (selected && itemStack.getDamageValue() < itemStack.getMaxDamage() - 1)
					entity.getLevel().getEntitiesOfClass(SoundTrackingEntity.class, new AABB(entity.blockPosition()).inflate(5D, 5D, 5D)).forEach(e -> {
						if (e.walkAnimation.speed() >= 0.15F) {
							this.soundcounter++;
							if (this.soundcounter >= 5) {
								if (entity.getLevel().isClientSide)
									entity.getLevel().playSound(entity, entity.blockPosition(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.BLOCKS, 1.0f, 3.3f);
								this.soundcounter = 0;
								itemStack.setDamageValue(itemStack.getDamageValue() + 1);
							}
						} else
							this.soundcounter = 0;
					});
				else
					this.soundcounter = 0;
		super.inventoryTick(itemStack, level, entity, slot, selected);
	}

}
