package mod.azure.aftershock.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GraboidEggBlock extends Block {

	public GraboidEggBlock() {
		super(Block.Properties.of(Material.STONE).strength(4.0F).sound(SoundType.STONE).noOcclusion());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Block.box(7, 0, 7, 9, 5, 9);
	}

}
