package net.meteor.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.meteor.client.ModEntityModelLayers;
import net.meteor.client.model.CometKittyModel;
import net.meteor.common.MeteorsMod;
import net.meteor.common.entity.CometKitty;
import net.minecraft.client.model.animal.feline.CatModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CatCollarLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * Renderer del Comet Kitty. Replica {@link net.minecraft.client.renderer.entity.CatRenderer}
 * pero usando {@link CometKittyModel} (con casco) como modelo adulto y bebé.
 *
 * Por qué no extender CatRenderer: {@code AgeableMobRenderer} guarda adultModel/babyModel
 * como private final y reasigna this.model en cada frame, así que un swap de modelo no
 * sobrevive. Aquí pasamos nuestro modelo por el constructor, que es la vía correcta.
 */
public class CometKittyRenderer extends AgeableMobRenderer<CometKitty, CatRenderState, CatModel> {

	private static final Identifier TEXTURE = MeteorsMod.id("textures/entity/comet_kitty.png");

	public CometKittyRenderer(EntityRendererProvider.Context context) {
		super(context,
				new CometKittyModel(context.bakeLayer(ModEntityModelLayers.COMET_KITTY)),
				new CometKittyModel(context.bakeLayer(ModEntityModelLayers.COMET_KITTY_BABY)),
				0.4F);
		this.addLayer(new CatCollarLayer(this, context.getModelSet()));
	}

	@Override
	public Identifier getTextureLocation(CatRenderState state) {
		return TEXTURE;
	}

	@Override
	public CatRenderState createRenderState() {
		return new CatRenderState();
	}

	@Override
	public void extractRenderState(CometKitty cat, CatRenderState state, float f) {
		super.extractRenderState(cat, state, f);
		state.isCrouching = cat.isCrouching();
		state.isSprinting = cat.isSprinting();
		state.isSitting = cat.isInSittingPose();
		state.lieDownAmount = cat.getLieDownAmount(f);
		state.lieDownAmountTail = cat.getLieDownAmountTail(f);
		state.relaxStateOneAmount = cat.getRelaxStateOneAmount(f);
		state.isLyingOnTopOfSleepingPlayer = cat.isLyingOnTopOfSleepingPlayer();
		state.collarColor = cat.isTame() ? cat.getCollarColor() : null;
	}

	@Override
	protected void setupRotations(CatRenderState state, PoseStack poseStack, float f, float g) {
		super.setupRotations(state, poseStack, f, g);
		float h = state.lieDownAmount;
		if (h > 0.0F) {
			poseStack.translate(0.4F * h, 0.15F * h, 0.1F * h);
			poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(h, 0.0F, 90.0F)));
			if (state.isLyingOnTopOfSleepingPlayer) {
				poseStack.translate(0.15F * h, 0.0F, 0.0F);
			}
		}
	}
}
