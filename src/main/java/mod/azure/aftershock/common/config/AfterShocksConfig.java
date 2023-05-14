package mod.azure.aftershock.common.config;

import mod.azure.aftershock.common.AftershockMod;
import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.Configurable;

@Config(id = AftershockMod.MODID)
public class AfterShocksConfig {

	@Configurable
	public float shotgun_damage = 25F;

	@Configurable
	public double americandirtdevil_health = 12;
	@Configurable
	public int americandirtdevil_exp = 2;
	@Configurable
	public int americandirtdevil_damage = 3;
	@Configurable
	public int americandirtdevil_ram_damage = 8;

	@Configurable
	public double americangraboid_health = 80;
	@Configurable
	public int americangraboid_armor = 5;
	@Configurable
	public int americangraboid_exp = 10;
	@Configurable
	public int americangraboid_damage = 6;

	@Configurable
	public double americanshreiker_health = 20;
	@Configurable
	public int americanshreiker_exp = 4;
	@Configurable
	public int americanshreiker_damage = 6;

	@Configurable
	public double americanblaster_health = 40;
	@Configurable
	public int americanblaster_exp = 8;
	@Configurable
	public int americanblaster_damage = 8;

//	@Configurable
//	public double tropicaldirtdevil_health = 12;
//	@Configurable
//	public int tropicaldirtdevil_exp = 2;
//	@Configurable
//	public int tropicaldirtdevil_damage = 3;
//	@Configurable
//	public int tropicaldirtdevil_ram_damage = 8;

//	@Configurable
//	public double tropicalgraboid_health = 100;
//	@Configurable
//	public int tropicalgraboid_armor = 5;
//	@Configurable
//	public int tropicalgraboid_exp = 10;
//	@Configurable
//	public int tropicalgraboid_damage = 6;

//	@Configurable
//	public double tropicalshreiker_health = 25;
//	@Configurable
//	public int tropicalshreiker_armor = 2;
//	@Configurable
//	public int tropicalshreiker_exp = 4;
//	@Configurable
//	public int tropicalshreiker_damage = 6;

//	@Configurable
//	public double tropicalblaster_health = 45;
//	@Configurable
//	public int tropicalblaster_exp = 8;
//	@Configurable
//	public int tropicalblaster_damage = 8;
}
