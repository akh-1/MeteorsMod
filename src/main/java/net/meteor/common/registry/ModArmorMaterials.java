package net.meteor.common.registry;

import net.meteor.common.MeteorsMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.Map;

/**
 * Materiales de armadura (record {@link ArmorMaterial} de 1.21.2+).
 *
 * En el original 1.7.10 el primer número de addArmorMaterial era un multiplicador
 * de durabilidad (36 / 7 / 40) y el array era la protección por pieza
 * {casco, peto, grebas, botas}. Se mapea casi 1:1: ese multiplicador es ahora la
 * durabilidad base que {@link ArmorType#getDurability(int)} multiplica por pieza.
 *
 * La tenacidad (toughness) no existía en 1.7.10; se añaden valores razonables
 * acordes al "tier" de cada material (fáciles de ajustar).
 */
public final class ModArmorMaterials {

	private ModArmorMaterials() {}

	// ---- Meteorite (tier diamante) ----
	public static final int METEORITE_DURABILITY = 36;
	public static final ResourceKey<EquipmentAsset> METEORITE_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, MeteorsMod.id("meteorite"));
	public static final ArmorMaterial METEORITE = new ArmorMaterial(
			METEORITE_DURABILITY,
			Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 7, ArmorType.LEGGINGS, 5, ArmorType.BOOTS, 2),
			15, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F,
			ModToolMaterials.REPAIRS_METEORITE, METEORITE_ASSET);

	// ---- Frezarite (frágil, alta encantabilidad) ----
	public static final int FREZARITE_DURABILITY = 7;
	public static final ResourceKey<EquipmentAsset> FREZARITE_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, MeteorsMod.id("frezarite"));
	public static final ArmorMaterial FREZARITE = new ArmorMaterial(
			FREZARITE_DURABILITY,
			Map.of(ArmorType.HELMET, 2, ArmorType.CHESTPLATE, 5, ArmorType.LEGGINGS, 3, ArmorType.BOOTS, 1),
			20, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F,
			ModToolMaterials.REPAIRS_FREZARITE, FREZARITE_ASSET);

	// ---- Kreknorite (tier netherite) ----
	public static final int KREKNORITE_DURABILITY = 40;
	public static final ResourceKey<EquipmentAsset> KREKNORITE_ASSET =
			ResourceKey.create(EquipmentAssets.ROOT_ID, MeteorsMod.id("kreknorite"));
	public static final ArmorMaterial KREKNORITE = new ArmorMaterial(
			KREKNORITE_DURABILITY,
			Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3),
			10, SoundEvents.ARMOR_EQUIP_NETHERITE, 3.0F, 0.0F,
			ModToolMaterials.REPAIRS_KREKNORITE, KREKNORITE_ASSET);
}
