package mod.azure.aftershock.common.blocks;

import net.minecraft.world.level.block.state.properties.EnumProperty;

public class SeismographProperties {

	public static final EnumProperty<SeismographStates> STORAGE_STATE = EnumProperty.create("seismograph_state", SeismographStates.class);
}
