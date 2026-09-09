package net.meteor.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.meteor.common.entity.MeteorEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockAndTintGetter;

/**
 * Renderiza el meteoro como el bloque de su material girando en el aire.
 */
public class MeteorEntityRenderer extends EntityRenderer<MeteorEntity, MeteorRenderState> {

	public MeteorEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public MeteorRenderState createRenderState() {
		return new MeteorRenderState();
	}

	@Override
	public void extractRenderState(MeteorEntity entity, MeteorRenderState state, float partialTick) {
		super.extractRenderState(entity, state, partialTick);
		state.block = entity.getMeteorType().getCoreBlock().defaultBlockState();
		state.spin = (entity.tickCount + partialTick) * 6.0F;
	}

	@Override
	public void submit(MeteorRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.5F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
		poseStack.mulPose(Axis.XP.rotationDegrees(state.spin * 0.7F));
		float s = 1.5F;
		poseStack.scale(s, s, s);
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		collector.order(0).submitBlock(poseStack, state.block, state.lightCoords, OverlayTexture.NO_OVERLAY, 0,
				EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO);
		poseStack.popPose();
		super.submit(state, poseStack, collector, cameraState);
	}
}
