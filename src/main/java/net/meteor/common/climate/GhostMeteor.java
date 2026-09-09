package net.meteor.common.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.meteor.common.MeteorType;

/**
 * Meteoro "fantasma": un meteoro ya programado que todavía no ha caído.
 *
 * Es la pieza que faltaba del original ({@code net.meteor.common.climate.GhostMeteor}):
 * el mundo sabe con antelación dónde, cuándo y de qué tipo será el próximo
 * impacto, y de aquí leen los detectores y el Meteor Timer.
 *
 * La cuenta atrás está en SEGUNDOS, igual que en el mod original.
 */
public final class GhostMeteor {

	public static final Codec<GhostMeteor> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.INT.fieldOf("x").forGetter(g -> g.x),
			Codec.INT.fieldOf("z").forGetter(g -> g.z),
			Codec.INT.fieldOf("size").forGetter(g -> g.size),
			Codec.INT.fieldOf("type").forGetter(g -> g.type.getId()),
			Codec.INT.fieldOf("seconds").forGetter(g -> g.secondsLeft),
			Codec.INT.optionalFieldOf("waited", 0).forGetter(g -> g.secondsWaitingForChunk)
	).apply(inst, GhostMeteor::new));

	private final int x;
	private final int z;
	private final int size;
	private final MeteorType type;
	private int secondsLeft;
	private int secondsWaitingForChunk;

	private GhostMeteor(int x, int z, int size, int typeId, int secondsLeft, int waited) {
		this(x, z, size, MeteorType.byId(typeId), secondsLeft);
		this.secondsWaitingForChunk = waited;
	}

	public GhostMeteor(int x, int z, int size, MeteorType type, int secondsLeft) {
		this.x = x;
		this.z = z;
		this.size = size;
		this.type = type;
		this.secondsLeft = secondsLeft;
		this.secondsWaitingForChunk = 0;
	}

	public int getX() { return x; }
	public int getZ() { return z; }
	public int getSize() { return size; }
	public MeteorType getType() { return type; }
	public int getSecondsLeft() { return secondsLeft; }

	/** Descuenta un segundo. Devuelve true si ya toca caer. */
	public boolean countDown() {
		if (secondsLeft > 0) secondsLeft--;
		return secondsLeft <= 0;
	}

	/** Segundos que lleva esperando a que se cargue su chunk. */
	public int waitForChunk() {
		return ++secondsWaitingForChunk;
	}

	@Override
	public String toString() {
		return type + " tam." + size + " en " + x + ", " + z + " (" + secondsLeft + " s)";
	}
}
