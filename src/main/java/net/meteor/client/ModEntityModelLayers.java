package net.meteor.client;

import net.meteor.common.MeteorsMod;
import net.minecraft.client.model.geom.ModelLayerLocation;

/** Localizaciones de las capas de modelo custom del mod. */
public final class ModEntityModelLayers {

	private ModEntityModelLayers() {}

	public static final ModelLayerLocation ALIEN_CREEPER =
			new ModelLayerLocation(MeteorsMod.id("alien_creeper"), "main");

	public static final ModelLayerLocation COMET_KITTY =
			new ModelLayerLocation(MeteorsMod.id("comet_kitty"), "main");

	public static final ModelLayerLocation COMET_KITTY_BABY =
			new ModelLayerLocation(MeteorsMod.id("comet_kitty"), "baby");

	public static final ModelLayerLocation METEOR_TIMER_MODEL =
			new ModelLayerLocation(MeteorsMod.id("meteor_timer"), "main");

	public static final ModelLayerLocation METEOR_SHIELD_MODEL =
			new ModelLayerLocation(MeteorsMod.id("meteor_shield"), "main");
}
