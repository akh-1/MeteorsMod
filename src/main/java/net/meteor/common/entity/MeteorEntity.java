package net.meteor.common.entity;

import net.meteor.common.MeteorType;
import net.meteor.common.MeteorsConfig;
import net.meteor.common.climate.MeteorForecast;
import net.meteor.common.block.MeteorCoreBlock;
import net.meteor.common.registry.ModBlocks;
import net.meteor.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Meteoro que cae del cielo: gravedad simple, partículas en la estela y, al
 * tocar suelo, explosión + cráter de mineral + creepers alienígenas.
 *
 * Es una entidad transitoria (vive pocos segundos), así que no persiste NBT.
 * El tipo y el tamaño se sincronizan al cliente vía SynchedEntityData para el render.
 */
public class MeteorEntity extends Entity {

	private static final EntityDataAccessor<Integer> DATA_TYPE =
			SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SIZE =
			SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_COMET =
			SynchedEntityData.defineId(MeteorEntity.class, EntityDataSerializers.BOOLEAN);

	public MeteorEntity(EntityType<? extends MeteorEntity> type, Level level) {
		super(type, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_TYPE, 0);
		builder.define(DATA_SIZE, 1);
		builder.define(DATA_COMET, false);
	}

	public void setComet(boolean comet) { this.entityData.set(DATA_COMET, comet); }
	public boolean isComet() { return this.entityData.get(DATA_COMET); }

	public void setMeteorType(MeteorType t) {
		this.entityData.set(DATA_TYPE, t.getId());
	}

	public MeteorType getMeteorType() {
		return MeteorType.byId(this.entityData.get(DATA_TYPE));
	}

	public void setMeteorSize(int s) {
		this.entityData.set(DATA_SIZE, Math.max(1, s));
	}

	public int getMeteorSize() {
		return this.entityData.get(DATA_SIZE);
	}

	@Override
	public void tick() {
		super.tick();

		Vec3 m = this.getDeltaMovement();
		this.setDeltaMovement(m.x, m.y - 0.04D, m.z);
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));

		if (this.level().isClientSide()) {
			this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.5D, this.getZ(), 0, 0, 0);
			this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0D, this.getZ(), 0, 0, 0);
			if (this.isComet()) {
				// Estela más brillante y larga para el cometa
				this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 2.0D, this.getZ(), 0, 0.05D, 0);
				this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY() + 1.5D, this.getZ(), 0, 0, 0);
			}
		} else if (this.onGround()) {
			impact((ServerLevel) this.level());
			this.discard();
		}
	}

	private void impact(ServerLevel level) {
		MeteorType type = getMeteorType();
		int size = getMeteorSize();
		MeteorForecast.get(level).setLastCrash(this.blockPosition(), type);
		level.playSound(null, this.blockPosition(), net.meteor.common.registry.ModSounds.METEOR_CRASH, net.minecraft.sounds.SoundSource.AMBIENT, 4.0F, 1.0F);

		if (isComet()) {
			cometImpact(level, type);
			return;
		}

		if (type == MeteorType.KITTY) {
			level.explode(this, this.getX(), this.getY(), this.getZ(), size * 1.5F, false, Level.ExplosionInteraction.MOB);
			spawnKitties(level, size);
			return;
		}

		if (type == MeteorType.UNKNOWN) {
			level.explode(this, this.getX(), this.getY(), this.getZ(),
					(float) (size * MeteorsConfig.INSTANCE.impactExplosionMultiplier), true, Level.ExplosionInteraction.MOB);
			generateUnknownCrater(level);
			return;
		}

		float power = (float) (size * MeteorsConfig.INSTANCE.impactExplosionMultiplier);
		level.explode(this, this.getX(), this.getY(), this.getZ(), power, type.isFiery(), Level.ExplosionInteraction.MOB);
		generateCrater(level, type, size);

		switch (type) {
			case METEORITE -> spawnCreepers(level, size);
			case KREKNORITE -> {
				spawnBlazes(level, size);
				if (size >= MeteorsConfig.INSTANCE.minMeteorSizeForPortal) {   // estructura con portal
					createNetherPortal(level, this.getBlockX(), this.getBlockY(), this.getBlockZ());
				}
			}
			default -> {}   // Frezarito: solo nieve/hielo (en el cráter)
		}
	}

	private void spawnBlazes(ServerLevel level, int size) {
		int n = this.random.nextInt(3);   // 0-2 blazes, como el original
		for (int i = 0; i < n; i++) {
			Blaze blaze = EntityType.BLAZE.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (blaze != null) {
				double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
				double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;
				blaze.snapTo(ox, this.getY() + 1.0D, oz, 0.0F, 0.0F);
				level.addFreshEntity(blaze);
			}
		}
	}

	/** Estructura completa del original: torre de ladrillo del Nether con base escalonada, columnas, techo y portal. */
	private void createNetherPortal(ServerLevel level, int i, int j, int k) {
		BlockState brick = Blocks.NETHER_BRICKS.defaultBlockState();
		BlockState obs = Blocks.OBSIDIAN.defaultBlockState();
		BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState portal = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, Direction.Axis.X);

		// Base escalonada
		fillBox(level, i + 1, j + 1, k, i, j + 1, k, brick);
		fillBox(level, i + 2, j + 2, k + 1, i - 1, j + 2, k - 1, brick);
		fillBox(level, i + 3, j + 3, k + 2, i - 2, j + 3, k - 2, brick);

		// Marco del portal (obsidiana): columnas, suelo y dintel
		fillBox(level, i - 1, j + 3, k, i - 1, j + 7, k, obs);
		fillBox(level, i + 2, j + 3, k, i + 2, j + 7, k, obs);
		fillBox(level, i - 1, j + 3, k, i + 2, j + 3, k, obs);
		fillBox(level, i - 1, j + 7, k, i + 2, j + 7, k, obs);
		// Interior: bloques de portal
		fillBox(level, i, j + 4, k, i + 1, j + 6, k, portal);

		// Columnas de la torre
		fillBox(level, i + 3, j + 4, k + 2, i + 3, j + 8, k + 2, brick);
		fillBox(level, i + 3, j + 4, k - 2, i + 3, j + 8, k - 2, brick);
		fillBox(level, i - 2, j + 4, k - 2, i - 2, j + 8, k - 2, brick);
		fillBox(level, i - 2, j + 4, k + 2, i - 2, j + 8, k + 2, brick);

		// Primer nivel del techo
		fillBox(level, i + 3, j + 8, k + 3, i - 2, j + 8, k + 3, brick);
		fillBox(level, i + 3, j + 8, k - 3, i - 2, j + 8, k - 3, brick);
		fillBox(level, i + 4, j + 8, k + 2, i + 4, j + 8, k - 2, brick);
		fillBox(level, i - 3, j + 8, k + 2, i - 3, j + 8, k - 2, brick);

		// Segundo nivel del techo (esquinas abiertas) + glowstone
		fillBox(level, i + 3, j + 9, k + 2, i - 2, j + 9, k - 2, brick);
		level.setBlock(new BlockPos(i + 3, j + 9, k + 2), air, 3);
		level.setBlock(new BlockPos(i + 3, j + 9, k - 2), air, 3);
		level.setBlock(new BlockPos(i - 2, j + 9, k - 2), air, 3);
		level.setBlock(new BlockPos(i - 2, j + 9, k + 2), air, 3);
		fillBox(level, i + 2, j + 9, k + 1, i - 1, j + 9, k - 1, glow);

		// Techo superior
		fillBox(level, i + 2, j + 10, k + 1, i - 1, j + 10, k - 1, brick);
	}

	/** Rellena una caja sólida entre dos esquinas (inclusive). */
	private static void fillBox(ServerLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
		int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
		int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
		int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					level.setBlock(new BlockPos(x, y, z), state, 3);
				}
			}
		}
	}

	private void spawnKitties(ServerLevel level, int size) {
		int n = size * size;   // tamaño² gatos, como el original
		for (int i = 0; i < n; i++) {
			CometKitty kitty = ModEntities.COMET_KITTY.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (kitty != null) {
				double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 4.0D;
				double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 4.0D;
				kitty.snapTo(ox, this.getY() + 1.0D, oz, 0.0F, 0.0F);
				level.addFreshEntity(kitty);
			}
		}
	}

	/**
	 * Impacto del cometa: explosión mínima (0.5) y un mini-cráter de pocos bloques
	 * de núcleo en un volumen 5x5x5, con rareza creciente (como CrashComet original).
	 * Un cometa KITTY suelta un CometKitty.
	 */
	private void cometImpact(ServerLevel level, MeteorType type) {
		level.explode(this, this.getX(), this.getY(), this.getZ(), 0.5F, false, Level.ExplosionInteraction.MOB);

		if (type == MeteorType.KITTY) {
			CometKitty kitty = ModEntities.COMET_KITTY.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (kitty != null) {
				kitty.snapTo(this.getX(), this.getY() + 1.0D, this.getZ(), 0.0F, 0.0F);
				level.addFreshEntity(kitty);
			}
		}

		BlockState core = type.getCoreBlock().defaultBlockState();
		int i = this.getBlockX(), j = this.getBlockY(), k = this.getBlockZ();
		int chance = 15, placed = 0;
		for (int y = j - 2; y <= j + 2; y++) {
			for (int x = i - 2; x <= i + 2; x++) {
				for (int z = k - 2; z <= k + 2; z++) {
					BlockPos p = new BlockPos(x, y, z);
					if (!level.getBlockState(p).canBeReplaced()) continue;
					if (this.random.nextInt(100) + 1 > chance) {
						BlockState place = core;
						if (type.isFiery()) place = core.setValue(MeteorCoreBlock.HEAT, 1 + this.random.nextInt(4));
						level.setBlock(p, place, 3);
						placed++;
						chance = 15 + 20 * placed;
					}
				}
			}
		}
	}

	/**
	 * Relleno disperso del cráter (estilo original): reemplaza terreno sólido con
	 * el bloque núcleo (~30%, sin apilar por columna). El frezarito se rellena más
	 * densamente y mezcla hielo. Las menas NO van aquí (generan por worldgen).
	 */
	private void generateCrater(ServerLevel level, MeteorType type, int size) {
		BlockState core = type.getCoreBlock().defaultBlockState();
		int spread = Math.max(1, MeteorsConfig.INSTANCE.impactSpread) * size;
		int reach = spread * 2;
		int cx = this.getBlockX();
		int cy = this.getBlockY();
		int cz = this.getBlockZ();
		boolean dense = (type == MeteorType.FREZARITE);

		// Pase principal (~30%, o ~60% si es hielo)
		int threshold = dense ? 4 : 7;
		for (int y = cy + spread; y >= cy - spread; y--) {
			for (int x = cx - spread; x <= cx + spread; x++) {
				for (int z = cz - spread; z <= cz + spread; z++) {
					if (this.random.nextInt(10) + 1 > threshold) {
						tryScatter(level, x, y, z, core, type, reach, dense);
					}
				}
			}
		}
		// Pase inferior: concentración bajo el centro
		for (int y = cy - spread; y >= cy - spread - 1; y--) {
			for (int x = cx - spread; x <= cx + spread; x++) {
				for (int z = cz - spread; z <= cz + spread; z++) {
					tryScatter(level, x, y, z, core, type, reach, dense);
				}
			}
		}
		applySurfaceAccents(level, type, spread);
	}

	/** Coloca el núcleo si el bloque es sólido reemplazable. El frezarito no comprueba columna y mezcla hielo. */
	private void tryScatter(ServerLevel level, int x, int y, int z, BlockState core, MeteorType type, int reach, boolean dense) {
		BlockPos pos = new BlockPos(x, y, z);
		BlockState cur = level.getBlockState(pos);
		if (cur.isAir() || cur.is(Blocks.BEDROCK) || cur.is(Blocks.WATER) || cur.is(Blocks.LAVA)) return;
		if (cur.is(type.getCoreBlock()) || cur.is(Blocks.ICE)) return;
		if (!dense && meteorInColumn(level, x, y, z, reach, type)) return;

		BlockState place = core;
		if (type == MeteorType.FREZARITE && this.random.nextInt(2) == 0) {
			place = Blocks.ICE.defaultBlockState();   // cráter helado más lleno
		} else if (type.isFiery()) {
			place = core.setValue(MeteorCoreBlock.HEAT, 1 + this.random.nextInt(4)); // recién caído: caliente
		}
		level.setBlock(pos, place, 3);
	}

	/** True si ya hay un núcleo de este tipo en la columna dentro del alcance vertical (evita apilar). */
	private boolean meteorInColumn(ServerLevel level, int x, int y, int z, int reach, MeteorType type) {
		for (int yy = y - reach; yy <= y + reach; yy++) {
			if (yy == y) continue;
			if (level.getBlockState(new BlockPos(x, yy, z)).is(type.getCoreBlock())) return true;
		}
		return false;
	}

	/** Acentos de superficie: tierra quemada (meteorito/kreknorite) o nieve (frezarito). */
	private void applySurfaceAccents(ServerLevel level, MeteorType type, int spread) {
		int cx = this.getBlockX();
		int cy = this.getBlockY();
		int cz = this.getBlockZ();
		for (int x = cx - spread; x <= cx + spread; x++) {
			for (int z = cz - spread; z <= cz + spread; z++) {
				for (int y = cy + spread; y >= cy - spread; y--) {
					BlockPos pos = new BlockPos(x, y, z);
					BlockState s = level.getBlockState(pos);
					if (s.isAir() || !level.getBlockState(pos.above()).isAir()) continue;

					if (type == MeteorType.FREZARITE) {
						if (this.random.nextInt(2) == 0) {
							level.setBlock(pos.above(),
									Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + this.random.nextInt(4)), 3);
						}
					} else { // METEORITE / KREKNORITE
						boolean dirt = s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT) || s.is(Blocks.COARSE_DIRT) || s.is(Blocks.PODZOL);
						if (dirt && this.random.nextInt(2) == 0) {
							level.setBlock(pos, ModBlocks.BURNED_EARTH.defaultBlockState(), 3);
						}
					}
					break; // solo la superficie superior de cada columna
				}
			}
		}
	}

	private void spawnCreepers(ServerLevel level, int size) {
		int n = this.random.nextInt(2 + size);
		for (int i = 0; i < n; i++) {
			AlienCreeper creeper = ModEntities.ALIEN_CREEPER.create(level, EntitySpawnReason.MOB_SUMMONED);
			if (creeper != null) {
				double ox = this.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
				double oz = this.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;
				creeper.snapTo(ox, this.getY() + 1.0D, oz, 0.0F, 0.0F);
				level.addFreshEntity(creeper);
			}
		}
	}

	/** El meteoro no recibe daño. */
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	// Entidad transitoria: sin persistencia NBT.
	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		this.entityData.set(DATA_TYPE, input.getIntOr("MeteorType", 0));
		this.entityData.set(DATA_SIZE, input.getIntOr("MeteorSize", 1));
		this.entityData.set(DATA_COMET, input.getBooleanOr("IsComet", false));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putInt("MeteorType", this.entityData.get(DATA_TYPE));
		output.putInt("MeteorSize", this.entityData.get(DATA_SIZE));
		output.putBoolean("IsComet", this.entityData.get(DATA_COMET));
	}

	/** Cráter del meteoro DESCONOCIDO: coraza de meteorito, glowstone y un cofre con loot. */
	private void generateUnknownCrater(ServerLevel level) {
		net.minecraft.core.BlockPos c = this.blockPosition();
		int i = c.getX(), j = c.getY(), k = c.getZ();
		BlockState meteorite = ModBlocks.METEORITE_CORE.defaultBlockState();
		// Coraza 3x3x3 de bloque de meteorito
		for (int dx = -1; dx <= 1; dx++)
			for (int dy = -1; dy <= 1; dy++)
				for (int dz = -1; dz <= 1; dz++)
					level.setBlock(new net.minecraft.core.BlockPos(i + dx, j + dy, k + dz), meteorite, 3);
		// Bloques de frezarito/kreknorita dispersos
		int extra = this.random.nextInt(5) + 4;
		for (int r = 0; r < extra; r++) {
			BlockState b = this.random.nextBoolean()
					? ModBlocks.FREZARITE_CORE.defaultBlockState()
					: ModBlocks.KREKNORITE_CORE.defaultBlockState();
			level.setBlock(new net.minecraft.core.BlockPos(
					i + this.random.nextInt(3) - 1, j + this.random.nextInt(3) - 1, k + this.random.nextInt(3) - 1), b, 3);
		}
		// Glowstone en 6 puntos
		BlockState glow = Blocks.GLOWSTONE.defaultBlockState();
		level.setBlock(new net.minecraft.core.BlockPos(i, j + 2, k), glow, 3);
		level.setBlock(new net.minecraft.core.BlockPos(i, j - 2, k), glow, 3);
		level.setBlock(new net.minecraft.core.BlockPos(i + 2, j, k), glow, 3);
		level.setBlock(new net.minecraft.core.BlockPos(i - 2, j, k), glow, 3);
		level.setBlock(new net.minecraft.core.BlockPos(i, j, k + 2), glow, 3);
		level.setBlock(new net.minecraft.core.BlockPos(i, j, k - 2), glow, 3);
		// Cofre con 8 tiradas de loot en el centro
		net.minecraft.core.BlockPos chestPos = new net.minecraft.core.BlockPos(i, j, k);
		level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
		if (level.getBlockEntity(chestPos) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest) {
			for (int s = 0; s < 8; s++) {
				net.minecraft.world.item.ItemStack loot = unknownLoot();
				if (!loot.isEmpty()) chest.setItem(this.random.nextInt(chest.getContainerSize()), loot);
			}
		}
	}

	private net.minecraft.world.item.ItemStack unknownLoot() {
		net.minecraft.util.RandomSource r = this.random;
		int roll = r.nextInt(60);
		net.minecraft.world.item.Item item;
		int count = 1;
		if (roll < 3)        { item = net.meteor.common.registry.ModItems.METEOR_CHIP; count = r.nextInt(8) + 1; }
		else if (roll < 6)   { item = net.meteor.common.registry.ModItems.FREZARITE_CRYSTAL; count = r.nextInt(8) + 1; }
		else if (roll < 9)   { item = net.meteor.common.registry.ModItems.KREKNORITE_CHIP; count = r.nextInt(8) + 1; }
		else if (roll < 12)  { item = net.meteor.common.registry.ModItems.METEORITE_CHESTPLATE; }
		else if (roll < 15)  { return net.minecraft.world.item.ItemStack.EMPTY; }
		else if (roll < 17)  { item = net.meteor.common.registry.ModItems.KREKNORITE_SWORD; }
		else if (roll < 19)  { return net.minecraft.world.item.ItemStack.EMPTY; }
		else if (roll < 23)  { item = net.meteor.common.registry.ModItems.RED_METEOR_GEM; count = r.nextInt(16) + 1; }
		else if (roll < 26)  { item = net.meteor.common.registry.ModItems.KREKNORITE_CHESTPLATE; }
		else if (roll < 30)  { item = net.minecraft.world.item.Items.DIAMOND; count = r.nextInt(2) + 1; }
		else if (roll < 32)  { item = net.minecraft.world.item.Items.GOLD_INGOT; count = r.nextInt(4) + 1; }
		else if (roll < 35)  { item = net.meteor.common.registry.ModItems.FREZARITE_PICKAXE; }
		else if (roll < 38)  { item = net.meteor.common.registry.ModItems.CHOCOLATE_ICE_CREAM; count = r.nextInt(6) + 1; }
		else if (roll < 39)  { item = net.meteor.common.registry.ModItems.FREZARITE_SHOVEL; }
		else if (roll < 41)  { item = net.meteor.common.registry.ModItems.KREKNORITE_HELMET; }
		else if (roll < 43)  { item = net.meteor.common.registry.ModItems.FREZARITE_HELMET; }
		else if (roll < 45)  { item = net.minecraft.world.item.Items.REDSTONE; count = r.nextInt(16) + 1; }
		else if (roll < 48)  { return net.minecraft.world.item.ItemStack.EMPTY; }
		else if (roll < 50)  { item = net.minecraft.world.item.Items.IRON_BLOCK; count = r.nextInt(5) + 1; }
		else if (roll < 53)  {
			net.minecraft.world.item.Item[] det = {
					net.meteor.common.registry.ModItems.METEOR_DETECTOR_PROXIMITY,
					net.meteor.common.registry.ModItems.METEOR_DETECTOR_TIME,
					net.meteor.common.registry.ModItems.METEOR_DETECTOR_CRASH };
			item = det[r.nextInt(3)];
		} else { return net.minecraft.world.item.ItemStack.EMPTY; }
		return new net.minecraft.world.item.ItemStack(item, count);
	}

}
