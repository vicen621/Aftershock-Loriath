package mod.azure.aftershock.common.entities.sensors;

import java.util.function.Supplier;

import net.minecraft.world.entity.ai.sensing.SensorType;
import net.tslat.smartbrainlib.SBLConstants;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;

public final class AftershockSensors {

	public static void initialize() {
	}

	public static final Supplier<SensorType<NearbyLightsBlocksSensor<?>>> NEARBY_LIGHT_BLOCKS = register("nearby_light_blocks_aftershock", NearbyLightsBlocksSensor::new);

	public static final Supplier<SensorType<ItemEntitySensor<?>>> FOOD_ITEMS = register("food_items_aftershock", ItemEntitySensor::new);

	private static <T extends ExtendedSensor<?>> Supplier<SensorType<T>> register(String id, Supplier<T> sensor) {
		return SBLConstants.SBL_LOADER.registerSensorType(id, sensor);
	}
}
