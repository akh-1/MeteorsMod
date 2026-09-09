package net.meteor.client.render;

import net.meteor.client.ModEntityModelLayers;
import net.meteor.client.model.AlienCreeperModel;
import net.meteor.common.MeteorsMod;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CreeperRenderState;
import net.minecraft.resources.Identifier;

/** Reusa el render del Creeper (carga eléctrica incluida) pero con modelo (antena) y textura propios. */
public class AlienCreeperRenderer extends CreeperRenderer {

	private static final Identifier TEXTURE = MeteorsMod.id("textures/entity/alien_creeper.png");

	public AlienCreeperRenderer(EntityRendererProvider.Context context) {
		super(context);
		// Sustituye el modelo del cuerpo por el que incluye la antena.
		this.model = new AlienCreeperModel(context.bakeLayer(ModEntityModelLayers.ALIEN_CREEPER));
	}

	@Override
	public Identifier getTextureLocation(CreeperRenderState state) {
		return TEXTURE;
	}
}
