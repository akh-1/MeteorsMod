package net.meteor.common.block;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.meteor.common.MeteorType;
import net.meteor.common.ShieldRegistry;
import net.meteor.common.menu.MeteorShieldMenu;
import net.meteor.common.registry.ModBlockEntities;
import net.meteor.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Escudo anti-meteoro. Inventario de 13 slots:
 *  - 0: chip de carga
 *  - 1-4: gemas de poder (cada gema sube 1 nivel)
 *  - 5-12: materiales recogidos de los meteoros bloqueados (solo extraer)
 * Potencia = 1 + nº de gemas (máx 5); rango = potencia × config. Dueño persistente.
 *
 * Además del bloqueo de meteoros lleva, como el original, el detector de
 * cometas (coordenadas del último cometa avistado en su radio) y el interruptor
 * para impedir que los cometas entren en su zona.
 */
public class MeteorShieldBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

	public static final int MAX_LEVEL = 5;
	public static final int RANGE_PER_LEVEL = 64;
	public static final int SIZE = 13;
	public static final int GEM_START = 1, GEM_END = 4, GATHERED_START = 5, GATHERED_END = 12;
	public static final int CHARGE_TIME = 2400;   // 120 s recién colocado

	/** Id del botón "bloquear cometas" (viaja por el paquete vanilla de botón de menú). */
	public static final int BUTTON_TOGGLE_COMETS = 0;

	private final SimpleContainer inv = new SimpleContainer(SIZE);
	private int powerLevel = 1;
	private int tickCounter = 0;
	private boolean charged = false;
	private int age = 0;          // ticks de carga acumulados
	private String owner = "None";

	// Detector de cometas
	private boolean blockComets = false;
	private int cometX, cometZ;
	private int cometType = -1;

	private final ContainerData data = new ContainerData() {
		@Override public int get(int index) {
			return switch (index) {
				case 0 -> charged ? powerLevel : 0;
				case 1 -> getRange();
				case 2 -> charged ? 1 : 0;
				case 3 -> charged ? 100 : (int) ((long) age * 100 / CHARGE_TIME);
				case 4 -> blockComets ? 1 : 0;
				case 5 -> cometX;
				case 6 -> cometZ;
				case 7 -> cometType;
				default -> 0;
			};
		}
		@Override public void set(int index, int value) { if (index == 0) powerLevel = value; }
		@Override public int getCount() { return 8; }
	};

	public MeteorShieldBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.METEOR_SHIELD, pos, state);
	}

	public int getRange() { return charged ? powerLevel * net.meteor.common.MeteorsConfig.INSTANCE.shieldRangePerLevel : 0; }
	public int getPowerLevel() { return powerLevel; }
	public boolean isCharged() { return charged; }
	public String getOwner() { return owner; }
	public boolean getBlockComets() { return blockComets; }

	public void setOwner(String name) {
		this.owner = name;
		setChanged();
		sync();
	}

	/** Alterna el bloqueo de cometas (botón de la interfaz). */
	public void toggleBlockComets() {
		this.blockComets = !this.blockComets;
		setChanged();
		if (this.level instanceof ServerLevel sl && charged) {
			ShieldRegistry.add(sl, this.worldPosition, getRange(), owner, blockComets);
		}
		sync();
	}

	/** Registra el avistamiento de un cometa dentro del radio. */
	public void reportComet(int x, int z, MeteorType type) {
		this.cometX = x;
		this.cometZ = z;
		this.cometType = type.getId();
		setChanged();
		sync();
	}

	public boolean hasComet() { return cometType >= 0; }
	public int getCometX() { return cometX; }
	public int getCometZ() { return cometZ; }
	public MeteorType getCometType() { return MeteorType.byId(Math.max(0, cometType)); }

	public SimpleContainer getInventory() { return inv; }
	public ContainerData getData() { return data; }

	private void sync() {
		if (this.level != null && !this.level.isClientSide()) {
			this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	private void recomputePower() {
		int gems = 0;
		for (int i = GEM_START; i <= GEM_END; i++) {
			if (inv.getItem(i).is(ModItems.RED_METEOR_GEM)) gems++;
		}
		powerLevel = Math.min(MAX_LEVEL, 1 + gems);
	}

	/** Deposita un material recogido en el primer hueco libre de la rejilla 5-12. */
	public void depositMaterial(ItemStack stack) {
		for (int i = GATHERED_START; i <= GATHERED_END; i++) {
			ItemStack s = inv.getItem(i);
			if (s.isEmpty()) { inv.setItem(i, stack.copy()); setChanged(); return; }
			if (ItemStack.isSameItemSameComponents(s, stack) && s.getCount() < s.getMaxStackSize()) {
				s.grow(1); setChanged(); return;
			}
		}
	}

	@Override
	public void setLevel(Level level) {
		super.setLevel(level);
		if (level instanceof ServerLevel sl && charged) {
			ShieldRegistry.add(sl, this.worldPosition, getRange(), owner, blockComets);
		}
	}

	@Override
	public void setRemoved() {
		if (this.level instanceof ServerLevel sl) ShieldRegistry.remove(sl, this.worldPosition);
		super.setRemoved();
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null) Containers.dropContents(this.level, pos, this.inv);
		super.preRemoveSideEffects(pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MeteorShieldBlockEntity be) {
		if (++be.tickCounter % 20 != 0) return;
		int prev = be.powerLevel;
		boolean prevCharged = be.charged;
		be.recomputePower();

		// Carga: el escudo recién colocado tarda CHARGE_TIME en activarse.
		if (!be.charged) {
			ItemStack chip = be.inv.getItem(0);   // slot 0 = chip de carga (acelera al instante)
			if (chip.is(ModItems.METEOR_CHIP)) {
				chip.shrink(1);
				be.charged = true;
				be.setChanged();
				level.playSound(null, pos, net.meteor.common.registry.ModSounds.SHIELD_POWERUP, net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, 1.0F);
			} else {
				be.age += 20;
				if (be.age >= CHARGE_TIME) { be.charged = true; be.setChanged(); }
			}
		}

		if (be.charged && net.meteor.common.MeteorsConfig.INSTANCE.shieldHumSound && level.random.nextInt(120) == 0) {
			level.playSound(null, pos, net.meteor.common.registry.ModSounds.SHIELD_HUMM, net.minecraft.sounds.SoundSource.BLOCKS, 0.4F, 1.0F);
		}

		if (level instanceof ServerLevel sl) {
			if (be.charged) ShieldRegistry.add(sl, pos, be.getRange(), be.owner, be.blockComets);
			else ShieldRegistry.remove(sl, pos);
		}

		if (prev != be.powerLevel) {
			if (be.powerLevel > prev) {
				level.playSound(null, pos, net.meteor.common.registry.ModSounds.SHIELD_POWERUP,
						net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, be.powerLevel / 10.0F + 0.5F);
			} else {
				level.playSound(null, pos, net.meteor.common.registry.ModSounds.SHIELD_POWERDOWN,
						net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
			}
		}

		// El renderer dibuja las capas giratorias a partir de charged y powerLevel,
		// así que cualquier cambio hay que EMPUJARLO al cliente. setChanged() solo
		// marca el chunk para guardar; sin esto las capas no aparecen hasta que el
		// chunk se reenvía entero (reconectar, recargar, alejarse y volver).
		if (prev != be.powerLevel || prevCharged != be.charged) {
			be.setChanged();
			be.sync();
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putString("Owner", owner);
		output.putBoolean("Charged", charged);
		output.putInt("Age", age);
		output.putBoolean("BlockComets", blockComets);
		output.putInt("CometX", cometX);
		output.putInt("CometZ", cometZ);
		output.putInt("CometType", cometType);
		for (int i = 0; i < SIZE; i++) {
			ItemStack s = inv.getItem(i);
			if (!s.isEmpty()) output.store("Slot" + i, ItemStack.CODEC, s);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.owner = input.getStringOr("Owner", "None");
		this.charged = input.getBooleanOr("Charged", false);
		this.age = input.getIntOr("Age", 0);
		this.blockComets = input.getBooleanOr("BlockComets", false);
		this.cometX = input.getIntOr("CometX", 0);
		this.cometZ = input.getIntOr("CometZ", 0);
		this.cometType = input.getIntOr("CometType", -1);
		for (int i = 0; i < SIZE; i++) {
			inv.setItem(i, input.read("Slot" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
		}
		recomputePower();
	}

	// --- Sincronización cliente ---
	@Override
	public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return saveWithoutMetadata(registries);
	}

	// --- MenuProvider / ExtendedScreenHandlerFactory ---
	@Override
	public Component getDisplayName() { return Component.translatable("container.meteors.meteor_shield"); }

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
		return new MeteorShieldMenu(id, playerInventory, this, this.data);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) { return this.worldPosition; }
}
