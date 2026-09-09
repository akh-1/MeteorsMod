package net.meteor.common.menu;

import net.meteor.common.block.MeteorShieldBlockEntity;
import net.meteor.common.registry.ModBlocks;
import net.meteor.common.registry.ModItems;
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

public class MeteorShieldMenu extends AbstractContainerMenu {

	private final Container inv;
	private final ContainerData data;
	private final ContainerLevelAccess access;
	public final MeteorShieldBlockEntity blockEntity;

	public MeteorShieldMenu(int id, Inventory playerInventory, BlockPos pos) {
		this(id, playerInventory, getBlockEntity(playerInventory, pos), new SimpleContainerData(8));
	}

	public MeteorShieldMenu(int id, Inventory playerInventory, MeteorShieldBlockEntity be, ContainerData data) {
		super(ModMenuTypes.METEOR_SHIELD, id);
		this.blockEntity = be;
		this.inv = be.getInventory();
		this.data = data;
		this.access = ContainerLevelAccess.create(be.getLevel(), be.getBlockPos());
		checkContainerSize(this.inv, MeteorShieldBlockEntity.SIZE);

		// Slot 0: chip de carga (un solo chip)
		this.addSlot(new SingleSlot(inv, 0, 47, 60, ModItems.METEOR_CHIP));
		// Slots 1-4: gemas de poder (una gema cada uno)
		for (int i = 0; i < 4; i++) {
			this.addSlot(new SingleSlot(inv, i + 1, 67 + i * 29, 60, ModItems.RED_METEOR_GEM));
		}
		// Slots 5-12: materiales recogidos (solo extraer), rejilla 2x4
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				this.addSlot(new TakeOnlySlot(inv, i * 2 + j + 5, 8 + j * 18, 6 + i * 18));
			}
		}

		// Inventario del jugador
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

	private static MeteorShieldBlockEntity getBlockEntity(Inventory inv, BlockPos pos) {
		BlockEntity be = inv.player.level().getBlockEntity(pos);
		if (be instanceof MeteorShieldBlockEntity s) return s;
		throw new IllegalStateException("Block entity incorrecto en " + pos);
	}

	public int getPowerLevel() { return this.data.get(0); }
	public int getRange() { return this.data.get(1); }
	public String getOwner() { return this.blockEntity.getOwner(); }
	public boolean getCharged() { return this.data.get(2) == 1; }
	public int getChargePercent() { return this.data.get(3); }
	public boolean getBlockComets() { return this.data.get(4) == 1; }
	public boolean hasComet() { return this.data.get(7) >= 0; }
	public int getCometX() { return this.data.get(5); }
	public int getCometZ() { return this.data.get(6); }
	public net.meteor.common.MeteorType getCometType() {
		return net.meteor.common.MeteorType.byId(Math.max(0, this.data.get(7)));
	}

	/** Botón "bloquear cometas": viaja por el paquete vanilla de botón de menú. */
	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == MeteorShieldBlockEntity.BUTTON_TOGGLE_COMETS) {
			this.blockEntity.toggleBlockComets();
			return true;
		}
		return false;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(this.access, player, ModBlocks.METEOR_SHIELD);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			copy = stack.copy();
			int shieldEnd = MeteorShieldBlockEntity.SIZE;       // 13
			int invEnd = shieldEnd + 36;                        // 49
			if (index < shieldEnd) {
				if (!this.moveItemStackTo(stack, shieldEnd, invEnd, true)) return ItemStack.EMPTY;
			} else if (stack.is(ModItems.METEOR_CHIP)) {
				if (!this.moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if (stack.is(ModItems.RED_METEOR_GEM)) {
				if (!this.moveItemStackTo(stack, 1, 5, false)) return ItemStack.EMPTY;
			} else {
				return ItemStack.EMPTY;
			}
			if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
			else slot.setChanged();
		}
		return copy;
	}

	/** Slot que solo admite un item concreto y como mucho 1 unidad. */
	private static class SingleSlot extends Slot {
		private final net.minecraft.world.item.Item allowed;
		SingleSlot(Container c, int index, int x, int y, net.minecraft.world.item.Item allowed) {
			super(c, index, x, y);
			this.allowed = allowed;
		}
		@Override public boolean mayPlace(ItemStack stack) { return stack.is(allowed); }
		@Override public int getMaxStackSize() { return 1; }
	}

	/** Slot de solo extracción (materiales recogidos). */
	private static class TakeOnlySlot extends Slot {
		TakeOnlySlot(Container c, int index, int x, int y) { super(c, index, x, y); }
		@Override public boolean mayPlace(ItemStack stack) { return false; }
	}
}
