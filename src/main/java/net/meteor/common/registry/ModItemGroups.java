package net.meteor.common.registry;

import net.meteor.common.MeteorsMod;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Pestaña creativa dedicada de Falling Meteors.
 * Debe registrarse DESPUÉS de items y bloques (referencia sus instancias).
 */
public final class ModItemGroups {

	private ModItemGroups() {}

	public static final ResourceKey<CreativeModeTab> METEORS_TAB =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, MeteorsMod.id("meteors"));

	public static void register() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, METEORS_TAB,
				FabricItemGroup.builder()
						.title(Component.translatable("itemGroup.meteors"))
						.icon(() -> new ItemStack(ModItems.METEOR_CHIP))
						.displayItems((params, output) -> {
							// Materiales
							output.accept(ModItems.METEOR_CHIP);
							output.accept(ModItems.FREZARITE_CRYSTAL);
							output.accept(ModItems.KREKNORITE_CHIP);
							output.accept(ModItems.RED_METEOR_GEM);
							output.accept(ModItems.METEORITE_INGOT);
							output.accept(ModItems.FROZEN_IRON);
							output.accept(ModItems.KREKNORITE_INGOT);
							// Comida
							output.accept(ModItems.VANILLA_ICE_CREAM);
							output.accept(ModItems.CHOCOLATE_ICE_CREAM);
							// Bloques
							output.accept(ModBlocks.METEORITE_ORE);
							output.accept(ModBlocks.FREZARITE_ORE);
							output.accept(ModBlocks.METEORITE_BLOCK);
							output.accept(ModBlocks.FROZEN_IRON_BLOCK);
							output.accept(ModBlocks.KREKNORITE_BLOCK);
							output.accept(ModBlocks.RED_METEOR_GEM_BLOCK);
							// Herramientas
							output.accept(ModItems.METEORITE_SWORD);
							output.accept(ModItems.METEORITE_PICKAXE);
							output.accept(ModItems.METEORITE_AXE);
							output.accept(ModItems.METEORITE_SHOVEL);
							output.accept(ModItems.METEORITE_HOE);
							output.accept(ModItems.FREZARITE_SWORD);
							output.accept(ModItems.FREZARITE_PICKAXE);
							output.accept(ModItems.FREZARITE_AXE);
							output.accept(ModItems.FREZARITE_SHOVEL);
							output.accept(ModItems.FREZARITE_HOE);
							output.accept(ModItems.KREKNORITE_SWORD);
							// Armaduras
							output.accept(ModItems.METEORITE_HELMET);
							output.accept(ModItems.METEORITE_CHESTPLATE);
							output.accept(ModItems.METEORITE_LEGGINGS);
							output.accept(ModItems.METEORITE_BOOTS);
							output.accept(ModItems.FREZARITE_HELMET);
							output.accept(ModItems.FREZARITE_CHESTPLATE);
							output.accept(ModItems.FREZARITE_LEGGINGS);
							output.accept(ModItems.FREZARITE_BOOTS);
							output.accept(ModItems.KREKNORITE_HELMET);
							output.accept(ModItems.KREKNORITE_CHESTPLATE);
							output.accept(ModItems.KREKNORITE_LEGGINGS);
							output.accept(ModItems.KREKNORITE_BOOTS);
							// Misc
							output.accept(ModItems.METEOR_SUMMONER);
							output.accept(ModItems.METEORITE_SUMMONER);
							output.accept(ModItems.FREZARITE_SUMMONER);
							output.accept(ModItems.KREKNORITE_SUMMONER);
							output.accept(ModItems.KITTY_SUMMONER);
							output.accept(ModItems.UNKNOWN_SUMMONER);
							output.accept(ModItems.MAGNETIC_FIELD_DISRUPTOR);
							output.accept(ModItems.MAGNETIZATION_CONTROLLER);
							// Bloques de meteoro
							output.accept(ModBlocks.METEORITE_CORE);
							output.accept(ModBlocks.FREZARITE_CORE);
							output.accept(ModBlocks.KREKNORITE_CORE);
							output.accept(ModBlocks.BURNED_EARTH);
							output.accept(ModBlocks.METEOR_SHIELD);
							output.accept(ModBlocks.FREEZING_MACHINE);
							output.accept(ModBlocks.SLIPPERY_BLOCK);
							output.accept(ModBlocks.SLIPPERY_STAIRS);
							output.accept(ModBlocks.METEOR_TIMER);
							output.accept(ModBlocks.METEOR_SHIELD_TORCH);
							output.accept(ModItems.METEOR_DETECTOR_PROXIMITY);
							output.accept(ModItems.METEOR_DETECTOR_TIME);
							output.accept(ModItems.METEOR_DETECTOR_CRASH);
						})
						.build());
	}
}
