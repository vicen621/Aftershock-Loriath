package mod.azure.aftershock.common;

import eu.midnightdust.lib.config.MidnightConfig;
import mod.azure.aftershock.common.blocks.GraboidEggBlock;
import mod.azure.aftershock.common.blocks.SeismographBlock;
import mod.azure.aftershock.common.blocks.SeismographBlockEntity;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.entities.AmericanBlasterEntity;
import mod.azure.aftershock.common.entities.AmericanGraboidEntity;
import mod.azure.aftershock.common.entities.AmericanShreikerEntity;
import mod.azure.aftershock.common.entities.sensors.AftershockMemoryTypes;
import mod.azure.aftershock.common.entities.sensors.AftershockSensors;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.aftershock.common.items.SeismographBlockItem;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.items.AzureSpawnEgg;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

@SuppressWarnings("static-access")
public class AftershockMod implements ModInitializer {

	public static final String MODID = "aftershock";
	public static ModItems ITEMS;
	public static ModMobs ModMobs;
	public static ModBlocks ModBlocks;
	public static final TagKey<Block> DESTRUCTIBLE_LIGHT = TagKey.create(Registries.BLOCK, AftershockMod.modResource("destructible_light"));
	public static final TagKey<Block> WEAK_BLOCKS = TagKey.create(Registries.BLOCK, AftershockMod.modResource("weak_block"));
	public static final TagKey<EntityType<?>> HEAT_ENTITY = TagKey.create(Registries.ENTITY_TYPE, AftershockMod.modResource("heat_entity"));
	public static final EntityDataSerializer<AttackType> ALIEN_ATTACK_TYPE = new EntityDataSerializer<>() {
		@Override
		public void write(FriendlyByteBuf packetByteBuf, AttackType AttackType) {
			packetByteBuf.writeEnum(AttackType);
		}

		@Override
		public AttackType read(FriendlyByteBuf packetByteBuf) {
			return packetByteBuf.readEnum(AttackType.class);
		}

		@Override
		public AttackType copy(AttackType AttackType) {
			return AttackType;
		}
	};
	public static final CreativeModeTab ITEMS_GROUP = FabricItemGroup.builder(AftershockMod.modResource("items")).icon(() -> new ItemStack(ModItems.SEIMOGRAPH_ITEM)).displayItems((context, entries) -> {
		entries.accept(ModItems.GRABOID_EGG_ITEM);
		entries.accept(ModItems.SEIMOGRAPH_ITEM);
		// entries.accept(ModItems.DIRT_DRAGON_SPAWNEGG);
		entries.accept(ModItems.AMERICAN_GRABOID_SPAWNEGG);
		entries.accept(ModItems.AMERICAN_SHREIKER_SPAWNEGG);
		entries.accept(ModItems.AMERICAN_BLASTER_SPAWNEGG);
	}).build();

	@Override
	public void onInitialize() {
		MidnightConfig.init(MODID, AfterShocksConfig.class);
		EntityDataSerializers.registerSerializer(ALIEN_ATTACK_TYPE);
		AftershockSensors.init();
		AftershockMemoryTypes.init();
		ITEMS = new ModItems();
		ModMobs = new ModMobs();
		ModBlocks = new ModBlocks();
		ModMobs.init();
		AzureLib.initialize();
	}

	public static final ResourceLocation modResource(String name) {
		return new ResourceLocation(AftershockMod.MODID, name);
	}

	public class ModMobs {
		public final static EntityType<AmericanBlasterEntity> AMERICAN_BLASTER = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_blaster"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanBlasterEntity::new).dimensions(EntityDimensions.fixed(1.3f, 1.15F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

		public final static EntityType<AmericanShreikerEntity> AMERICAN_SHREIKER = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_shreiker"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanShreikerEntity::new).dimensions(EntityDimensions.fixed(1.0f, 1.0F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

		public final static EntityType<AmericanGraboidEntity> AMERICAN_GRABOID = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_graboid"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanGraboidEntity::new).dimensions(EntityDimensions.fixed(2.0f, 1.8F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

//		public final static EntityType<DirtDragonEntity> DIRT_DRAGON = Registry.register(
//				BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("dirt_dragon"),
//				FabricEntityTypeBuilder.create(MobCategory.MONSTER, DirtDragonEntity::new)
//						.dimensions(EntityDimensions.fixed(1.0f, 1.0F)).trackRangeBlocks(90).trackedUpdateRate(1)
//						.build());

		public static final BlockEntityType<SeismographBlockEntity> SEIMOGRAPH = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AftershockMod.MODID + ":seismograph", FabricBlockEntityTypeBuilder.create(SeismographBlockEntity::new, ModBlocks.SEIMOGRAPH).build(null));

		public void init() {
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_BLASTER, AmericanBlasterEntity.createMobAttributes());
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_SHREIKER, AmericanShreikerEntity.createMobAttributes());
//			FabricDefaultAttributeRegistry.register(ModMobs.DIRT_DRAGON, DirtDragonEntity.createMobAttributes());
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_GRABOID, AmericanGraboidEntity.createMobAttributes());
		}
	}

	public class ModBlocks {
		public final static Block GRABOID_EGG = block(new GraboidEggBlock(), "graboid_egg");
		public final static Block SEIMOGRAPH = block(new SeismographBlock(), "seismograph");

		static <T extends Block> T block(T c, String id) {
			Registry.register(BuiltInRegistries.BLOCK, AftershockMod.modResource(id), c);
			return c;
		}

	}

	public class ModItems {
		public static AzureSpawnEgg AMERICAN_BLASTER_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_BLASTER, 0x927452, 0x213744), "american_blaster_spawnegg");
		public static AzureSpawnEgg AMERICAN_SHREIKER_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_SHREIKER, 0x7c5c51, 0xe7ded4), "american_shreiker_spawnegg");
		public static AzureSpawnEgg AMERICAN_GRABOID_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_GRABOID, 0x2d383d, 0xa2967f), "american_graboid_spawnegg");
//		public static AzureSpawnEgg DIRT_DRAGON_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.DIRT_DRAGON, 0x9e6025, 0x939393), "dirt_dragon_spawnegg");

		public static BlockItem GRABOID_EGG_ITEM = item(new BlockItem(ModBlocks.GRABOID_EGG, new Item.Properties()), "graboid_egg");
		public static SeismographBlockItem SEIMOGRAPH_ITEM = item(new SeismographBlockItem(), "seismograph");

		static <T extends Item> T item(T c, String id) {
			Registry.register(BuiltInRegistries.ITEM, AftershockMod.modResource(id), c);
			return c;
		}
	}
}