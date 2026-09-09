package net.meteor.common.entity;

import net.meteor.common.MeteorType;
import net.meteor.common.registry.ModEntities;
import net.meteor.common.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

/**
 * Proyectil arrojadizo del invocador (estilo perla de ender). Donde impacta,
 * hace caer un meteoro del tipo correspondiente desde el cielo.
 *
 * Se renderiza como el propio item volando (ThrownItemRenderer), así que se
 * nota claramente que se lanza, a diferencia del invocador directo anterior.
 */
public class SummonerProjectile extends ThrowableItemProjectile {

	/** Tipo a invocar; null = aleatorio. Campo transitorio (el proyectil vive poco). */
	@Nullable
	public MeteorType summonerType = null;

	public SummonerProjectile(EntityType<? extends SummonerProjectile> type, Level level) {
		super(type, level);
	}

	public SummonerProjectile(Level level, LivingEntity owner, ItemStack item) {
		super(ModEntities.SUMMONER_PROJECTILE, owner, level, item);
	}

	@Override
	protected Item getDefaultItem() {
		return ModItems.METEOR_SUMMONER;
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (this.level() instanceof ServerLevel serverLevel) {
			MeteorType type = (this.summonerType != null) ? this.summonerType : randomType(serverLevel);

			// Escudo anti-meteoro: solo los escudos AJENOS bloquean la invocación,
			// igual que en el original (puedes invocar dentro de tu propia zona).
			String summoner = (this.getOwner() != null) ? this.getOwner().getName().getString() : "";
			net.minecraft.core.BlockPos shieldPos = net.meteor.common.MeteorsConfig.INSTANCE.allowSummonedMeteorGrief
					? null : net.meteor.common.ShieldRegistry.getForeignShield(serverLevel, this.getX(), this.getZ(), summoner);
			if (shieldPos != null) {
				if (serverLevel.getBlockEntity(shieldPos) instanceof net.meteor.common.block.MeteorShieldBlockEntity shield) {
					shield.depositMaterial(new net.minecraft.world.item.ItemStack(materialFor(type)));
				}
				this.discard();
				return;
			}

			MeteorEntity meteor = ModEntities.METEOR.create(serverLevel, EntitySpawnReason.TRIGGERED);
			if (meteor != null) {
				meteor.setMeteorType(type);
				meteor.setMeteorSize(net.meteor.common.MeteorsConfig.INSTANCE.minMeteorSize + serverLevel.random.nextInt(net.meteor.common.MeteorsConfig.INSTANCE.maxMeteorSize - net.meteor.common.MeteorsConfig.INSTANCE.minMeteorSize + 1));
				meteor.snapTo(this.getX(), 250.0D, this.getZ(), 0.0F, 0.0F);
				meteor.setDeltaMovement(0.0D, -0.2D, 0.0D);
				serverLevel.addFreshEntity(meteor);
			}
		}
		this.discard();
	}

	private static net.minecraft.world.item.Item materialFor(MeteorType type) {
		return switch (type) {
			case FREZARITE -> ModItems.FREZARITE_CRYSTAL;
			case KREKNORITE -> ModItems.KREKNORITE_CHIP;
			case KITTY -> ModItems.RED_METEOR_GEM;
			default -> ModItems.METEOR_CHIP;
		};
	}

	private static MeteorType randomType(ServerLevel level) {
		return switch (level.random.nextInt(3)) {
			case 1 -> MeteorType.FREZARITE;
			case 2 -> MeteorType.KREKNORITE;
			default -> MeteorType.METEORITE;
		};
	}
}
