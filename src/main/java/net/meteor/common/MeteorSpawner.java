package net.meteor.common;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.meteor.common.climate.MeteorForecast;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Motor de la lluvia de meteoros. Ya no guarda estado: solo decide si esta
 * dimensión debe tickear su pronóstico y con qué condiciones, y delega en
 * {@link MeteorForecast}, que sí persiste con el mundo.
 *
 * El pronóstico avanza una vez por segundo, igual que el ClimateUpdater original.
 */
public final class MeteorSpawner {

	private MeteorSpawner() {}

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(MeteorSpawner::tick);
	}

	private static void tick(ServerLevel world) {
		if (world.getGameTime() % 20L != 0L) return;                       // una vez por segundo

		MeteorsConfig config = MeteorsConfig.INSTANCE;
		if (!config.meteorsFallEnabled) return;
		if (!isDimensionAllowed(world, config)) return;
		if (world.players().isEmpty()) return;

		// La cuenta atrás de PROGRAMACIÓN solo avanza de noche (si así está configurado);
		// los meteoros ya programados siguen cayendo a cualquier hora.
		boolean night = (world.getDayTime() % 24000L) >= 12000L;
		boolean canSchedule = night || !config.meteorsFallOnlyAtNight;

		MeteorForecast.get(world).tickSecond(world, canSchedule);
	}

	/** Sustituye a la whitelist de IDs del original: un interruptor por dimensión vanilla. */
	private static boolean isDimensionAllowed(ServerLevel world, MeteorsConfig config) {
		ResourceKey<Level> dimension = world.dimension();
		if (dimension == Level.OVERWORLD) return config.allowOverworld;
		if (dimension == Level.NETHER) return config.allowNether;
		if (dimension == Level.END) return config.allowEnd;
		return config.allowOtherDimensions;
	}
}
