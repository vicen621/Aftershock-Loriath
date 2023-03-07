package mod.azure.aftershock.common.blocks;

import mod.azure.aftershock.common.AftershockMod.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SeismographBlock extends Block implements EntityBlock {

	public SeismographBlock() {
		super(Block.Properties.of(Material.METAL).strength(4.0F).sound(SoundType.METAL).noOcclusion());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Block.box(3, 0, 3, 13, 6, 13);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return ModMobs.SEIMOGRAPH.create(pos, state);
	}

}
