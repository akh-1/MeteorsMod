package net.meteor.common.block;

import net.meteor.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/** Bloque del escudo anti-meteoro: abre su interfaz al usarlo y tiene tick de servidor. */
public class MeteorShieldBlock extends Block implements EntityBlock {

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	public MeteorShieldBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide() && placer != null && level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield) {
			shield.setOwner(placer.getName().getString());
		}
	}

	/**
	 * Glifos que caen hacia el escudo desde un anillo de 5x5, igual que el
	 * EntityMeteorShieldParticleFX original (que heredaba de la partícula de la
	 * mesa de encantamientos). Solo se ven si el escudo está cargado.
	 */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!(level.getBlockEntity(pos) instanceof MeteorShieldBlockEntity shield) || !shield.isCharged()) return;
		if (!level.getBlockState(pos.above()).isAir()) return;

		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		for (int cx = x - 2; cx <= x + 2; cx++) {
			for (int cz = z - 2; cz <= z + 2; cz++) {
				if (cx > x - 2 && cx < x + 2 && cz == z - 1) cz = z + 2;
				if (random.nextInt(100) != 25) continue;

				for (int cy = y; cy <= y + 1; cy++) {
					if (!level.getBlockState(new BlockPos((cx - x) / 2 + x, cy, (cz - z) / 2 + z)).isAir()) break;
					level.addParticle(ParticleTypes.ENCHANT,
							x + 0.5D, y + 2.0D, z + 0.5D,
							cx - x + random.nextFloat() - 0.5D,
							cy - y - random.nextFloat() - 1.0F,
							cz - z + random.nextFloat() - 0.5D);
				}
			}
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MeteorShieldBlockEntity(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!level.isClientSide() && player instanceof ServerPlayer) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof MeteorShieldBlockEntity shield) {
				player.openMenu(shield);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) return null;
		return type == ModBlockEntities.METEOR_SHIELD
				? (lvl, pos, st, be) -> MeteorShieldBlockEntity.serverTick(lvl, pos, st, (MeteorShieldBlockEntity) be)
				: null;
	}
}
