package mod.azure.aftershock.client.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import eu.midnightdust.lib.config.MidnightConfig;
import mod.azure.aftershock.common.AftershockMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ModMenuAftershock implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> MidnightConfig.getScreen(parent, AftershockMod.MODID);
	}
}
