package net.meteor.common.registry;

import net.meteor.common.MeteorsMod;
import net.meteor.common.block.MeteorCoreBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

/**
 * Registro de bloques del mod. Cada bloque obtiene automáticamente su
 * {@link BlockItem} con el mismo identificador.
 *
 * Los valores de dureza/resistencia replican los del mod original 1.7.10.
 */
public final class ModBlocks {

	private ModBlocks() {}

	// --- Menas (solo generan por worldgen, no en cráteres) ---
	public static final Block METEORITE_ORE = register("meteorite_ore", Block::new,
			BlockBehaviour.Properties.of().strength(10f, 200f).sound(SoundType.STONE).requiresCorrectToolForDrops());
	public static final Block FREZARITE_ORE = register("frezarite_ore", Block::new,
			BlockBehaviour.Properties.of().strength(10f, 200f).sound(SoundType.STONE).requiresCorrectToolForDrops());

	// --- Cuerpos de meteoro (forman el cráter; sueltan fragmentos al picar) ---
	// Meteorito y Kreknorite tienen estado de calor (queman al pisar recién caídos).
	public static final Block METEORITE_CORE = register("meteorite_core", MeteorCoreBlock::new,
			BlockBehaviour.Properties.of().strength(10f, 200f).sound(SoundType.STONE).randomTicks()
					.lightLevel(s -> s.getValue(MeteorCoreBlock.HEAT) > 0 ? 13 : 8).requiresCorrectToolForDrops());
	public static final Block FREZARITE_CORE = register("frezarite_core", Block::new,
			BlockBehaviour.Properties.of().strength(8.5f, 150f).sound(SoundType.GLASS).lightLevel(s -> 4).requiresCorrectToolForDrops());
	public static final Block KREKNORITE_CORE = register("kreknorite_core", MeteorCoreBlock::new,
			BlockBehaviour.Properties.of().strength(11f, 350f).sound(SoundType.STONE).randomTicks()
					.lightLevel(s -> s.getValue(MeteorCoreBlock.HEAT) > 0 ? 15 : 11).requiresCorrectToolForDrops());

	// --- Bloque del cráter (efecto del impacto) ---
	public static final Block BURNED_EARTH = register("burned_earth", Block::new,
			BlockBehaviour.Properties.of().strength(0.6f).sound(SoundType.GRAVEL));

	// --- Escudo anti-meteoro ---
	public static final Block METEOR_TIMER = register("meteor_timer",
			net.meteor.common.block.BlockMeteorTimer::new,
			BlockBehaviour.Properties.of().strength(3f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion());

	public static final Block SLIPPERY_BLOCK = register("slippery_block", Block::new,
			BlockBehaviour.Properties.of().strength(0.5f).friction(0.98f).sound(SoundType.GLASS));
	public static final Block SLIPPERY_STAIRS = register("slippery_stairs",
			p -> new net.minecraft.world.level.block.StairBlock(SLIPPERY_BLOCK.defaultBlockState(), p),
			BlockBehaviour.Properties.of().strength(0.5f).friction(0.98f).sound(SoundType.GLASS));

	public static final Block FREEZING_MACHINE = register("freezing_machine",
			net.meteor.common.block.BlockFreezingMachine::new,
			BlockBehaviour.Properties.of().strength(4f, 1200f).sound(SoundType.METAL).requiresCorrectToolForDrops());

	public static final Block METEOR_SHIELD_TORCH = register("meteor_shield_torch",
			net.meteor.common.block.BlockMeteorShieldTorch::new,
			BlockBehaviour.Properties.of().instabreak().sound(SoundType.WOOD)
					.lightLevel(st -> st.getValue(net.meteor.common.block.BlockMeteorShieldTorch.LIT) ? 14 : 2));

	public static final Block METEOR_SHIELD = register("meteor_shield",
			net.meteor.common.block.MeteorShieldBlock::new,
			BlockBehaviour.Properties.of().strength(5f, 1200f).sound(SoundType.METAL).lightLevel(s -> 7).requiresCorrectToolForDrops().noOcclusion());

	// --- Bloques de almacenamiento (el "BlockDecoration" con metadata del original) ---
	public static final Block METEORITE_BLOCK = register("meteorite_block", Block::new,
			BlockBehaviour.Properties.of().strength(5f, 10f).sound(SoundType.METAL).requiresCorrectToolForDrops());
	public static final Block FROZEN_IRON_BLOCK = register("frozen_iron_block", Block::new,
			BlockBehaviour.Properties.of().strength(5f, 10f).sound(SoundType.METAL).requiresCorrectToolForDrops());
	public static final Block KREKNORITE_BLOCK = register("kreknorite_block", Block::new,
			BlockBehaviour.Properties.of().strength(5f, 10f).sound(SoundType.METAL).requiresCorrectToolForDrops());
	public static final Block RED_METEOR_GEM_BLOCK = register("red_meteor_gem_block", Block::new,
			BlockBehaviour.Properties.of().strength(5f, 10f).sound(SoundType.METAL).requiresCorrectToolForDrops());

	/** Helper de registro: crea el bloque, su BlockItem, y registra ambos (1.21.2+). */
	public static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, MeteorsMod.id(name));
		Block block = factory.apply(props.setId(blockKey));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, MeteorsMod.id(name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	public static void register() {
		MeteorsMod.LOGGER.debug("Registrando bloques de Falling Meteors");
	}
}
