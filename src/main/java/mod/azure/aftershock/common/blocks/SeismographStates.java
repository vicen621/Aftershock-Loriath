package mod.azure.aftershock.common.blocks;

import net.minecraft.util.StringRepresentable;

public enum SeismographStates implements StringRepresentable {
	OPENED("opened"), CLOSED("closed");

	private final String name;

	private SeismographStates(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

}
