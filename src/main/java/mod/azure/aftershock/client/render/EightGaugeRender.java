package mod.azure.aftershock.client.render;

import mod.azure.aftershock.client.model.EightGaugeModel;
import mod.azure.aftershock.common.items.EightGaugeItem;
import mod.azure.azurelib.renderer.GeoItemRenderer;

public class EightGaugeRender extends GeoItemRenderer<EightGaugeItem> {
	public EightGaugeRender() {
		super(new EightGaugeModel());
	}
}
