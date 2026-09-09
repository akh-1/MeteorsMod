package net.meteor.common.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.meteor.common.MeteorsMod;
import net.meteor.common.entity.AlienCreeper;
import net.meteor.common.entity.CometKitty;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.Monster;

/**
 * Registro de tipos de entidad y sus atributos por defecto.
 *
 * Fase 4a: los dos mobs vivos (reusan modelo/render vanilla con textura propia).
 * Los proyectiles que caen (Meteor/Comet) y el Summoner llegan en la Fase 4b,
 * junto con el sistema de impacto y spawn en el mundo.
 */
public final class ModEntities {

	private ModEntities() {}

	public static final ResourceKey<EntityType<?>> ALIEN_CREEPER_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MeteorsMod.id("alien_creeper"));
	public static final EntityType<AlienCreeper> ALIEN_CREEPER = Registry.register(
			BuiltInRegistries.ENTITY_TYPE, ALIEN_CREEPER_KEY,
			EntityType.Builder.of(AlienCreeper::new, MobCategory.MONSTER)
					.sized(0.6F, 1.7F)
					.fireImmune()
					.clientTrackingRange(8)
					.build(ALIEN_CREEPER_KEY));

	public static final ResourceKey<EntityType<?>> COMET_KITTY_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MeteorsMod.id("comet_kitty"));
	public static final EntityType<CometKitty> COMET_KITTY = Registry.register(
			BuiltInRegistries.ENTITY_TYPE, COMET_KITTY_KEY,
			EntityType.Builder.of(CometKitty::new, MobCategory.CREATURE)
					.sized(0.6F, 0.7F)
					.fireImmune()
					.clientTrackingRange(8)
					.build(COMET_KITTY_KEY));

	public static final ResourceKey<EntityType<?>> METEOR_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MeteorsMod.id("meteor"));
	public static final EntityType<net.meteor.common.entity.MeteorEntity> METEOR = Registry.register(
			BuiltInRegistries.ENTITY_TYPE, METEOR_KEY,
			EntityType.Builder.of(net.meteor.common.entity.MeteorEntity::new, MobCategory.MISC)
					.sized(0.98F, 0.98F)
					.clientTrackingRange(10)
					.build(METEOR_KEY));

	public static final ResourceKey<EntityType<?>> SUMMONER_PROJECTILE_KEY =
			ResourceKey.create(Registries.ENTITY_TYPE, MeteorsMod.id("summoner_projectile"));
	public static final EntityType<net.meteor.common.entity.SummonerProjectile> SUMMONER_PROJECTILE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE, SUMMONER_PROJECTILE_KEY,
			EntityType.Builder.<net.meteor.common.entity.SummonerProjectile>of(net.meteor.common.entity.SummonerProjectile::new, MobCategory.MISC)
					.sized(0.25F, 0.25F)
					.clientTrackingRange(4)
					.build(SUMMONER_PROJECTILE_KEY));

	public static void register() {
		MeteorsMod.LOGGER.debug("Registrando entidades de Falling Meteors");
	}

	public static void registerAttributes() {
		FabricDefaultAttributeRegistry.register(ALIEN_CREEPER, Monster.createMonsterAttributes());
		FabricDefaultAttributeRegistry.register(COMET_KITTY,
				Cat.createAttributes().add(Attributes.MAX_HEALTH, 16.0));
	}
}
