package net.meteor.common.menu;

import net.meteor.common.block.FreezingMachineBlockEntity;
import net.meteor.common.registry.ModBlocks;
import net.meteor.common.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FreezingMachineMenu extends AbstractContainerMenu {

	private final Container inv;
	private final ContainerData data;
	private final ContainerLevelAccess access;

	public FreezingMachineMenu(int id, Inventory playerInventory, BlockPos pos) {
		this(id, playerInventory, getBlockEntity(playerInventory, pos), new SimpleContainerData(6));
	}

	public FreezingMachineMenu(int id, Inventory playerInventory, FreezingMachineBlockEntity be, ContainerData data) {
		super(ModMenuTypes.FREEZING_MACHINE, id);
		this.inv = be.getInventory();
		this.data = data;
		this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
		checkContainerSize(this.inv, FreezingMachineBlockEntity.SIZE);

		this.addSlot(new Slot(inv, 0, 73, 17));                    // entrada
		this.addSlot(new Slot(inv, 1, 73, 53));                    // refrigerante
		this.addSlot(new OutputSlot(inv, 2, 133, 35));             // salida (solo extraer)
		this.addSlot(new Slot(inv, 3, 38, 7));                     // cubeta de agua (entrada)
		this.addSlot(new OutputSlot(inv, 4, 38, 60));              // cubo vacío (salida)

		for (int row = 0; row < 3; ++row) {
			for (int col = 0; col < 9; ++col) {
				this.addSlot(new Slot(playerInventory, 9 + row * 9 + col, 8 + col * 18, 84 + row * 18));
			}
		}
		for (int col = 0; col < 9; ++col) {
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}
		this.addDataSlots(data);
	}

	private static FreezingMachineBlockEntity getBlockEntity(Inventory inv, BlockPos pos) {
		BlockEntity be = inv.player.level().getBlockEntity(pos);
		if (be instanceof FreezingMachineBlockEntity m) return m;
		throw new IllegalStateException("Block entity incorrecto en " + pos);
	}

	public int getCoolScaled(int n) {
		int total = this.data.get(1);
		return total == 0 ? 0 : this.data.get(0) * n / total;
	}

	public int getProgressScaled(int n) {
		int total = this.data.get(3);
		return total == 0 ? 0 : this.data.get(2) * n / total;
	}

	public int getWaterScaled(int n) {
		int total = this.data.get(5);
		return total == 0 ? 0 : this.data.get(4) * n / total;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.FREEZING_MACHINE);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			copy = stack.copy();
			int machineEnd = FreezingMachineBlockEntity.SIZE;   // 3
			int invEnd = machineEnd + 36;
			if (index < machineEnd) {
				if (!this.moveItemStackTo(stack, machineEnd, invEnd, true)) return ItemStack.EMPTY;
			} else {
				// jugador -> cubeta / refrigerante / entrada
				if (stack.is(net.minecraft.world.item.Items.WATER_BUCKET)) {
					if (!this.moveItemStackTo(stack, 3, 4, false)) return ItemStack.EMPTY;
				} else if (FreezingMachineBlockEntity.coolValue(stack) > 0) {
					if (!this.moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
				} else if (!this.moveItemStackTo(stack, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
		}
		return copy;
	}

	private static class OutputSlot extends Slot {
		OutputSlot(Container c, int index, int x, int y) { super(c, index, x, y); }
		@Override public boolean mayPlace(ItemStack stack) { return false; }
	}
}
