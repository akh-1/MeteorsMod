package net.meteor.common.block;

import net.meteor.common.climate.GhostMeteor;
import net.meteor.common.climate.MeteorForecast;
import net.meteor.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity del Meteor Timer: emite redstone según cuánto falta para el
 * próximo meteoro PROGRAMADO, leído del pronóstico persistente.
 */
public class MeteorTimerBlockEntity extends BlockEntity {

	private int power = 0;
	private boolean quickMode = false;

	public MeteorTimerBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntities.METEOR_TIMER, pos, state);
	}

	public int getPower() {
		return power;
	}

	/** Alterna entre rampa analógica y pulso, devuelve el nuevo estado. */
	public boolean toggleQuickMode() {
		this.quickMode = !this.quickMode;
		setChanged();
		return this.quickMode;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, MeteorTimerBlockEntity be) {
		if (level.getGameTime() % 20L != 0L) return;   // una vez por segundo
		if (!(level instanceof ServerLevel sl)) return;

		GhostMeteor soonest = MeteorForecast.get(sl).getSoonestMeteor();

		int newPower;
		if (soonest == null) {
			newPower = 0;
		} else {
			// Igual que el original: sube de 0 a 15 en los últimos ~450 s antes del impacto.
			int calc = Math.min(15, soonest.getSecondsLeft() / 30);
			int ramp = Mth.clamp(15 - calc, 0, 15);
			newPower = be.quickMode ? (ramp >= 15 ? 15 : 0) : ramp;
		}

		if (newPower != be.power) {
			be.power = newPower;
			be.setChanged();
			level.updateNeighborsAt(pos, state.getBlock());
		}
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putBoolean("QuickMode", quickMode);
		output.putInt("Power", power);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.quickMode = input.getBooleanOr("QuickMode", false);
		this.power = input.getIntOr("Power", 0);
	}
}
