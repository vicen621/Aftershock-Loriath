package mod.azure.aftershock.common.entities.sensors;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.tslat.smartbrainlib.SBLConstants;

public class AftershockMemoryTypes {

	public static void init() {
	}

	public static final Supplier<MemoryModuleType<List<Pair<BlockPos, BlockState>>>> NEARBY_LIGHT_BLOCKS = register("nearby_light_blocks_aftershock");

	public static final Supplier<MemoryModuleType<List<ItemEntity>>> FOOD_ITEMS = register("food_items_aftershock");

	private static <T> Supplier<MemoryModuleType<T>> register(String id) {
		return register(id, null);
	}

	private static <T> Supplier<MemoryModuleType<T>> register(String id, @Nullable Codec<T> codec) {
		return SBLConstants.SBL_LOADER.registerMemoryType(id, codec);
	}

}
