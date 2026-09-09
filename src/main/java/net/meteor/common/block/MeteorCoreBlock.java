package net.meteor.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Bloque núcleo de meteoro con estado de "calor" (0-4). Recién caído sale
 * caliente (incandescente), quema a quien lo pisa y se va enfriando con el
 * tiempo (tick aleatorio) hasta heat=0.
 */
public class MeteorCoreBlock extends Block {

	public static final IntegerProperty HEAT = IntegerProperty.create("heat", 0, 4);

	public MeteorCoreBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(HEAT, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HEAT);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int heat = state.getValue(HEAT);
		if (heat > 0) {
			level.setBlock(pos, state.setValue(HEAT, heat - 1), 3);
		}
	}

	/** Polvo oscuro y humo mientras el núcleo sigue incandescente. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		int heat = state.getValue(HEAT);
		if (heat <= 0) return;

		for (int i = 0; i < heat; i++) {
			double x = pos.getX() + random.nextDouble();
			double y = pos.getY() + 1.0D + random.nextDouble() * 0.2D;
			double z = pos.getZ() + random.nextDouble();
			level.addParticle(ModParticleEffects.METEOR_DUST, x, y, z, 0.0D, 0.0D, 0.0D);
		}
		if (random.nextInt(3) == 0) {
			level.addParticle(ParticleTypes.LARGE_SMOKE,
					pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
		}
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		int heat = state.getValue(HEAT);
		if (heat > 0 && !entity.fireImmune()) {
			entity.igniteForSeconds(heat);
		}
		super.stepOn(level, pos, state, entity);
	}
}
