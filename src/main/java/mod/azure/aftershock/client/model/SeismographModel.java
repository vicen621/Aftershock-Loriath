package mod.azure.aftershock.client.model;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.aftershock.common.blocks.SeismographBlockEntity;
import mod.azure.azurelib.model.DefaultedBlockGeoModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SeismographModel extends DefaultedBlockGeoModel<SeismographBlockEntity> {

	public SeismographModel() {
		super(AftershockMod.modResource("seismograph/seismograph"));
	}

}
