package net.meteor.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuración del mod (sustituto del ModConfig original). Se guarda en config/meteors.json.
 *
 * El planificador funciona en SEGUNDOS, igual que el ClimateUpdater original
 * (que solo tickeaba una vez cada 20 ticks). "Deterrence" es el mismo valor del
 * original: de él se derivan las cuatro ventanas de tiempo.
 */
public class MeteorsConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static MeteorsConfig INSTANCE = new MeteorsConfig();

	// --- General ---
	public boolean meteorsFallOnlyAtNight = true;
	public int meteorFallRadius = 350;       // bloques alrededor de un jugador (original: 350)
	public int meteorFallDeterrence = 25;    // de aquí salen las ventanas de aviso e impacto
	public boolean textCrashNotification = false;  // aviso en el chat al programarse un meteoro
	public boolean blockedMeteorNotification = true; // aviso al dueño cuando su escudo intercepta

	/** Interruptor maestro de la lluvia de meteoros (sustituye al gamerule meteorsFall). */
	public boolean meteorsFallEnabled = true;

	// --- Dimensiones donde pueden caer meteoros de forma natural ---
	public boolean allowOverworld = true;
	public boolean allowNether = false;
	public boolean allowEnd = true;
	public boolean allowOtherDimensions = false;   // dimensiones de otros mods

	// --- Tipos de meteoro ---
	public boolean meteoriteEnabled = true;
	public boolean frezariteEnabled = true;
	public boolean kreknoriteEnabled = true;
	public boolean kittyEnabled = true;
	public boolean unknownEnabled = true;
	public int cometFallChance = 20;         // % de que además caiga un cometa (0-100)
	public int kittyAttackChance = 1;        // % de que el meteoro programado sea un ataque kitty

	// --- Tamaño e impacto ---
	public int minMeteorSize = 1;            // 1-3
	public int maxMeteorSize = 3;            // 1-3
	public double impactExplosionMultiplier = 5.0D;  // × tamaño = potencia de la explosión
	public int impactSpread = 4;             // × tamaño = alcance del cráter
	public int minMeteorSizeForPortal = 2;   // tamaño mínimo de kreknorito para generar portal

	// --- Escudo ---
	public int shieldRangePerLevel = 64;     // rango = nivel * esto
	public boolean allowSummonedMeteorGrief = false;  // permitir invocar en terreno protegido ajeno
	public boolean shieldHumSound = true;    // zumbido ocasional del escudo

	// --- Otros ---
	public boolean slipperyBlocksEnabled = true;

	// ------------------------------------------------------------------
	// Ventanas derivadas de "deterrence" (en segundos), igual que el original
	// ------------------------------------------------------------------

	/** Espera mínima hasta que se programa el siguiente meteoro. */
	public int minSecondsUntilSchedule() {
		return (int) (meteorFallDeterrence * 100 * 0.25D * 0.25D);
	}

	/** Aleatorio adicional sobre la espera de programación. */
	public int randomSecondsUntilSchedule() {
		return Math.max(1, (int) (meteorFallDeterrence * 100 * 0.25D * 0.75D));
	}

	/** Cuenta atrás mínima de un meteoro ya programado hasta que cae. */
	public int minSecondsUntilCrash() {
		return (int) (meteorFallDeterrence * 100 * 0.75D * 0.5D);
	}

	/** Aleatorio adicional sobre la cuenta atrás de caída. */
	public int randomSecondsUntilCrash() {
		return Math.max(1, (int) (meteorFallDeterrence * 100 * 0.75D * 0.5D));
	}


	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("meteors.json");
	}

	public static void load() {
		try {
			Path p = path();
			if (Files.exists(p)) {
				try (Reader r = Files.newBufferedReader(p)) {
					MeteorsConfig c = GSON.fromJson(r, MeteorsConfig.class);
					if (c != null) INSTANCE = c;
				}
			}
			INSTANCE.sanitize();
			save();
			MeteorsMod.LOGGER.info("Config de Meteors cargada.");
		} catch (Exception e) {
			MeteorsMod.LOGGER.error("No se pudo cargar la config de Meteors; usando valores por defecto.", e);
		}
	}

	public static void save() {
		try (Writer w = Files.newBufferedWriter(path())) {
			GSON.toJson(INSTANCE, w);
		} catch (Exception e) {
			MeteorsMod.LOGGER.error("No se pudo guardar la config de Meteors.", e);
		}
	}

	private void sanitize() {
		minMeteorSize = clamp(minMeteorSize, 1, 3);
		maxMeteorSize = clamp(maxMeteorSize, minMeteorSize, 3);
		minMeteorSizeForPortal = clamp(minMeteorSizeForPortal, minMeteorSize, 3);
		cometFallChance = clamp(cometFallChance, 0, 100);
		kittyAttackChance = clamp(kittyAttackChance, 0, 100);
		meteorFallRadius = Math.max(16, meteorFallRadius);
		meteorFallDeterrence = Math.max(1, meteorFallDeterrence);
		shieldRangePerLevel = Math.max(0, shieldRangePerLevel);
		impactSpread = Math.abs(impactSpread);
		impactExplosionMultiplier = Math.max(0.0D, Math.min(20.0D, impactExplosionMultiplier));
	}

	private static int clamp(int v, int lo, int hi) {
		return Math.max(lo, Math.min(hi, v));
	}
}
