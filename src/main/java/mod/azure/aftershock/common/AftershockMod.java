package mod.azure.aftershock.common;

import java.util.LinkedList;
import java.util.List;

import com.mojang.serialization.Codec;

import mod.azure.aftershock.common.blocks.GraboidEggBlock;
import mod.azure.aftershock.common.blocks.SeismographBlock;
import mod.azure.aftershock.common.blocks.SeismographBlockEntity;
import mod.azure.aftershock.common.config.AfterShocksConfig;
import mod.azure.aftershock.common.entities.american.AmericanBlasterEntity;
import mod.azure.aftershock.common.entities.american.AmericanDirtDragonEntity;
import mod.azure.aftershock.common.entities.american.AmericanGraboidEntity;
import mod.azure.aftershock.common.entities.american.AmericanShreikerEntity;
import mod.azure.aftershock.common.entities.projectiles.ShellEntity;
import mod.azure.aftershock.common.entities.sensors.AftershockMemoryTypes;
import mod.azure.aftershock.common.entities.sensors.AftershockSensors;
import mod.azure.aftershock.common.helpers.AttackType;
import mod.azure.aftershock.common.items.EightGaugeItem;
import mod.azure.aftershock.common.items.GraboidEggBlockItem;
import mod.azure.aftershock.common.items.PortableSeismographItem;
import mod.azure.aftershock.common.items.SeismographBlockItem;
import mod.azure.aftershock.common.structures.GraniteEggStructure;
import mod.azure.aftershock.common.structures.RedSandEggStructure;
import mod.azure.aftershock.common.structures.SandEggStructure;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.items.AzureSpawnEgg;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class AftershockMod implements ModInitializer {

	public static AfterShocksConfig config;
	public static final String MODID = "aftershock";
	public static final TagKey<Block> DESTRUCTIBLE_LIGHT = TagKey.create(Registries.BLOCK, AftershockMod.modResource("destructible_light"));
	public static final TagKey<Block> WEAK_BLOCKS = TagKey.create(Registries.BLOCK, AftershockMod.modResource("weak_block"));
	public static final TagKey<EntityType<?>> HEAT_ENTITY = TagKey.create(Registries.ENTITY_TYPE, AftershockMod.modResource("heat_entity"));
	public static final ResourceLocation SHOTGUN = new ResourceLocation(MODID, "shotgun");
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
	public static final ResourceKey<CreativeModeTab> ITEMS_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, AftershockMod.modResource("items"));

	@Override
	public void onInitialize() {
		config = AzureLibMod.registerConfig(AfterShocksConfig.class, ConfigFormats.json()).getConfigInstance();
		EntityDataSerializers.registerSerializer(ALIEN_ATTACK_TYPE);
		AftershockSensors.initialize();
		AftershockMemoryTypes.initialize();
		ModSounds.initialize();
		ModItems.initialize();
		ModBlocks.initialize();
		ModMobs.initialize();
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEMS_GROUP, FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.SEIMOGRAPH_ITEM)) // icon
				.title(Component.translatable("itemGroup.aftershock.items")) // title
				.displayItems((context, entries) -> {
					entries.accept(ModItems.GRABOID_EGG_ITEM);
					entries.accept(ModItems.SEIMOGRAPH_ITEM);
					entries.accept(ModItems.PORTABLE_SEIMOGRAPH_ITEM);
					entries.accept(ModItems.AMERICAN_DIRT_DRAGON_SPAWNEGG);
					entries.accept(ModItems.AMERICAN_GRABOID_SPAWNEGG);
					entries.accept(ModItems.AMERICAN_SHREIKER_SPAWNEGG);
					entries.accept(ModItems.AMERICAN_BLASTER_SPAWNEGG);
//					entries.accept(ModItems.TROPICAL_DIRT_DRAGON_SPAWNEGG);
//					entries.accept(ModItems.TROPICAL_GRABOID_SPAWNEGG);
//					entries.accept(ModItems.TROPICAL_SHREIKER_SPAWNEGG);
//					entries.accept(ModItems.TROPICAL_BLASTER_SPAWNEGG);
					entries.accept(ModItems.EIGHTGAUGE);
					entries.accept(ModItems.SHOTGUN_SHELL);
				}).build()); // build() no longer registers by itself
		ModStructures.registerStructureFeatures();
		AzureLib.initialize();
		PacketHandler.registerMessages();
	}

	public static final ResourceLocation modResource(String name) {
		return new ResourceLocation(AftershockMod.MODID, name);
	}

	public class ModMobs {

		public static List<EntityType<? extends Entity>> ENTITY_TYPES = new LinkedList();
		public static List<EntityType<? extends Entity>> ENTITY_THAT_USE_ITEM_RENDERS = new LinkedList();

		public final static EntityType<AmericanBlasterEntity> AMERICAN_BLASTER = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_blaster"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanBlasterEntity::new).dimensions(EntityDimensions.scalable(1.3f, 1.15F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

		public final static EntityType<AmericanShreikerEntity> AMERICAN_SHREIKER = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_shreiker"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanShreikerEntity::new).dimensions(EntityDimensions.scalable(1.0f, 1.0F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

		public final static EntityType<AmericanGraboidEntity> AMERICAN_GRABOID = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_graboid"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanGraboidEntity::new).dimensions(EntityDimensions.scalable(2.0f, 1.8F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

		public final static EntityType<AmericanDirtDragonEntity> AMERICAN_DIRT_DRAGON = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("american_dirt_dragon"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, AmericanDirtDragonEntity::new).dimensions(EntityDimensions.scalable(0.9f, 0.3F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

//		public final static EntityType<TropicalBlasterEntity> TROPICAL_BLASTER = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("tropical_blaster"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, TropicalBlasterEntity::new).dimensions(EntityDimensions.scalable(1.3f, 1.15F)).trackRangeBlocks(90).trackedUpdateRate(1).build());
//
//		public final static EntityType<TropicalShreikerEntity> TROPICAL_SHREIKER = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("tropical_shreiker"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, TropicalShreikerEntity::new).dimensions(EntityDimensions.scalable(1.0f, 1.0F)).trackRangeBlocks(90).trackedUpdateRate(1).build());
//
//		public final static EntityType<TropicalGraboidEntity> TROPICAL_GRABOID = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("tropical_graboid"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, TropicalGraboidEntity::new).dimensions(EntityDimensions.scalable(2.0f, 1.8F)).trackRangeBlocks(90).trackedUpdateRate(1).build());
//
//		public final static EntityType<TropicalGraboidEntity> TROPICAL_DIRT_DRAGON = Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource("tropical_dirt_dragon"), FabricEntityTypeBuilder.create(MobCategory.MONSTER, TropicalGraboidEntity::new).dimensions(EntityDimensions.scalable(0.9f, 0.3F)).trackRangeBlocks(90).trackedUpdateRate(1).build());

		public static final BlockEntityType<SeismographBlockEntity> SEIMOGRAPH = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AftershockMod.MODID + ":seismograph", FabricBlockEntityTypeBuilder.create(SeismographBlockEntity::new, ModBlocks.SEIMOGRAPH).build(null));

		public static EntityType<ShellEntity> SHELL = projectile(ShellEntity::new, "shell");

		private static <T extends Entity> EntityType<T> projectile(EntityType.EntityFactory<T> factory, String id) {
			EntityType<T> type = FabricEntityTypeBuilder.<T>create(MobCategory.MISC, factory).dimensions(new EntityDimensions(0.5F, 0.5F, true)).disableSummon().spawnableFarFromPlayer().trackRangeBlocks(90).trackedUpdateRate(1).build();

			Registry.register(BuiltInRegistries.ENTITY_TYPE, AftershockMod.modResource(id), type);

			ENTITY_TYPES.add(type);
			ENTITY_THAT_USE_ITEM_RENDERS.add(type);

			return type;
		}

		public static void initialize() {
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_BLASTER, AmericanBlasterEntity.createMobAttributes());
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_SHREIKER, AmericanShreikerEntity.createMobAttributes());
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_DIRT_DRAGON, AmericanDirtDragonEntity.createMobAttributes());
			FabricDefaultAttributeRegistry.register(ModMobs.AMERICAN_GRABOID, AmericanGraboidEntity.createMobAttributes());
//			FabricDefaultAttributeRegistry.register(ModMobs.TROPICAL_BLASTER, TropicalBlasterEntity.createMobAttributes());
//			FabricDefaultAttributeRegistry.register(ModMobs.TROPICAL_SHREIKER, TropicalShreikerEntity.createMobAttributes());
//			FabricDefaultAttributeRegistry.register(ModMobs.TROPICAL_DIRT_DRAGON, TropicalDirtDragonEntity.createMobAttributes());
//			FabricDefaultAttributeRegistry.register(ModMobs.TROPICAL_GRABOID, TropicalGraboidEntity.createMobAttributes());
		}
	}

	public class ModBlocks {
		public final static Block GRABOID_EGG = block(new GraboidEggBlock(), "graboid_egg");
//		public final static Block TROPICAL_GRABOID_EGG = block(new TropicalGraboidEggBlock(), "tropical_graboid_egg");
		public final static Block SEIMOGRAPH = block(new SeismographBlock(), "seismograph");

		static <T extends Block> T block(T c, String id) {
			Registry.register(BuiltInRegistries.BLOCK, AftershockMod.modResource(id), c);
			return c;
		}

		public static void initialize() {
		}
	}

	public class ModSounds {

		public static SoundEvent SHOTGUN = of("aftershock.shotgun");
		public static SoundEvent SHOTGUNRELOAD = of("aftershock.shotgunreload");
		public static SoundEvent GRABOID_ATTACK = of("aftershock.graboid_attacking");
		public static SoundEvent GRABOID_DYING = of("aftershock.graboid_dying");
		public static SoundEvent GRABOID_IDLE = of("aftershock.graboid_idle");
		public static SoundEvent GRABOID_MOVING = of("aftershock.graboid_moving");

		static SoundEvent of(String id) {
			SoundEvent sound = SoundEvent.createVariableRangeEvent(new ResourceLocation(AftershockMod.MODID, id));
			Registry.register(BuiltInRegistries.SOUND_EVENT, new ResourceLocation(AftershockMod.MODID, id), sound);
			return sound;
		}

		public static void initialize() {
		}
	}

	public class ModItems {
		public static AzureSpawnEgg AMERICAN_BLASTER_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_BLASTER, 0x927452, 0x213744), "american_blaster_spawnegg");
		public static AzureSpawnEgg AMERICAN_SHREIKER_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_SHREIKER, 0x7c5c51, 0xe7ded4), "american_shreiker_spawnegg");
		public static AzureSpawnEgg AMERICAN_GRABOID_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_GRABOID, 0x4d4d4d, 0x7e6d50), "american_graboid_spawnegg");
		public static AzureSpawnEgg AMERICAN_DIRT_DRAGON_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.AMERICAN_DIRT_DRAGON, 0xf2e7b0, 0x9e6025), "american_dirt_dragon_spawnegg");

//		public static AzureSpawnEgg TROPICAL_BLASTER_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.TROPICAL_BLASTER, 0x927452, 0x213744), "tropical_blaster_spawnegg");
//		public static AzureSpawnEgg TROPICAL_SHREIKER_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.TROPICAL_SHREIKER, 0x7c5c51, 0xe7ded4), "tropical_shreiker_spawnegg");
//		public static AzureSpawnEgg TROPICAL_GRABOID_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.TROPICAL_GRABOID, 0x4d4d4d, 0x7e6d50), "tropical_graboid_spawnegg");
//		public static AzureSpawnEgg TROPICAL_DIRT_DRAGON_SPAWNEGG = item(new AzureSpawnEgg(ModMobs.TROPICAL_DIRT_DRAGON, 0xf2e7b0, 0x9e6025), "tropical_dirt_dragon_spawnegg");

		public static GraboidEggBlockItem GRABOID_EGG_ITEM = item(new GraboidEggBlockItem(ModBlocks.GRABOID_EGG), "graboid_egg");
//		public static GraboidEggBlockItem TROPICAL_GRABOID_EGG_ITEM = item(new GraboidEggBlockItem(ModBlocks.TROPICAL_GRABOID_EGG), "tropical_graboid_egg");
		public static SeismographBlockItem SEIMOGRAPH_ITEM = item(new SeismographBlockItem(), "seismograph");
		public static PortableSeismographItem PORTABLE_SEIMOGRAPH_ITEM = item(new PortableSeismographItem(), "portable_seismograph");

		public static Item SHOTGUN_SHELL = item(new Item(new Item.Properties()), "shotgun_shell");
		public static EightGaugeItem EIGHTGAUGE = item(new EightGaugeItem(), "eightgauge");

		static <T extends Item> T item(T c, String id) {
			Registry.register(BuiltInRegistries.ITEM, AftershockMod.modResource(id), c);
			return c;
		}

		public static void initialize() {
		}
	}

	public class ModStructures {

		public static StructureType<?> SANDEGG_STRUCTURE;
		public static StructureType<?> REDSANDEGG_STRUCTURE;
		public static StructureType<?> GRANITEEGG_STRUCTURE;
//		public static StructureType<?> TROPICALEGG_STRUCTURE;

		public static void registerStructureFeatures() {
			SANDEGG_STRUCTURE = register(AftershockMod.modResource("sand_egg"), SandEggStructure.CODEC);
			REDSANDEGG_STRUCTURE = register(AftershockMod.modResource("redsand_egg"), RedSandEggStructure.CODEC);
			GRANITEEGG_STRUCTURE = register(AftershockMod.modResource("granite_egg"), GraniteEggStructure.CODEC);
//			TROPICALEGG_STRUCTURE = register(AftershockMod.modResource("tropical_egg"), TropicalEggStructure.CODEC);
		}

		private static <S extends Structure> StructureType<S> register(ResourceLocation id, Codec<S> codec) {
			return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, id, () -> codec);
		}
	}

	public class PacketHandler {

		public static void registerMessages() {
			ServerPlayNetworking.registerGlobalReceiver(AftershockMod.SHOTGUN, (server, player, serverPlayNetworkHandler, inputPacket, packetSender) -> {
				if (player.getMainHandItem().getItem() instanceof EightGaugeItem)
					((EightGaugeItem) player.getMainHandItem().getItem()).reload(player, InteractionHand.MAIN_HAND);
			});
		}
	}
}