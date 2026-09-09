package net.meteor.common;

import net.fabricmc.api.ModInitializer;
import net.meteor.common.registry.ModBlockEntities;
import net.meteor.common.registry.ModMenuTypes;
import net.meteor.common.registry.ModBlocks;
import net.meteor.common.registry.ModEntities;
import net.meteor.common.registry.ModItemGroups;
import net.meteor.common.registry.ModItems;
import net.meteor.common.registry.ModSounds;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Punto de entrada principal (común a cliente y servidor) del port de Falling Meteors.
 *
 * El registro de contenido se hace por fases en clases dedicadas dentro de
 * {@code net.meteor.common.registry}. Esta clase solo orquesta el orden de carga.
 */
public class MeteorsMod implements ModInitializer {

	public static final String MOD_ID = "meteors";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Crea un identificador dentro del namespace del mod, p.ej. {@code meteors:meteor_chip}. */
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Falling Meteors: inicializando (port a Fabric 1.21.11)");

		// El orden importa: los items deben existir antes de que la pestaña
		// creativa los referencie, y los bloques crean sus BlockItems al registrarse.
		MeteorsConfig.load();
		ModSounds.register();
		ModItems.register();
		if (net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("trinkets")) {
			net.meteor.common.compat.MeteorsTrinketsCompat.init();
		}
		ModBlocks.register();
		ModBlockEntities.register();
		ModMenuTypes.register();
		ModEntities.register();
		ModEntities.registerAttributes();
		ModItemGroups.register();
		GearEffects.register();
		MeteorSpawner.register();
		net.meteor.common.command.MeteorsCommand.register();
		ModWorldGen.register();
	}
}
