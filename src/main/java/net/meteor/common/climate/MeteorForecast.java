package net.meteor.common.climate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.meteor.common.MeteorType;
import net.meteor.common.MeteorsConfig;
import net.meteor.common.MeteorsMod;
import net.meteor.common.ShieldRegistry;
import net.meteor.common.block.MeteorShieldBlockEntity;
import net.meteor.common.entity.MeteorEntity;
import net.meteor.common.registry.ModEntities;
import net.meteor.common.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Pronóstico de meteoros de una dimensión, persistido con el mundo.
 *
 * Reemplaza el contador estático en memoria por el sistema del mod original:
 * hay una cuenta atrás hasta que se PROGRAMA un meteoro, y cada meteoro
 * programado ({@link GhostMeteor}) tiene su propia cuenta atrás hasta caer,
 * con coordenadas y tipo conocidos de antemano. De aquí leen los tres
 * detectores y el Meteor Timer.
 *
 * Todas las cuentas atrás están en segundos: {@link #tickSecond} debe llamarse
 * una vez por segundo.
 */
public class MeteorForecast extends SavedData {

	public static final String ID = "meteors_forecast";

	private static final Codec<MeteorForecast> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			GhostMeteor.CODEC.listOf().optionalFieldOf("pending", List.of()).forGetter(f -> f.pending),
			BlockPos.CODEC.optionalFieldOf("last_crash").forGetter(f -> Optional.ofNullable(f.lastCrash)),
			Codec.INT.optionalFieldOf("last_crash_type", 0).forGetter(f -> f.lastCrashType),
			Codec.INT.optionalFieldOf("schedule_countdown", -1).forGetter(f -> f.scheduleCountdown),
			Codec.LONG.listOf().optionalFieldOf("crashed_chunks", List.of()).forGetter(f -> List.copyOf(f.crashedChunks))
	).apply(inst, MeteorForecast::new));

	public static final SavedDataType<MeteorForecast> TYPE =
			new SavedDataType<>(ID, MeteorForecast::new, CODEC, null);

	private final List<GhostMeteor> pending = new ArrayList<>();
	private final Set<Long> crashedChunks = new LinkedHashSet<>();
	private @Nullable BlockPos lastCrash;
	private int lastCrashType;
	private int scheduleCountdown = -1;

	public MeteorForecast() {}

	private MeteorForecast(List<GhostMeteor> pending, Optional<BlockPos> lastCrash, int lastCrashType,
	                       int scheduleCountdown, List<Long> crashedChunks) {
		this.pending.addAll(pending);
		this.lastCrash = lastCrash.orElse(null);
		this.lastCrashType = lastCrashType;
		this.scheduleCountdown = scheduleCountdown;
		this.crashedChunks.addAll(crashedChunks);
	}

	public static MeteorForecast get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	// ------------------------------------------------------------------
	// Consultas (detectores, Meteor Timer, comandos)
	// ------------------------------------------------------------------

	public List<GhostMeteor> getPending() {
		return List.copyOf(pending);
	}

	/** Segundos hasta que se programe el siguiente meteoro (-1 si aún no hay lectura). */
	public int getSecondsUntilNewMeteor() {
		return scheduleCountdown;
	}

	/** El meteoro programado que caerá antes (los kitty no cuentan, como en el original). */
	public @Nullable GhostMeteor getSoonestMeteor() {
		GhostMeteor best = null;
		for (GhostMeteor g : pending) {
			if (g.getType() == MeteorType.KITTY) continue;
			if (best == null || g.getSecondsLeft() < best.getSecondsLeft()) best = g;
		}
		return best;
	}

	/** El meteoro programado más cercano a un punto. */
	public @Nullable GhostMeteor getNearestMeteor(double x, double z) {
		GhostMeteor best = null;
		double bestDist = Double.MAX_VALUE;
		for (GhostMeteor g : pending) {
			double dx = g.getX() - x;
			double dz = g.getZ() - z;
			double d = dx * dx + dz * dz;
			if (d < bestDist) { bestDist = d; best = g; }
		}
		return best;
	}

	public @Nullable BlockPos getLastCrash() {
		return lastCrash;
	}

	public MeteorType getLastCrashType() {
		return MeteorType.byId(lastCrashType);
	}

	public void setLastCrash(BlockPos pos, MeteorType type) {
		this.lastCrash = pos;
		this.lastCrashType = type.getId();
		this.crashedChunks.add(new ChunkPos(pos).toLong());
		setDirty();
	}

	public boolean hasCrashedIn(ChunkPos chunk) {
		return crashedChunks.contains(chunk.toLong());
	}

	public void clearPending() {
		pending.clear();
		setDirty();
	}

	public void addGhost(GhostMeteor ghost) {
		pending.add(ghost);
		setDirty();
	}

	// ------------------------------------------------------------------
	// Tick (una vez por segundo, desde MeteorSpawner)
	// ------------------------------------------------------------------

	public void tickSecond(ServerLevel level, boolean canSchedule) {
		boolean dirty = false;

		// 1. Avanzar los meteoros ya programados.
		Iterator<GhostMeteor> it = pending.iterator();
		while (it.hasNext()) {
			GhostMeteor ghost = it.next();
			if (ghost.countDown()) {
				if (drop(level, ghost)) {
					it.remove();
				} else if (ghost.waitForChunk() > 60) {
					// Su zona lleva un minuto descargada: se descarta.
					MeteorsMod.LOGGER.debug("Meteoro descartado, chunk descargado: {}", ghost);
					it.remove();
				}
			}
			dirty = true;
		}

		// 2. Programar uno nuevo.
		if (canSchedule) {
			MeteorsConfig c = MeteorsConfig.INSTANCE;
			if (scheduleCountdown < 0) {
				scheduleCountdown = newScheduleGoal(level);
			} else if (--scheduleCountdown <= 0) {
				scheduleCountdown = newScheduleGoal(level);
				scheduleNewMeteor(level);
				if (level.random.nextInt(100) < c.cometFallChance) {
					spawnComet(level);
				}
			}
			dirty = true;
		}

		if (dirty) setDirty();
	}

	private int newScheduleGoal(ServerLevel level) {
		MeteorsConfig c = MeteorsConfig.INSTANCE;
		return c.minSecondsUntilSchedule() + level.random.nextInt(c.randomSecondsUntilSchedule());
	}

	/** Elige sitio, tipo y tamaño y mete un meteoro fantasma en el pronóstico. */
	private void scheduleNewMeteor(ServerLevel level) {
		List<ServerPlayer> players = level.players();
		if (players.isEmpty()) return;

		MeteorsConfig c = MeteorsConfig.INSTANCE;
		ServerPlayer player = players.get(level.random.nextInt(players.size()));
		int x = (int) player.getX() + level.random.nextInt(c.meteorFallRadius * 2) - c.meteorFallRadius;
		int z = (int) player.getZ() + level.random.nextInt(c.meteorFallRadius * 2) - c.meteorFallRadius;

		if (hasCrashedIn(new ChunkPos(new BlockPos(x, 0, z)))) return;   // ese chunk ya recibió uno

		int size = c.minMeteorSize + level.random.nextInt(c.maxMeteorSize - c.minMeteorSize + 1);

		// Evento Kitty Attack: cae casi de inmediato, como en el original.
		boolean kittyAttack = c.kittyEnabled && level.random.nextInt(100) < c.kittyAttackChance;
		MeteorType type = kittyAttack ? MeteorType.KITTY : randomType(level);
		int seconds = kittyAttack
				? 90
				: c.minSecondsUntilCrash() + level.random.nextInt(c.randomSecondsUntilCrash());

		addGhost(new GhostMeteor(x, z, size, type, seconds));

		if (c.textCrashNotification) {
			Component msg = Component.translatable("event.meteors.scheduled", x, z, seconds)
					.withStyle(ChatFormatting.GOLD);
			for (ServerPlayer p : level.players()) p.displayClientMessage(msg, false);
		}
	}

	/**
	 * Hace caer un meteoro programado. Devuelve false si su zona no está cargada
	 * (se reintenta el segundo siguiente).
	 */
	private boolean drop(ServerLevel level, GhostMeteor ghost) {
		BlockPos target = new BlockPos(ghost.getX(), 0, ghost.getZ());
		if (!level.isLoaded(target)) return false;

		// ¿Hay un escudo cubriendo el punto? Entonces lo absorbe.
		BlockPos shieldPos = ShieldRegistry.getProtectingShield(level, ghost.getX(), ghost.getZ());
		if (shieldPos != null) {
			absorb(level, shieldPos, ghost);
			return true;
		}

		MeteorEntity meteor = ModEntities.METEOR.create(level, EntitySpawnReason.EVENT);
		if (meteor == null) return true;
		meteor.setMeteorType(ghost.getType());
		meteor.setMeteorSize(ghost.getSize());
		meteor.snapTo(ghost.getX() + 0.5D, 250.0D, ghost.getZ() + 0.5D, 0.0F, 0.0F);
		meteor.setDeltaMovement(
				(level.random.nextDouble() - level.random.nextDouble()) * 1.2D, -0.04D,
				(level.random.nextDouble() - level.random.nextDouble()) * 1.2D);
		level.addFreshEntity(meteor);
		MeteorsMod.LOGGER.debug("Cae meteoro programado: {}", ghost);
		return true;
	}

	/** El escudo intercepta: deposita el material, avisa y lanza el estallido de partículas. */
	private void absorb(ServerLevel level, BlockPos shieldPos, GhostMeteor ghost) {
		if (level.getBlockEntity(shieldPos) instanceof MeteorShieldBlockEntity shield) {
			for (int i = 0; i < ghost.getSize(); i++) {
				shield.depositMaterial(new ItemStack(materialFor(ghost.getType())));
			}
			blockedEffect(level, shieldPos, ghost);

			if (MeteorsConfig.INSTANCE.blockedMeteorNotification) {
				Component msg = Component.translatable("event.meteors.blocked",
						ghost.getX(), ghost.getZ()).withStyle(ChatFormatting.AQUA);
				ServerPlayer owner = level.getServer().getPlayerList().getPlayerByName(shield.getOwner());
				if (owner != null) owner.displayClientMessage(msg, false);
			}
		}
	}

	/** Estallido de partículas de bloque sobre el escudo, como el PacketBlockedMeteor original. */
	private void blockedEffect(ServerLevel level, BlockPos shieldPos, GhostMeteor ghost) {
		BlockParticleOption option = new BlockParticleOption(ParticleTypes.BLOCK,
				ghost.getType().getCoreBlock().defaultBlockState());
		level.sendParticles(option,
				shieldPos.getX() + 0.5D, shieldPos.getY() + 1.5D, shieldPos.getZ() + 0.5D,
				150, 0.6D, 0.4D, 0.6D, 0.35D);
		level.sendParticles(ParticleTypes.EXPLOSION,
				shieldPos.getX() + 0.5D, shieldPos.getY() + 2.0D, shieldPos.getZ() + 0.5D,
				3, 0.5D, 0.5D, 0.5D, 0.0D);
		level.playSound(null, shieldPos, net.minecraft.sounds.SoundEvents.BEACON_DEACTIVATE,
				net.minecraft.sounds.SoundSource.BLOCKS, 0.8F, 1.4F);
	}

	/** El cometa sigue siendo inmediato (no se programa), pero ahora los escudos lo reportan. */
	private void spawnComet(ServerLevel level) {
		List<ServerPlayer> players = level.players();
		if (players.isEmpty()) return;

		MeteorsConfig c = MeteorsConfig.INSTANCE;
		ServerPlayer player = players.get(level.random.nextInt(players.size()));
		int radius = Math.max(16, c.meteorFallRadius / 4);
		int x = (int) player.getX() + level.random.nextInt(radius * 2) - radius;
		int z = (int) player.getZ() + level.random.nextInt(radius * 2) - radius;

		MeteorType type = randomType(level);

		// Un escudo con "bloquear cometas" activo lo impide; el resto solo lo reportan.
		List<BlockPos> shields = ShieldRegistry.getShieldsInRange(level, x, z);
		for (BlockPos pos : shields) {
			if (level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield && shield.getBlockComets()) {
				return;
			}
		}
		for (BlockPos pos : shields) {
			if (level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield) {
				shield.reportComet(x, z, type);
			}
		}

		MeteorEntity comet = ModEntities.METEOR.create(level, EntitySpawnReason.EVENT);
		if (comet == null) return;
		comet.setComet(true);
		comet.setMeteorType(type);
		comet.setMeteorSize(1);
		comet.snapTo(x + 0.5D, 250.0D, z + 0.5D, 0.0F, 0.0F);
		comet.setDeltaMovement(
				(level.random.nextDouble() - level.random.nextDouble()) * 1.2D, -0.04D,
				(level.random.nextDouble() - level.random.nextDouble()) * 1.2D);
		level.addFreshEntity(comet);

		Component msg = Component.translatable("event.meteors.cometOrbit", x, z).withStyle(ChatFormatting.AQUA);
		for (ServerPlayer p : level.players()) p.displayClientMessage(msg, false);
	}

	private static MeteorType randomType(ServerLevel level) {
		MeteorsConfig c = MeteorsConfig.INSTANCE;
		List<MeteorType> pool = new ArrayList<>();
		List<Integer> weights = new ArrayList<>();
		if (c.meteoriteEnabled)  { pool.add(MeteorType.METEORITE);  weights.add(58); }
		if (c.frezariteEnabled)  { pool.add(MeteorType.FREZARITE);  weights.add(24); }
		if (c.kreknoriteEnabled) { pool.add(MeteorType.KREKNORITE); weights.add(15); }
		if (c.kittyEnabled)      { pool.add(MeteorType.KITTY);      weights.add(3); }
		if (c.unknownEnabled)    { pool.add(MeteorType.UNKNOWN);    weights.add(2); }
		if (pool.isEmpty()) return MeteorType.METEORITE;

		int total = weights.stream().mapToInt(Integer::intValue).sum();
		int r = level.random.nextInt(total);
		int acc = 0;
		for (int i = 0; i < pool.size(); i++) {
			acc += weights.get(i);
			if (r < acc) return pool.get(i);
		}
		return pool.get(0);
	}

	public static Item materialFor(MeteorType type) {
		return switch (type) {
			case FREZARITE -> ModItems.FREZARITE_CRYSTAL;
			case KREKNORITE -> ModItems.KREKNORITE_CHIP;
			case KITTY -> ModItems.RED_METEOR_GEM;
			default -> ModItems.METEOR_CHIP;
		};
	}
}
