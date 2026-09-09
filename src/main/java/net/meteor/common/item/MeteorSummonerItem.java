package net.meteor.common.item;

import net.meteor.common.MeteorType;
import net.meteor.common.entity.SummonerProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Invocador de meteoritos: arrojadizo como una perla de ender. Lanza un
 * {@link SummonerProjectile} y donde cae aparece un meteoro.
 *
 * @param type tipo de meteoro a invocar; null = aleatorio.
 */
public class MeteorSummonerItem extends Item {

	@Nullable
	private final MeteorType type;

	public MeteorSummonerItem(Properties properties, @Nullable MeteorType type) {
		super(properties);
		this.type = type;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				SoundEvents.ENDER_PEARL_THROW, SoundSource.PLAYERS,
				0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

		if (level instanceof ServerLevel serverLevel) {
			SummonerProjectile projectile = new SummonerProjectile(level, player, stack);
			projectile.summonerType = this.type;
			projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
			serverLevel.addFreshEntity(projectile);
		}

		stack.consume(1, player);
		return InteractionResult.SUCCESS;
	}
}
