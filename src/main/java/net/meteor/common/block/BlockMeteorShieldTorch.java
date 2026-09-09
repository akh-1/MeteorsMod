package net.meteor.common.block;

import net.meteor.common.ShieldRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Antorcha del escudo: se enciende cuando su posición está dentro del radio de un escudo activo. */
public class BlockMeteorShieldTorch extends Block {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;
	private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 10, 10);

	public BlockMeteorShieldTorch(Properties properties) {
		super(properties);
		registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.FALSE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return Shapes.empty();
	}

	/** Llama de polvo de meteorito en la punta cuando la antorcha está encendida. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;

		double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
		double y = pos.getY() + 0.7D + (random.nextDouble() - 0.5D) * 0.2D;
		double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D;
		level.addParticle(ModParticleEffects.METEOR_DUST, x, y, z, 0.0D, 0.0D, 0.0D);
		if (random.nextInt(4) == 0) {
			level.addParticle(ParticleTypes.SMOKE, x, y + 0.1D, z, 0.0D, 0.01D, 0.0D);
		}
	}

	@Override
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
		if (!level.isClientSide()) {
			level.scheduleTick(pos, this, 20);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		boolean protectedNow = ShieldRegistry.isProtected(level, pos.getX() + 0.5, pos.getZ() + 0.5);
		if (state.getValue(LIT) != protectedNow) {
			level.setBlock(pos, state.setValue(LIT, protectedNow), 3);
		}
		level.scheduleTick(pos, this, 20);
	}
}
