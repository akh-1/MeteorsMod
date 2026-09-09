package net.meteor.common.registry;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.meteor.common.MeteorsMod;
import net.meteor.common.menu.FreezingMachineMenu;
import net.meteor.common.menu.MeteorShieldMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {

	private ModMenuTypes() {}

	public static final MenuType<MeteorShieldMenu> METEOR_SHIELD = Registry.register(
			BuiltInRegistries.MENU, MeteorsMod.id("meteor_shield"),
			new ExtendedScreenHandlerType<>(MeteorShieldMenu::new, BlockPos.STREAM_CODEC));

	public static final MenuType<FreezingMachineMenu> FREEZING_MACHINE = Registry.register(
			BuiltInRegistries.MENU, MeteorsMod.id("freezing_machine"),
			new ExtendedScreenHandlerType<>(FreezingMachineMenu::new, BlockPos.STREAM_CODEC));

	public static void register() {
		MeteorsMod.LOGGER.debug("Registrando menús de Falling Meteors");
	}
}
