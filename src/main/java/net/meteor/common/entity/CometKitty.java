package net.meteor.common.entity;

import net.meteor.common.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Comet Kitty: un gato espacial domesticable e inmune al fuego.
 *
 * Se basa en {@link Cat} para heredar domesticación, cría y comportamiento.
 * La salud (16) y la inmunidad al fuego se configuran en el registro.
 * El botín (gema roja) va por loot table.
 */
public class CometKitty extends Cat {

	public CometKitty(EntityType<? extends Cat> type, Level level) {
		super(type, level);
	}

	/**
	 * Cría: 50% Comet Kitty (gato espacial), 50% gato normal, como en el original.
	 * Hereda la domesticación y el dueño del progenitor.
	 */
	@Override
	public @Nullable Cat getBreedOffspring(ServerLevel level, AgeableMob other) {
		Cat baby = this.random.nextBoolean()
				? ModEntities.COMET_KITTY.create(level, EntitySpawnReason.BREEDING)
				: EntityType.CAT.create(level, EntitySpawnReason.BREEDING);

		if (baby != null && this.isTame()) {
			baby.setOwnerReference(this.getOwnerReference());
			baby.setTame(true, true);
		}
		return baby;
	}
}
