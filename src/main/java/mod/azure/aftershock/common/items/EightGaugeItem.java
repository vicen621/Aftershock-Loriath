package mod.azure.aftershock.common.items;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.netty.buffer.Unpooled;
import mod.azure.aftershock.client.render.EightGaugeRender;
import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.AftershockMod.ModItems;
import mod.azure.aftershock.common.AftershockMod.ModSounds;
import mod.azure.aftershock.common.blocks.TickingLightEntity;
import mod.azure.aftershock.common.entities.projectiles.ShellEntity;
import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.Keybindings;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.items.BaseGunItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EightGaugeItem extends BaseGunItem {

	private final Supplier<Object> renderProvider = GeoItem.makeRenderer(this);
	private BlockPos lightBlockPos = null;

	public EightGaugeItem() {
		super(new Item.Properties().stacksTo(1).durability(2));
		SingletonGeoAnimatable.registerSyncedAnimatable(this);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int remainingUseTicks) {
		if (entityLiving instanceof Player playerentity) {
			if (stack.getDamageValue() < (stack.getMaxDamage() - 1)) {
				playerentity.getCooldowns().addCooldown(this, 18);
				if (!level.isClientSide) {
					stack.hurtAndBreak(1, entityLiving, p -> p.broadcastBreakEvent(entityLiving.getUsedItemHand()));
					var result = BaseGunItem.hitscanTrace(playerentity, 64, 1.0F);
					if (result != null) {
						// Hitscan if possible
						if (result.getEntity()instanceof LivingEntity livingEntity) {
							livingEntity.invulnerableTime = 0;
							livingEntity.setDeltaMovement(0, 0, 0);
							livingEntity.hurt(playerentity.damageSources().playerAttack(playerentity), AftershockMod.config.shotgun_damage);
							livingEntity.invulnerableTime = 0;
							livingEntity.setDeltaMovement(0, 0, 0);
							livingEntity.hurt(playerentity.damageSources().playerAttack(playerentity), AftershockMod.config.shotgun_damage);
						}
					} else {
						// Use fast projectile if hitscanning fails
						var bullet = createArrow(level, stack, playerentity);
						bullet.shootFromRotation(playerentity, playerentity.getXRot(), playerentity.getYRot() + 1, 0.5F, 20.0F * 3.0F, 1.0F);
						var bullet1 = createArrow(level, stack, playerentity);
						bullet1.shootFromRotation(playerentity, playerentity.getXRot(), playerentity.getYRot() - 1, 0.5F, 20.0F * 3.0F, 1.0F);
						bullet.tickCount = -15;
						bullet1.tickCount = -15;
						level.addFreshEntity(bullet);
						level.addFreshEntity(bullet1);
					}
					// Blasts the player back 1 block when firing
					playerentity.moveTo(entityLiving.getX() + (switch (playerentity.getDirection()) {
					case WEST -> 1.0F;
					case EAST -> -1.0F;
					default -> 0.0F;
					}), entityLiving.getY(), entityLiving.getZ() + (switch (playerentity.getDirection()) {
					case NORTH -> 1.0F;
					case SOUTH -> -1.0F;
					default -> 0.0F;
					}));
					// Plays firing sound
					level.playSound((Player) null, playerentity.getX(), playerentity.getY(), playerentity.getZ(), ModSounds.SHOTGUN, SoundSource.PLAYERS, 1.5F, 1.7F);
					// Plays firing animation
					triggerAnim(playerentity, GeoItem.getOrAssignId(stack, (ServerLevel) level), "shoot_controller", "firing");
				}
				// spawn light block
				spawnLightSource(entityLiving, playerentity.level().isWaterAt(playerentity.blockPosition()));
			}
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (world.isClientSide)
			if (((Player) entity).getMainHandItem().getItem() instanceof EightGaugeItem) {
				if (Keybindings.RELOAD.isDown() && selected) {
					var passedData = new FriendlyByteBuf(Unpooled.buffer());
					passedData.writeBoolean(true);
					ClientPlayNetworking.send(AftershockMod.SHOTGUN, passedData);
				}
			}
	}

	public void reload(Player user, InteractionHand hand) {
		if (user.getItemInHand(hand).getItem() instanceof EightGaugeItem) {
			while (!user.isCreative() && user.getItemInHand(hand).getDamageValue() != 0 && user.getInventory().countItem(ModItems.SHOTGUN_SHELL) > 0) {
				removeAmmo(ModItems.SHOTGUN_SHELL, user);
				user.getItemInHand(hand).hurtAndBreak(-1, user, s -> user.broadcastBreakEvent(hand));
				user.getItemInHand(hand).setPopTime(3);
				user.getCommandSenderWorld().playSound((Player) null, user.getX(), user.getY(), user.getZ(), ModSounds.SHOTGUNRELOAD, SoundSource.PLAYERS, 1.00F, 1.0F);
				if (!user.level().isClientSide)
					triggerAnim(user, GeoItem.getOrAssignId(user.getItemInHand(hand), (ServerLevel) user.getCommandSenderWorld()), "shoot_controller", "reload");
			}
		}
	}

	public ShellEntity createArrow(Level worldIn, ItemStack stack, LivingEntity shooter) {
		return new ShellEntity(worldIn, shooter);
	}

	public void addNBTData(ItemStack stack, String key, Tag tag) {
		CompoundTag compound = stack.getOrCreateTag();
		compound.put(key, tag);
		stack.setTag(compound);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
		tooltip.add(Component.translatable(AftershockMod.MODID + ".ammo.reloadshells").withStyle(ChatFormatting.ITALIC));
		super.appendHoverText(stack, world, tooltip, context);
	}

	@Override
	public void createRenderer(Consumer<Object> consumer) {
		consumer.accept(new RenderProvider() {
			private final EightGaugeRender renderer = null;

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				if (renderer == null)
					return new EightGaugeRender();
				return this.renderer;
			}
		});
	}

	@Override
	public Supplier<Object> getRenderProvider() {
		return this.renderProvider;
	}

	@Override
	protected void spawnLightSource(Entity entity, boolean isInWaterBlock) {
		if (lightBlockPos == null) {
			lightBlockPos = findFreeSpace(entity.level(), entity.blockPosition(), 2);
			if (lightBlockPos == null)
				return;
			entity.level().setBlockAndUpdate(lightBlockPos, AftershockMod.ModBlocks.TICKING_LIGHT_BLOCK.defaultBlockState());
		} else if (checkDistance(lightBlockPos, entity.blockPosition(), 2)) {
			BlockEntity blockEntity = entity.level().getBlockEntity(lightBlockPos);
			if (blockEntity instanceof TickingLightEntity) {
				((TickingLightEntity) blockEntity).refresh(isInWaterBlock ? 20 : 0);
			} else
				lightBlockPos = null;
		} else
			lightBlockPos = null;
	}

	private boolean checkDistance(BlockPos blockPosA, BlockPos blockPosB, int distance) {
		return Math.abs(blockPosA.getX() - blockPosB.getX()) <= distance && Math.abs(blockPosA.getY() - blockPosB.getY()) <= distance && Math.abs(blockPosA.getZ() - blockPosB.getZ()) <= distance;
	}

	private BlockPos findFreeSpace(Level world, BlockPos blockPos, int maxDistance) {
		if (blockPos == null)
			return null;

		int[] offsets = new int[maxDistance * 2 + 1];
		offsets[0] = 0;
		for (int i = 2; i <= maxDistance * 2; i += 2) {
			offsets[i - 1] = i / 2;
			offsets[i] = -i / 2;
		}
		for (int x : offsets)
			for (int y : offsets)
				for (int z : offsets) {
					BlockPos offsetPos = blockPos.offset(x, y, z);
					BlockState state = world.getBlockState(offsetPos);
					if (state.isAir() || state.getBlock().equals(AftershockMod.ModBlocks.TICKING_LIGHT_BLOCK))
						return offsetPos;
				}

		return null;
	}
}
