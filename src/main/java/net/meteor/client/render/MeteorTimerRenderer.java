package net.meteor.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.meteor.client.ModEntityModelLayers;
import net.meteor.common.MeteorsMod;
import net.meteor.common.block.MeteorTimerBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** Render fiel del Meteor Timer: modelo horneado con sus 3 elementos girando (eje Y, sin flip). */
public class MeteorTimerRenderer implements BlockEntityRenderer<MeteorTimerBlockEntity, MeteorTimerRenderState> {

	private static final Material MATERIAL =
			new Material(TextureAtlas.LOCATION_BLOCKS, MeteorsMod.id("entity/meteor_timer"));

	private final ModelPart root;
	private final ModelPart elementMeteorite;
	private final ModelPart elementFrezarite;
	private final ModelPart elementKreknorite;
	private final RenderType renderType;
	private final TextureAtlasSprite sprite;

	public MeteorTimerRenderer(BlockEntityRendererProvider.Context context) {
		this.root = context.bakeLayer(ModEntityModelLayers.METEOR_TIMER_MODEL);
		this.elementMeteorite = root.getChild("element_meteorite");
		this.elementFrezarite = root.getChild("element_frezarite");
		this.elementKreknorite = root.getChild("element_kreknorite");
		this.renderType = MATERIAL.renderType(RenderTypes::entityCutoutNoCull);
		this.sprite = context.materials().get(MATERIAL);
	}

	@Override
	public MeteorTimerRenderState createRenderState() {
		return new MeteorTimerRenderState();
	}

	@Override
	public void extractRenderState(MeteorTimerBlockEntity be, MeteorTimerRenderState st, float partial, Vec3 cam, ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
		BlockEntityRenderer.super.extractRenderState(be, st, partial, cam, crumbling);
		st.time = be.getLevel() != null ? (float) (be.getLevel().getGameTime() % 360000L) + partial : 0.0F;
	}

	@Override
	public void submit(MeteorTimerRenderState st, PoseStack pose, SubmitNodeCollector collector, CameraRenderState cam) {
		// Giro de los 3 elementos sobre el pivote central (como el original)
		this.elementMeteorite.yRot = st.time * 0.02F;
		this.elementFrezarite.yRot = st.time * 0.01F;
		this.elementKreknorite.yRot = st.time * 0.003F;
		collector.submitModelPart(this.root, pose, this.renderType, st.lightCoords, OverlayTexture.NO_OVERLAY, this.sprite);
	}
}
