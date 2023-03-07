package mod.azure.aftershock.client.render;

import mod.azure.aftershock.client.model.SeismographModel;
import mod.azure.aftershock.common.blocks.SeismographBlockEntity;
import mod.azure.azurelib.renderer.GeoBlockRenderer;

public class SeismographBlockRenderer extends GeoBlockRenderer<SeismographBlockEntity> {
	public SeismographBlockRenderer() {
		super(new SeismographModel());
	}
}