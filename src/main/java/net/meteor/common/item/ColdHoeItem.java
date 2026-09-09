package net.meteor.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Azada de Frezarito (Tacto Frío): al usarla sobre tierra/pasto, la convierte
 * directamente en tierra de cultivo ya regada (humedad máxima), sin necesidad
 * de agua cerca.
 */
public class ColdHoeItem extends Item {

	public ColdHoeItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Block b = level.getBlockState(pos).getBlock();
		boolean tillable = b == Blocks.DIRT || b == Blocks.GRASS_BLOCK || b == Blocks.DIRT_PATH
				|| b == Blocks.COARSE_DIRT || b == Blocks.ROOTED_DIRT;

		if (tillable && level.getBlockState(pos.above()).isAir()) {
			level.playSound(ctx.getPlayer(), pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
			if (!level.isClientSide()) {
				BlockState farmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);
				level.setBlock(pos, farmland, 11);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(ctx);
	}
}
