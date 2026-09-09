package net.meteor.common;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Generación de menas del mod en el mundo (worldgen). Las features configuradas
 * y emplazadas se definen por JSON en data/meteors/worldgen/; aquí solo se
 * enganchan a los biomas del Overworld.
 */
public final class ModWorldGen {

	private ModWorldGen() {}

	private static final ResourceKey<PlacedFeature> METEORITE_ORE =
			ResourceKey.create(Registries.PLACED_FEATURE, MeteorsMod.id("meteorite_ore"));
	private static final ResourceKey<PlacedFeature> FREZARITE_ORE =
			ResourceKey.create(Registries.PLACED_FEATURE, MeteorsMod.id("frezarite_ore"));

	public static void register() {
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES, METEORITE_ORE);
		BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES, FREZARITE_ORE);
	}
}
