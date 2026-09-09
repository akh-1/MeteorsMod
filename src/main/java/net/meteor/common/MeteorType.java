package net.meteor.common;

import net.meteor.common.registry.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.Locale;

/**
 * Tipos de meteoro. Define el bloque núcleo del cráter, si la explosión es ígnea
 * y si spawnea creepers. El tipo KITTY no hace cráter: spawnea gatos espaciales.
 */
public enum MeteorType {
	METEORITE(0, true, true),
	FREZARITE(1, false, false),
	KREKNORITE(2, true, false),
	KITTY(3, false, false),
	UNKNOWN(4, true, false);

	private final int id;
	private final boolean fiery;
	private final boolean spawnsCreepers;

	MeteorType(int id, boolean fiery, boolean spawnsCreepers) {
		this.id = id;
		this.fiery = fiery;
		this.spawnsCreepers = spawnsCreepers;
	}

	public int getId() {
		return id;
	}

	public boolean isFiery() {
		return fiery;
	}

	public boolean spawnsCreepers() {
		return spawnsCreepers;
	}

	/** Bloque núcleo del cráter. El Kitty no tiene cuerpo propio; se ve como meteorito al caer. */
	public Block getCoreBlock() {
		return switch (this) {
			case METEORITE, KITTY, UNKNOWN -> ModBlocks.METEORITE_CORE;
			case FREZARITE -> ModBlocks.FREZARITE_CORE;
			case KREKNORITE -> ModBlocks.KREKNORITE_CORE;
		};
	}

	/** Nombre traducible del tipo, p.ej. {@code meteor.meteors.frezarite}. */
	public Component getDisplayName() {
		return Component.translatable("meteor.meteors." + name().toLowerCase(Locale.ROOT));
	}

	public static MeteorType byId(int id) {
		return switch (id) {
			case 1 -> FREZARITE;
			case 2 -> KREKNORITE;
			case 3 -> KITTY;
			case 4 -> UNKNOWN;
			default -> METEORITE;
		};
	}
}
