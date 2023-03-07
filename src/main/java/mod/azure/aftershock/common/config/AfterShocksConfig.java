package mod.azure.aftershock.common.config;

import eu.midnightdust.lib.config.MidnightConfig;

public class AfterShocksConfig extends MidnightConfig {

	@Entry
	public static double dirtdevil_health = 12;
	@Entry
	public static int dirtdevil_exp = 2;
	@Entry
	public static int dirtdevil_damage = 3;
	@Entry
	public static int dirtdevil_ram_damage = 8;
	
	@Entry
	public static double americangraboid_health = 80;
	@Entry
	public static int americangraboid_armor = 5;
	@Entry
	public static int americangraboid_exp = 10;
	@Entry
	public static int americangraboid_damage = 6;
	
	@Entry
	public static double americanshreiker_health = 20;
	@Entry
	public static int americanshreiker_exp = 4;
	@Entry
	public static int americanshreiker_damage = 6;
	
	@Entry
	public static double americanblaster_health = 40;
	@Entry
	public static int americanblaster_exp = 8;
	@Entry
	public static int americanblaster_damage = 8;
}
