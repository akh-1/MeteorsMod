package net.meteor.common;

import net.meteor.common.climate.ShieldSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fachada de consulta de escudos. Ahora se apoya en {@link ShieldSavedData},
 * así que la protección persiste entre reinicios y funciona con el chunk del
 * escudo descargado (como el ShieldManager del mod original).
 */
public final class ShieldRegistry {

	private ShieldRegistry() {}

	public static void add(ServerLevel level, BlockPos pos, int range, String owner, boolean blockComets) {
		ShieldSavedData.get(level).put(pos, range, owner, blockComets);
	}

	public static void remove(ServerLevel level, BlockPos pos) {
		ShieldSavedData.get(level).remove(pos);
	}

	public static boolean isProtected(ServerLevel level, double x, double z) {
		return getProtectingShield(level, x, z) != null;
	}

	public static @Nullable BlockPos getProtectingShield(ServerLevel level, double x, double z) {
		List<ShieldSavedData.Entry> found = ShieldSavedData.get(level).inRange(x, z);
		return found.isEmpty() ? null : found.get(0).pos();
	}

	/** Como el anterior, pero ignorando los escudos cuyo dueño sea el jugador dado. */
	public static @Nullable BlockPos getForeignShield(ServerLevel level, double x, double z, String playerName) {
		for (ShieldSavedData.Entry e : ShieldSavedData.get(level).inRange(x, z)) {
			if (!e.owner().equalsIgnoreCase(playerName)) return e.pos();
		}
		return null;
	}

	public static List<BlockPos> getShieldsInRange(ServerLevel level, double x, double z) {
		List<BlockPos> out = new ArrayList<>();
		for (ShieldSavedData.Entry e : ShieldSavedData.get(level).inRange(x, z)) out.add(e.pos());
		return out;
	}

	public static List<ShieldSavedData.Entry> all(ServerLevel level) {
		return List.copyOf(ShieldSavedData.get(level).all());
	}
}
