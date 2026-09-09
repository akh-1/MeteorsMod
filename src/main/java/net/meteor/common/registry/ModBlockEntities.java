package net.meteor.common.registry;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.meteor.common.MeteorsMod;
import net.meteor.common.block.FreezingMachineBlockEntity;
import net.meteor.common.block.MeteorTimerBlockEntity;
import net.meteor.common.block.MeteorShieldBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

	private ModBlockEntities() {}

	public static final BlockEntityType<MeteorShieldBlockEntity> METEOR_SHIELD = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, MeteorsMod.id("meteor_shield"),
			FabricBlockEntityTypeBuilder.create(MeteorShieldBlockEntity::new, ModBlocks.METEOR_SHIELD).build());

	public static final BlockEntityType<FreezingMachineBlockEntity> FREEZING_MACHINE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, MeteorsMod.id("freezing_machine"),
			FabricBlockEntityTypeBuilder.create(FreezingMachineBlockEntity::new, ModBlocks.FREEZING_MACHINE).build());

	public static final BlockEntityType<MeteorTimerBlockEntity> METEOR_TIMER = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, MeteorsMod.id("meteor_timer"),
			FabricBlockEntityTypeBuilder.create(MeteorTimerBlockEntity::new, ModBlocks.METEOR_TIMER).build());

	public static void register() {
		MeteorsMod.LOGGER.debug("Registrando block entities de Falling Meteors");
	}
}
