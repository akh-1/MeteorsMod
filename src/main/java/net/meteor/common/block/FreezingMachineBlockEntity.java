package net.meteor.common.block;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.meteor.common.menu.FreezingMachineMenu;
import net.meteor.common.registry.ModBlockEntities;
import net.meteor.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Máquina congeladora: tipo horno con "refrigerante" en vez de combustible y un
 * tanque de agua que se llena con cubetas.
 *  Slot 0 = entrada, 1 = refrigerante, 2 = salida, 3 = cubeta de agua, 4 = cubo vacío.
 * Recetas: hierro->hierro congelado, hielo(+agua)->hielo compacto, hielo compacto->hielo azul,
 *          agua(tanque)->hielo.
 */
public class FreezingMachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<BlockPos> {

	public static final int SIZE = 5;
	public static final int TANK_MAX = 4000;   // 4 cubetas
	private static final int BUCKET = 1000;
	private static final int WATER_PER_OP = 250;   // agua consumida por congelado

	private final SimpleContainer inv = new SimpleContainer(SIZE);
	private int coolTime = 0, coolTimeTotal = 0, cookTime = 0;
	private final int cookTimeTotal = 200;
	private int waterAmount = 0;

	private final ContainerData data = new ContainerData() {
		@Override public int get(int i) {
			return switch (i) {
				case 0 -> coolTime; case 1 -> coolTimeTotal; case 2 -> cookTime;
				case 3 -> cookTimeTotal; case 4 -> waterAmount; case 5 -> TANK_MAX; default -> 0;
			};
		}
		@Override public void set(int i, int v) {
			switch (i) { case 0 -> coolTime = v; case 1 -> coolTimeTotal = v; case 2 -> cookTime = v; case 4 -> waterAmount = v; }
		}
		@Override public int getCount() { return 6; }
	};

	public FreezingMachineBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.FREEZING_MACHINE, pos, state);
	}

	/** True si hay frío disponible y una operación en curso (para las partículas). */
	public boolean isWorking() { return coolTime > 0 && cookTime > 0; }

	public SimpleContainer getInventory() { return inv; }
	public ContainerData getData() { return data; }

	/** El refrigerante es el cristal de frezarito (aporta tiempo de frío). */
	public static int coolValue(ItemStack s) {
		return s.is(ModItems.FREZARITE_CRYSTAL) ? 1000 : 0;
	}

	/** Resultado de congelar el item del slot de entrada (vacío si no hay receta). */
	private static ItemStack freezeResult(ItemStack in) {
		if (in.is(Items.IRON_INGOT)) return new ItemStack(ModItems.FROZEN_IRON);
		if (in.is(Items.ICE)) return new ItemStack(Items.PACKED_ICE);
		if (in.is(Items.PACKED_ICE)) return new ItemStack(Items.BLUE_ICE);
		if (in.is(Items.WATER_BUCKET)) return new ItemStack(Items.ICE);   // agua en el slot de entrada -> hielo
		if (in.is(Items.LAVA_BUCKET)) return new ItemStack(Items.OBSIDIAN);   // como el original: lava -> obsidiana
		return ItemStack.EMPTY;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, FreezingMachineBlockEntity be) {
		if (be.coolTime > 0) be.coolTime--;

		// Llenado del tanque con cubetas de agua (slot 3 -> slot 4 cubo vacío)
		ItemStack bucketIn = be.inv.getItem(3);
		ItemStack bucketOut = be.inv.getItem(4);
		if (bucketIn.is(Items.WATER_BUCKET) && be.waterAmount + BUCKET <= TANK_MAX && canAddBucket(bucketOut)) {
			be.waterAmount += BUCKET;
			bucketIn.shrink(1);
			addBucket(be, bucketOut);
			be.setChanged();
		}

		ItemStack input = be.inv.getItem(0);
		ItemStack result = freezeResult(input);
		boolean waterFreeze = input.is(Items.WATER_BUCKET) || input.is(Items.LAVA_BUCKET);   // devuelve cubo vacío
		ItemStack out = be.inv.getItem(2);

		boolean outOk = !result.isEmpty() && (out.isEmpty()
				|| (ItemStack.isSameItemSameComponents(out, result) && out.getCount() + result.getCount() <= out.getMaxStackSize()));
		boolean bucketOk = !waterFreeze || canAddBucket(be.inv.getItem(4));
		// La máquina NECESITA agua en el tanque para congelar cualquier cosa
		boolean canFreeze = outOk && bucketOk && be.waterAmount >= WATER_PER_OP;

		// Recargar frío con cristal de frezarito
		ItemStack coolant = be.inv.getItem(1);
		if (be.coolTime == 0 && canFreeze && coolValue(coolant) > 0) {
			be.coolTime = be.coolTimeTotal = coolValue(coolant);
			coolant.shrink(1);
			be.setChanged();
		}

		if (be.coolTime > 0 && canFreeze) {
			be.cookTime++;
			if (be.cookTime >= be.cookTimeTotal) {
				be.cookTime = 0;
				if (out.isEmpty()) be.inv.setItem(2, result.copy());
				else out.grow(result.getCount());
				input.shrink(1);
				be.waterAmount -= WATER_PER_OP;
				if (waterFreeze) addBucket(be, be.inv.getItem(4));   // devuelve cubo vacío
			}
			be.setChanged();
		} else if (be.cookTime > 0) {
			be.cookTime = Math.max(0, be.cookTime - 2);
		}

		// Estado visible (partículas de frío) sincronizado al cliente.
		boolean lit = be.isWorking();
		if (state.hasProperty(BlockFreezingMachine.LIT) && state.getValue(BlockFreezingMachine.LIT) != lit) {
			level.setBlock(pos, state.setValue(BlockFreezingMachine.LIT, lit), 3);
		}
	}

	private static boolean canAddBucket(ItemStack bucketOut) {
		return bucketOut.isEmpty() || (bucketOut.is(Items.BUCKET) && bucketOut.getCount() < bucketOut.getMaxStackSize());
	}

	private static void addBucket(FreezingMachineBlockEntity be, ItemStack bucketOut) {
		if (bucketOut.isEmpty()) be.inv.setItem(4, new ItemStack(Items.BUCKET));
		else bucketOut.grow(1);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		if (this.level != null) Containers.dropContents(this.level, pos, this.inv);
		super.preRemoveSideEffects(pos, state);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("CoolTime", coolTime);
		output.putInt("CoolTimeTotal", coolTimeTotal);
		output.putInt("CookTime", cookTime);
		output.putInt("Water", waterAmount);
		for (int i = 0; i < SIZE; i++) {
			ItemStack s = inv.getItem(i);
			if (!s.isEmpty()) output.store("Slot" + i, ItemStack.CODEC, s);
		}
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		coolTime = input.getIntOr("CoolTime", 0);
		coolTimeTotal = input.getIntOr("CoolTimeTotal", 0);
		cookTime = input.getIntOr("CookTime", 0);
		waterAmount = input.getIntOr("Water", 0);
		for (int i = 0; i < SIZE; i++) {
			inv.setItem(i, input.read("Slot" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
		}
	}

	@Override
	public Component getDisplayName() { return Component.translatable("container.meteors.freezing_machine"); }

	@Nullable
	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
		return new FreezingMachineMenu(id, playerInventory, this, this.data);
	}

	@Override
	public BlockPos getScreenOpeningData(ServerPlayer player) { return this.worldPosition; }
}
