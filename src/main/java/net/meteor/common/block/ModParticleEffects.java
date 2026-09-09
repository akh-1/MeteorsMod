package net.meteor.common.block;

import net.minecraft.core.particles.DustParticleOptions;

/**
 * Colores de las partículas de polvo del mod, equivalentes a las tres
 * partículas propias del original sin necesidad de registrar tipos nuevos:
 *  - "meteordust": polvo casi negro del meteorito y de la antorcha del escudo.
 *  - "frezadust": polvo turquesa del frezarito y de la congeladora.
 */
public final class ModParticleEffects {

	private ModParticleEffects() {}

	public static final DustParticleOptions METEOR_DUST = new DustParticleOptions(0x171717, 1.0F);
	public static final DustParticleOptions FREZA_DUST = new DustParticleOptions(0x33FFCC, 1.0F);
}
