package net.meteor.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;

/**
 * Creeper alienígena: siempre cargado (como si lo hubiera alcanzado un rayo),
 * inmune al fuego (vía EntityType) y huye de los Comet Kitty.
 */
public class AlienCreeper extends Creeper {

	public AlienCreeper(EntityType<? extends Creeper> type, Level level) {
		super(type, level);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, CometKitty.class, 6.0F, 0.25, 0.3));
	}

	@Override
	public boolean isPowered() {
		return true;
	}
}
