package net.meteor.common.registry;

import net.meteor.common.MeteorsMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Sonidos del mod (impacto, escudo). */
public final class ModSounds {

	public static final SoundEvent METEOR_CRASH = register("meteor.crash");
	public static final SoundEvent SHIELD_POWERUP = register("shield.powerup");
	public static final SoundEvent SHIELD_POWERDOWN = register("shield.powerdown");
	public static final SoundEvent SHIELD_HUMM = register("shield.humm");

	private static SoundEvent register(String path) {
		Identifier id = MeteorsMod.id(path);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void register() {
		MeteorsMod.LOGGER.debug("Sonidos de Meteors registrados.");
	}

	private ModSounds() {}
}
