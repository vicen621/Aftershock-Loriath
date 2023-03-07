package mod.azure.aftershock.client.render;

import mod.azure.aftershock.client.model.SeismographItemModel;
import mod.azure.aftershock.common.items.SeismographBlockItem;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class SeismographItemRender extends GeoItemRenderer<SeismographBlockItem> {

	public SeismographItemRender() {
		super(new SeismographItemModel());
	}

}