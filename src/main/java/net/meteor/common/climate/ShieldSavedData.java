package net.meteor.common.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Escudos anti-meteoro de una dimensión, guardados con el mundo.
 *
 * En el original el ShieldManager era global y persistente, así que un escudo
 * protegía su radio aunque su chunk estuviera descargado. Esta clase recupera
 * ese comportamiento: el block entity solo sincroniza su entrada aquí, y las
 * consultas de protección no necesitan que el escudo esté cargado.
 */
public class ShieldSavedData extends SavedData {

	public static final String ID = "meteors_shields";

	public record Entry(BlockPos pos, int range, String owner, boolean blockComets) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
				BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
				Codec.INT.fieldOf("range").forGetter(Entry::range),
				Codec.STRING.optionalFieldOf("owner", "None").forGetter(Entry::owner),
				Codec.BOOL.optionalFieldOf("block_comets", false).forGetter(Entry::blockComets)
		).apply(inst, Entry::new));
	}

	private static final Codec<ShieldSavedData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Entry.CODEC.listOf().optionalFieldOf("shields", List.of()).forGetter(d -> List.copyOf(d.shields.values()))
	).apply(inst, ShieldSavedData::new));

	public static final SavedDataType<ShieldSavedData> TYPE =
			new SavedDataType<>(ID, ShieldSavedData::new, CODEC, null);

	private final Map<BlockPos, Entry> shields = new HashMap<>();

	public ShieldSavedData() {}

	private ShieldSavedData(List<Entry> entries) {
		for (Entry e : entries) shields.put(e.pos(), e);
	}

	public static ShieldSavedData get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	public void put(BlockPos pos, int range, String owner, boolean blockComets) {
		Entry old = shields.get(pos);
		Entry now = new Entry(pos.immutable(), range, owner, blockComets);
		if (!now.equals(old)) {
			shields.put(now.pos(), now);
			setDirty();
		}
	}

	public void remove(BlockPos pos) {
		if (shields.remove(pos) != null) setDirty();
	}

	public Collection<Entry> all() {
		return List.copyOf(shields.values());
	}

	public List<Entry> inRange(double x, double z) {
		List<Entry> found = new ArrayList<>();
		for (Entry e : shields.values()) {
			double dx = e.pos().getX() + 0.5D - x;
			double dz = e.pos().getZ() + 0.5D - z;
			long r = e.range();
			if (r > 0 && dx * dx + dz * dz <= (double) r * r) found.add(e);
		}
		return found;
	}
}
