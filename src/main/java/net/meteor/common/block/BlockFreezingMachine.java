package net.meteor.common.block;

import net.meteor.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Bloque de la máquina congeladora (orientable, con interfaz y tick). */
public class BlockFreezingMachine extends Block implements EntityBlock {

	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
	/** Encendida: hay frío y trabajo en curso. Solo se usa para las partículas. */
	public static final net.minecraft.world.level.block.state.properties.BooleanProperty LIT =
			BlockStateProperties.LIT;

	public BlockFreezingMachine(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(LIT, Boolean.FALSE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState()
				.setValue(FACING, ctx.getHorizontalDirection().getOpposite())
				.setValue(LIT, Boolean.FALSE);
	}

	/** Aura fría en las cuatro esquinas mientras la máquina tiene trabajo. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(LIT)) return;

		float cx = pos.getX() + 0.5F;
		float cy = pos.getY() + 0.4F + random.nextFloat() * 0.4F;
		float cz = pos.getZ() + 0.5F;
		float a = 0.52F;
		float b = random.nextFloat() * 0.6F - 0.3F;

		level.addParticle(ModParticleEffects.FREZA_DUST, cx - a, cy, cz + b, 0.0D, 0.0D, 0.0D);
		level.addParticle(ModParticleEffects.FREZA_DUST, cx + a, cy, cz + b, 0.0D, 0.0D, 0.0D);
		level.addParticle(ModParticleEffects.FREZA_DUST, cx + b, cy, cz - a, 0.0D, 0.0D, 0.0D);
		level.addParticle(ModParticleEffects.FREZA_DUST, cx + b, cy, cz + a, 0.0D, 0.0D, 0.0D);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new FreezingMachineBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && player instanceof ServerPlayer) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof FreezingMachineBlockEntity machine) {
				player.openMenu(machine);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return type == ModBlockEntities.FREEZING_MACHINE
				? (lvl, pos, st, be) -> FreezingMachineBlockEntity.serverTick(lvl, pos, st, (FreezingMachineBlockEntity) be)
				: null;
	}
}
