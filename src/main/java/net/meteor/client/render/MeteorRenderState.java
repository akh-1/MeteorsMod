package net.meteor.client.render;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Estado de render del meteoro: el bloque a dibujar y el ángulo de giro. */
public class MeteorRenderState extends EntityRenderState {
	public BlockState block = Blocks.STONE.defaultBlockState();
	public float spin;
}
