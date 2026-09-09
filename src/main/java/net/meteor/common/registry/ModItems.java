package net.meteor.common.registry;

import net.meteor.common.MeteorType;
import net.meteor.common.MeteorsMod;
import net.meteor.common.item.MeteorSummonerItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.function.Function;

/**
 * Registro de items "simples" del mod: materiales en bruto, lingotes y comida.
 *
 * Desde 1.21.2 cada item necesita su {@link ResourceKey} guardado en las
 * {@link Item.Properties} mediante {@code setId(...)}, si no el juego no arranca.
 */
public final class ModItems {

	private ModItems() {}

	// --- Materiales en bruto ---
	public static final Item METEOR_CHIP = register("meteor_chip",
			Item::new, new Item.Properties());
	public static final Item FREZARITE_CRYSTAL = register("frezarite_crystal",
			Item::new, new Item.Properties());
	public static final Item KREKNORITE_CHIP = register("kreknorite_chip",
			Item::new, new Item.Properties());
	public static final Item RED_METEOR_GEM = register("red_meteor_gem",
			Item::new, new Item.Properties());

	// --- Detectores de meteoros ---
	public static final Item METEOR_DETECTOR_PROXIMITY = register("meteor_detector_proximity",
			p -> new net.meteor.common.item.DetectorItem(p, net.meteor.common.item.DetectorItem.Mode.PROXIMITY),
			new Item.Properties().stacksTo(1));
	public static final Item METEOR_DETECTOR_TIME = register("meteor_detector_time",
			p -> new net.meteor.common.item.DetectorItem(p, net.meteor.common.item.DetectorItem.Mode.TIME),
			new Item.Properties().stacksTo(1));
	public static final Item METEOR_DETECTOR_CRASH = register("meteor_detector_crash",
			p -> new net.meteor.common.item.DetectorItem(p, net.meteor.common.item.DetectorItem.Mode.CRASH),
			new Item.Properties().stacksTo(1));

	// --- Lingotes procesados ---
	public static final Item METEORITE_INGOT = register("meteorite_ingot",
			Item::new, new Item.Properties());
	public static final Item FROZEN_IRON = register("frozen_iron",
			Item::new, new Item.Properties());
	public static final Item KREKNORITE_INGOT = register("kreknorite_ingot",
			Item::new, new Item.Properties());

	// --- Comida ---
	public static final Item VANILLA_ICE_CREAM = register("vanilla_ice_cream",
			Item::new, new Item.Properties().food(
					new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).build()));
	public static final Item CHOCOLATE_ICE_CREAM = register("chocolate_ice_cream",
			Item::new, new Item.Properties().food(
					new FoodProperties.Builder().nutrition(6).saturationModifier(0.4f).build()));

	// === HERRAMIENTAS ===
	// Valores (daño, velocidad) estándar por tipo de herramienta; fáciles de ajustar.
	// Meteorite
	public static final Item METEORITE_SWORD = register("meteorite_sword",
			Item::new, new Item.Properties().sword(ModToolMaterials.METEORITE, 3f, -2.4f));
	public static final Item METEORITE_PICKAXE = register("meteorite_pickaxe",
			Item::new, new Item.Properties().pickaxe(ModToolMaterials.METEORITE, 1f, -2.8f));
	public static final Item METEORITE_AXE = register("meteorite_axe",
			Item::new, new Item.Properties().axe(ModToolMaterials.METEORITE, 6f, -3.0f));
	public static final Item METEORITE_SHOVEL = register("meteorite_shovel",
			Item::new, new Item.Properties().shovel(ModToolMaterials.METEORITE, 1.5f, -3.0f));
	public static final Item METEORITE_HOE = register("meteorite_hoe",
			Item::new, new Item.Properties().hoe(ModToolMaterials.METEORITE, 0f, -3.0f));
	// Frezarite
	public static final Item FREZARITE_SWORD = register("frezarite_sword",
			Item::new, new Item.Properties().sword(ModToolMaterials.FREZARITE, 3f, -2.4f));
	public static final Item FREZARITE_PICKAXE = register("frezarite_pickaxe",
			Item::new, new Item.Properties().pickaxe(ModToolMaterials.FREZARITE, 1f, -2.8f));
	public static final Item FREZARITE_AXE = register("frezarite_axe",
			Item::new, new Item.Properties().axe(ModToolMaterials.FREZARITE, 6f, -3.0f));
	public static final Item FREZARITE_SHOVEL = register("frezarite_shovel",
			Item::new, new Item.Properties().shovel(ModToolMaterials.FREZARITE, 1.5f, -3.0f));
	public static final Item FREZARITE_HOE = register("frezarite_hoe",
			net.meteor.common.item.ColdHoeItem::new, new Item.Properties().hoe(ModToolMaterials.FREZARITE, 0f, -3.0f));
	// Kreknorite (en el original era una espada de fuego con material de Meteorito;
	// el efecto de incendiar al enemigo se implementará en una fase posterior).
	public static final Item KREKNORITE_SWORD = register("kreknorite_sword",
			Item::new, new Item.Properties().sword(ModToolMaterials.METEORITE, 5f, -2.4f));

	// === MISC ===
	public static final Item METEOR_SUMMONER = register("meteor_summoner",
			props -> new MeteorSummonerItem(props, null), new Item.Properties().stacksTo(16));
	public static final Item METEORITE_SUMMONER = register("meteorite_summoner",
			props -> new MeteorSummonerItem(props, MeteorType.METEORITE), new Item.Properties().stacksTo(16));
	public static final Item FREZARITE_SUMMONER = register("frezarite_summoner",
			props -> new MeteorSummonerItem(props, MeteorType.FREZARITE), new Item.Properties().stacksTo(16));
	public static final Item KREKNORITE_SUMMONER = register("kreknorite_summoner",
			props -> new MeteorSummonerItem(props, MeteorType.KREKNORITE), new Item.Properties().stacksTo(16));
	public static final Item KITTY_SUMMONER = register("kitty_summoner",
			props -> new MeteorSummonerItem(props, MeteorType.KITTY), new Item.Properties().stacksTo(16));
	public static final Item UNKNOWN_SUMMONER = register("unknown_summoner",
			props -> new MeteorSummonerItem(props, MeteorType.UNKNOWN), new Item.Properties().stacksTo(16));
	public static final Item MAGNETIC_FIELD_DISRUPTOR = register("magnetic_field_disruptor",
			Item::new, new Item.Properties());
	public static final Item MAGNETIZATION_CONTROLLER = register("magnetization_controller",
			net.meteor.common.item.MagnetizationControllerItem::new, new Item.Properties().stacksTo(1));

	// === ARMADURAS ===
	// Meteorite
	public static final Item METEORITE_HELMET = registerArmor("meteorite_helmet", ModArmorMaterials.METEORITE, ArmorType.HELMET, ModArmorMaterials.METEORITE_DURABILITY);
	public static final Item METEORITE_CHESTPLATE = registerArmor("meteorite_chestplate", ModArmorMaterials.METEORITE, ArmorType.CHESTPLATE, ModArmorMaterials.METEORITE_DURABILITY);
	public static final Item METEORITE_LEGGINGS = registerArmor("meteorite_leggings", ModArmorMaterials.METEORITE, ArmorType.LEGGINGS, ModArmorMaterials.METEORITE_DURABILITY);
	public static final Item METEORITE_BOOTS = registerArmor("meteorite_boots", ModArmorMaterials.METEORITE, ArmorType.BOOTS, ModArmorMaterials.METEORITE_DURABILITY);
	// Frezarite
	public static final Item FREZARITE_HELMET = registerArmor("frezarite_helmet", ModArmorMaterials.FREZARITE, ArmorType.HELMET, ModArmorMaterials.FREZARITE_DURABILITY);
	public static final Item FREZARITE_CHESTPLATE = registerArmor("frezarite_chestplate", ModArmorMaterials.FREZARITE, ArmorType.CHESTPLATE, ModArmorMaterials.FREZARITE_DURABILITY);
	public static final Item FREZARITE_LEGGINGS = registerArmor("frezarite_leggings", ModArmorMaterials.FREZARITE, ArmorType.LEGGINGS, ModArmorMaterials.FREZARITE_DURABILITY);
	public static final Item FREZARITE_BOOTS = registerArmor("frezarite_boots", ModArmorMaterials.FREZARITE, ArmorType.BOOTS, ModArmorMaterials.FREZARITE_DURABILITY);
	// Kreknorite
	public static final Item KREKNORITE_HELMET = registerArmor("kreknorite_helmet", ModArmorMaterials.KREKNORITE, ArmorType.HELMET, ModArmorMaterials.KREKNORITE_DURABILITY);
	public static final Item KREKNORITE_CHESTPLATE = registerArmor("kreknorite_chestplate", ModArmorMaterials.KREKNORITE, ArmorType.CHESTPLATE, ModArmorMaterials.KREKNORITE_DURABILITY);
	public static final Item KREKNORITE_LEGGINGS = registerArmor("kreknorite_leggings", ModArmorMaterials.KREKNORITE, ArmorType.LEGGINGS, ModArmorMaterials.KREKNORITE_DURABILITY);
	public static final Item KREKNORITE_BOOTS = registerArmor("kreknorite_boots", ModArmorMaterials.KREKNORITE, ArmorType.BOOTS, ModArmorMaterials.KREKNORITE_DURABILITY);

	/** Helper de registro de una pieza de armadura humanoide. */
	private static Item registerArmor(String name, net.minecraft.world.item.equipment.ArmorMaterial material, ArmorType type, int baseDurability) {
		return register(name, Item::new,
				new Item.Properties().humanoidArmor(material, type).durability(type.getDurability(baseDurability)));
	}

	/** Helper genérico de registro de items para 1.21.2+. */
	public static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, MeteorsMod.id(name));
		T item = factory.apply(props.setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	/** Fuerza la carga de la clase (sus campos estáticos hacen el registro). */
	public static void register() {
		MeteorsMod.LOGGER.debug("Registrando items de Falling Meteors");
	}
}
