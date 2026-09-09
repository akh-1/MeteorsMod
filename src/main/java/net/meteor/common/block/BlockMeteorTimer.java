package net.meteor.common.block;

import net.meteor.common.climate.GhostMeteor;
import net.meteor.common.climate.MeteorForecast;
import net.meteor.common.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/** Bloque Meteor Timer: informa del tiempo y emite redstone justo antes del impacto. */
public class BlockMeteorTimer extends Block implements EntityBlock {

	private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.625, 1.0);

	public BlockMeteorTimer(Properties properties) {
		super(properties);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MeteorTimerBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return type == ModBlockEntities.METEOR_TIMER
				? (lvl, pos, st, be) -> MeteorTimerBlockEntity.serverTick(lvl, pos, st, (MeteorTimerBlockEntity) be)
				: null;
	}

	// --- Redstone ---
	@Override
	protected boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return level.getBlockEntity(pos) instanceof MeteorTimerBlockEntity t ? t.getPower() : 0;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && player instanceof ServerPlayer) {
			boolean quick = false;
			if (level.getBlockEntity(pos) instanceof MeteorTimerBlockEntity t) {
				quick = t.toggleQuickMode();
			}
			GhostMeteor soonest = MeteorForecast.get((net.minecraft.server.level.ServerLevel) level).getSoonestMeteor();
			Component mode = Component.translatable(quick ? "gui.meteors.timer.mode.pulse" : "gui.meteors.timer.mode.analog");
			Component time = soonest == null
					? Component.translatable("gui.meteors.timer.unknown")
					: Component.translatable("gui.meteors.timer.next", soonest.getSecondsLeft());
			player.displayClientMessage(mode.copy().append(" — ").append(time).withStyle(ChatFormatting.AQUA), true);
		}
		return InteractionResult.SUCCESS;
	}
}
