package mod.azure.aftershock.common.blocks;

import org.jetbrains.annotations.Nullable;

import mod.azure.aftershock.common.AftershockMod.ModBlocks;
import mod.azure.aftershock.common.AftershockMod.ModItems;
import mod.azure.aftershock.common.AftershockMod.ModMobs;
import mod.azure.aftershock.common.entities.base.BaseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GraboidEggBlock extends Block {

	public static final int MAX_HATCH_LEVEL = 2;
	public static final IntegerProperty HATCH = BlockStateProperties.HATCH;
	public static final IntegerProperty EGGS = BlockStateProperties.EGGS;

	public GraboidEggBlock() {
		super(Block.Properties.of().strength(4.0F).sound(SoundType.STONE).randomTicks().noOcclusion());
		this.registerDefaultState((BlockState) ((BlockState) ((BlockState) this.stateDefinition.any()).setValue(HATCH, 0)).setValue(EGGS, 1));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Block.box(7, 0, 7, 9, 5, 9);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HATCH, EGGS);
	}

	@Override
	public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
		if (!entity.isSteppingCarefully())
			this.destroyEgg(level, blockState, blockPos, entity, 100);
		super.stepOn(level, blockPos, blockState, entity);
	}

	@Override
	public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, float f) {
		if (!(entity instanceof Zombie))
			this.destroyEgg(level, blockState, blockPos, entity, 3);
		super.fallOn(level, blockState, blockPos, entity, f);
	}

	protected void destroyEgg(Level level, BlockState blockState, BlockPos blockPos, Entity entity, int i) {
		if (!this.canDestroyEgg(level, entity))
			return;
		if (!level.isClientSide && level.random.nextInt(i) == 0 && blockState.is(ModBlocks.GRABOID_EGG))
			this.decreaseEggs(level, blockPos, blockState);
	}

	protected void decreaseEggs(Level level, BlockPos blockPos, BlockState blockState) {
		level.playSound(null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.2f, 0.9f + level.random.nextFloat() * 0.2f);
		var i = blockState.getValue(EGGS);
		if (i <= 1)
			level.destroyBlock(blockPos, false);
		else {
			level.setBlock(blockPos, (BlockState) blockState.setValue(EGGS, i - 1), 2);
			level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(blockState));
			level.levelEvent(2001, blockPos, Block.getId(blockState));
		}
	}

	protected boolean shouldUpdateHatchLevel(Level level) {
		var f = level.getTimeOfDay(1.0f);
		if ((double) f < 0.69 && (double) f > 0.65)
			return true;
		return level.random.nextInt(200) == 0;
	}

	@Override
	public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (this.shouldUpdateHatchLevel(serverLevel)) {
			var i = blockState.getValue(HATCH);
			if (i < 2) {
				serverLevel.playSound(null, blockPos, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.3f, 0.9f + randomSource.nextFloat() * 0.2f);
				serverLevel.setBlock(blockPos, (BlockState) blockState.setValue(HATCH, i + 1), 2);
			} else {
				serverLevel.playSound(null, blockPos, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.3f, 0.9f + randomSource.nextFloat() * 0.2f);
				serverLevel.removeBlock(blockPos, false);
				serverLevel.levelEvent(2001, blockPos, Block.getId(blockState));
				var dirtDragon = ModMobs.AMERICAN_DIRT_DRAGON.create(serverLevel);
				dirtDragon.setGrowth(0);
				dirtDragon.moveTo((double) blockPos.getX() + 0.3 + 0, blockPos.getY(), (double) blockPos.getZ() + 0.3, 0.0f, 0.0f);
				serverLevel.addFreshEntity(dirtDragon);
			}
		}
	}

	@Override
	public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
		super.playerDestroy(level, player, blockPos, blockState, blockEntity, itemStack);
		this.decreaseEggs(level, blockPos, blockState);
	}

	protected boolean canDestroyEgg(Level level, Entity entity) {
		if (entity instanceof BaseEntity || entity instanceof Bat)
			return false;
		if (entity instanceof LivingEntity)
			return entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
		return false;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!world.isClientSide) {
			world.destroyBlock(pos, true);
			if (!player.getMainHandItem().is(ItemTags.TOOLS))
				world.addFreshEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), ModItems.GRABOID_EGG_ITEM.getDefaultInstance()));
		}
		return InteractionResult.SUCCESS;
	}

}
