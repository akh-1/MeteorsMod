package net.meteor.client.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/** Estado de render del escudo: nivel de potencia, si está cargado y el tiempo de animación. */
public class MeteorShieldRenderState extends BlockEntityRenderState {
	public int powerLevel;
	public boolean charged;
	public float time;
}
