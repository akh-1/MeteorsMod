package net.meteor.common.item;

import net.meteor.common.climate.GhostMeteor;
import net.meteor.common.climate.MeteorForecast;
import net.meteor.common.entity.MeteorEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Detector de meteoros de mano. Lee el pronóstico persistente
 * ({@link MeteorForecast}), así que informa de meteoros que aún no han caído:
 * dónde, cuándo y de qué tipo, como en el mod original.
 */
public class DetectorItem extends Item {

	public enum Mode { PROXIMITY, TIME, CRASH }

	private final Mode mode;

	public DetectorItem(Properties properties, Mode mode) {
		super(properties);
		this.mode = mode;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && level instanceof ServerLevel sl) {
			MeteorForecast forecast = MeteorForecast.get(sl);
			Component msg = switch (mode) {
				case PROXIMITY -> proximityInfo(sl, forecast, player);
				case TIME -> timeInfo(forecast);
				case CRASH -> crashInfo(forecast);
			};
			player.displayClientMessage(msg, true);
		}
		return InteractionResult.SUCCESS;
	}

	/** Primero mira el pronóstico; si no hay nada programado, busca meteoros ya cayendo. */
	private Component proximityInfo(ServerLevel level, MeteorForecast forecast, Player player) {
		GhostMeteor ghost = forecast.getNearestMeteor(player.getX(), player.getZ());
		if (ghost != null) {
			double dx = ghost.getX() - player.getX();
			double dz = ghost.getZ() - player.getZ();
			int dist = (int) Math.sqrt(dx * dx + dz * dz);
			return Component.translatable("detector.meteors.proximity.ghost",
					ghost.getType().getDisplayName(), dist, cardinal(dx, dz), ghost.getSecondsLeft())
					.withStyle(ChatFormatting.LIGHT_PURPLE);
		}

		AABB box = player.getBoundingBox().inflate(256.0D);
		List<MeteorEntity> meteors = level.getEntitiesOfClass(MeteorEntity.class, box);
		MeteorEntity nearest = null;
		double best = Double.MAX_VALUE;
		for (MeteorEntity m : meteors) {
			double d = m.distanceToSqr(player);
			if (d < best) { best = d; nearest = m; }
		}
		if (nearest == null) {
			return Component.translatable("detector.meteors.proximity.none").withStyle(ChatFormatting.GRAY);
		}
		int dist = (int) Math.sqrt(best);
		String dir = cardinal(nearest.getX() - player.getX(), nearest.getZ() - player.getZ());
		return Component.translatable("detector.meteors.proximity.found", dist, dir)
				.withStyle(ChatFormatting.LIGHT_PURPLE);
	}

	private Component timeInfo(MeteorForecast forecast) {
		GhostMeteor soonest = forecast.getSoonestMeteor();
		if (soonest != null) {
			return Component.translatable("detector.meteors.time.scheduled",
					soonest.getType().getDisplayName(), soonest.getSecondsLeft(),
					soonest.getX(), soonest.getZ()).withStyle(ChatFormatting.AQUA);
		}
		int seconds = forecast.getSecondsUntilNewMeteor();
		if (seconds < 0) {
			return Component.translatable("detector.meteors.time.unknown").withStyle(ChatFormatting.GRAY);
		}
		return Component.translatable("detector.meteors.time.next", seconds).withStyle(ChatFormatting.AQUA);
	}

	private Component crashInfo(MeteorForecast forecast) {
		BlockPos pos = forecast.getLastCrash();
		if (pos == null) {
			return Component.translatable("detector.meteors.crash.none").withStyle(ChatFormatting.GRAY);
		}
		return Component.translatable("detector.meteors.crash.typed",
				forecast.getLastCrashType().getDisplayName(), pos.getX(), pos.getZ())
				.withStyle(ChatFormatting.RED);
	}

	private static String cardinal(double dx, double dz) {
		if (Math.abs(dx) > Math.abs(dz)) return dx > 0 ? "E" : "W";
		return dz > 0 ? "S" : "N";
	}
}
