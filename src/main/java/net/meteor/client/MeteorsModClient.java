package net.meteor.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.meteor.client.model.MeteorShieldModelData;
import net.meteor.client.model.MeteorTimerModelData;
import net.meteor.client.render.MeteorShieldRenderer;
import net.meteor.client.render.MeteorTimerRenderer;
import net.meteor.common.registry.ModBlockEntities;
import net.minecraft.client.gui.screens.MenuScreens;
import net.meteor.client.gui.FreezingMachineScreen;
import net.meteor.client.gui.MeteorShieldScreen;
import net.meteor.common.registry.ModMenuTypes;
import net.meteor.client.model.AlienCreeperModel;
import net.meteor.client.model.CometKittyModel;
import net.meteor.client.render.AlienCreeperRenderer;
import net.meteor.client.render.CometKittyRenderer;
import net.meteor.client.render.MeteorEntityRenderer;
import net.meteor.common.MeteorsMod;
import net.meteor.common.registry.ModEntities;

/**
 * Punto de entrada del lado cliente.
 *
 * Registra las capas de modelo custom y los renderers de entidades.
 */
public class MeteorsModClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		MeteorsMod.LOGGER.debug("Falling Meteors: init cliente");

		EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.ALIEN_CREEPER, AlienCreeperModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.COMET_KITTY, CometKittyModel::createBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.COMET_KITTY_BABY, CometKittyModel::createBabyBodyLayer);
		EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.METEOR_TIMER_MODEL, MeteorTimerModelData::createLayer);
		EntityModelLayerRegistry.registerModelLayer(ModEntityModelLayers.METEOR_SHIELD_MODEL, MeteorShieldModelData::createLayer);

		EntityRendererRegistry.register(ModEntities.ALIEN_CREEPER, AlienCreeperRenderer::new);
		EntityRendererRegistry.register(ModEntities.COMET_KITTY, CometKittyRenderer::new);
		EntityRendererRegistry.register(ModEntities.METEOR, MeteorEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.SUMMONER_PROJECTILE,
				ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx));

		MenuScreens.register(ModMenuTypes.METEOR_SHIELD, MeteorShieldScreen::new);
		MenuScreens.register(ModMenuTypes.FREEZING_MACHINE, FreezingMachineScreen::new);

		BlockEntityRendererRegistry.register(ModBlockEntities.METEOR_TIMER, MeteorTimerRenderer::new);
		BlockEntityRendererRegistry.register(ModBlockEntities.METEOR_SHIELD, MeteorShieldRenderer::new);
	}
}
