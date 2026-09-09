package net.meteor.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.meteor.client.ModEntityModelLayers;
import net.meteor.common.MeteorsMod;
import net.meteor.common.block.MeteorShieldBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Render fiel del Meteor Shield: modelo horneado del ModelMeteorShield original.
 * Transform como el TESR: trasladar a (0.5,1.5,0.5) y rotar 180° en Z.
 * Las 3 capas giran y flotan (bob) según el nivel de potencia.
 */
public class MeteorShieldRenderer implements BlockEntityRenderer<MeteorShieldBlockEntity, MeteorShieldRenderState> {

	private static final int MAX_LEVEL = MeteorShieldBlockEntity.MAX_LEVEL;

	private static final Material MATERIAL =
			new Material(TextureAtlas.LOCATION_BLOCKS, MeteorsMod.id("entity/meteor_shield"));

	/**
	 * Un modelo horneado por nivel de potencia (0-5) en vez de uno compartido.
	 *
	 * El renderer es una sola instancia para TODOS los escudos, y submit() no
	 * dibuja: encola una referencia al ModelPart que se dibuja más tarde. Si se
	 * mutara un modelo único, el último escudo en pasar por submit() impondría
	 * su visibilidad y su rotación a todos los demás, y bastaba colocar un
	 * segundo escudo sin gemas para apagarle las capas al primero.
	 *
	 * La visibilidad depende solo del nivel, y la rotación solo del nivel y del
	 * tiempo, así que dos escudos del mismo nivel se ven igual y pueden
	 * compartir modelo sin pisarse.
	 */
	private final ModelPart[] roots = new ModelPart[MAX_LEVEL + 1];
	private final ModelPart[][] layers = new ModelPart[MAX_LEVEL + 1][];
	private final RenderType renderType;
	private final TextureAtlasSprite sprite;

	public MeteorShieldRenderer(BlockEntityRendererProvider.Context context) {
		for (int lvl = 0; lvl <= MAX_LEVEL; lvl++) {
			ModelPart baked = context.bakeLayer(ModEntityModelLayers.METEOR_SHIELD_MODEL);
			ModelPart top = baked.getChild("top_layer");
			ModelPart middle = baked.getChild("middle_layer");
			ModelPart bottom = baked.getChild("bottom_layer");
			// Mismos umbrales que el switch del ModelMeteorShield original.
			top.visible = lvl >= 2;
			middle.visible = lvl >= 3;
			bottom.visible = lvl >= 4;
			this.roots[lvl] = baked;
			this.layers[lvl] = new ModelPart[] { top, middle, bottom };
		}
		this.renderType = MATERIAL.renderType(RenderTypes::entityCutoutNoCull);
		this.sprite = context.materials().get(MATERIAL);
	}

	@Override
	public MeteorShieldRenderState createRenderState() {
		return new MeteorShieldRenderState();
	}

	@Override
	public void extractRenderState(MeteorShieldBlockEntity be, MeteorShieldRenderState st, float partial, Vec3 cam, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, st, partial, cam, crumbling);
		st.powerLevel = be.getPowerLevel();
		st.charged = be.isCharged();
		st.time = be.getLevel() != null ? (float) (be.getLevel().getGameTime() % 360000L) + partial : 0.0F;
	}

	@Override
	public void submit(MeteorShieldRenderState st, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cam) {
		int lvl = st.charged ? Mth.clamp(st.powerLevel, 0, MAX_LEVEL) : 0;

		if (lvl > 0) {
			ModelPart[] layer = this.layers[lvl];
			layer[0].yRot = st.time / (140F / lvl);
			layer[1].yRot = st.time / (70F / lvl);
			layer[2].yRot = st.time / (40F / lvl);
			float bob = Mth.sin(st.time / 1600F * 360F) * 0.5F - 0.3F;
			layer[0].y = 15F + bob;
			layer[1].y = 15F + bob;
			layer[2].y = 15F + bob;
		}

		pose.pushPose();
		pose.translate(0.5F, 1.5F, 0.5F);
		pose.mulPose(Axis.ZP.rotationDegrees(180F));
		collector.submitModelPart(this.roots[lvl], pose, this.renderType, st.lightCoords, OverlayTexture.NO_OVERLAY, this.sprite);
		pose.popPose();
	}
}
