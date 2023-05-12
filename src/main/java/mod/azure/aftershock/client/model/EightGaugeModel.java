package mod.azure.aftershock.client.model;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.items.EightGaugeItem;
import mod.azure.azurelib.model.DefaultedItemGeoModel;

public class EightGaugeModel extends DefaultedItemGeoModel<EightGaugeItem> {

	public EightGaugeModel() {
		super(AftershockMod.modResource("eightgauge/eightgauge"));
	}

}