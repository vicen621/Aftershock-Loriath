package mod.azure.aftershock.client.model;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.items.SeismographBlockItem;
import mod.azure.azurelib.model.DefaultedBlockGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SeismographItemModel extends DefaultedBlockGeoModel<SeismographBlockItem> {

	public SeismographItemModel() {
		super(AftershockMod.modResource("seismograph/seismograph"));
	}

}
