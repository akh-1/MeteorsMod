package net.meteor.common.registry;

import net.meteor.common.MeteorsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * Materiales de herramienta (record {@link ToolMaterial} de 1.21.2+).
 *
 * Equivalencias con el mod original 1.7.10:
 *  - Meteorite: nivel de cosecha 3 (diamante), durabilidad 900, eficiencia 10, daño 2, ench 15.
 *  - Frezarite: nivel 2 (hierro), durabilidad 225, eficiencia 7, daño 2, ench 20.
 * La espada de Kreknorito usaba el material de Meteorito en el original.
 *
 * El "nivel de cosecha" ahora se expresa con la etiqueta incorrectBlocksForDrops.
 */
public final class ModToolMaterials {

	private ModToolMaterials() {}

	public static final TagKey<Item> REPAIRS_METEORITE =
			TagKey.create(Registries.ITEM, MeteorsMod.id("repairs_meteorite"));
	public static final TagKey<Item> REPAIRS_FREZARITE =
			TagKey.create(Registries.ITEM, MeteorsMod.id("repairs_frezarite"));
	public static final TagKey<Item> REPAIRS_KREKNORITE =
			TagKey.create(Registries.ITEM, MeteorsMod.id("repairs_kreknorite"));

	public static final ToolMaterial METEORITE = new ToolMaterial(
			BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 900, 10.0F, 2.0F, 15, REPAIRS_METEORITE);

	public static final ToolMaterial FREZARITE = new ToolMaterial(
			BlockTags.INCORRECT_FOR_IRON_TOOL, 225, 7.0F, 2.0F, 20, REPAIRS_FREZARITE);
}
