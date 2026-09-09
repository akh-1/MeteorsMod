package net.meteor.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

/**
 * Controlador de Magnetización: interruptor maestro de la magnetización del jugador.
 * Llevándolo (equipado en el cinturón o en el inventario):
 *   - ACTIVADO  -> la magnetización (armadura/herramientas/encantamientos) funciona.
 *   - DESACTIVADO -> corta TODA la magnetización.
 * No genera magnetización por sí mismo. Clic derecho para alternar.
 */
public class MagnetizationControllerItem extends Item {

	public MagnetizationControllerItem(Properties properties) {
		super(properties);
	}

	/** true = magnetización permitida; false = cortada. Por defecto activado. */
	public static boolean isEnabled(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null || !data.copyTag().getBooleanOr("disabled", false);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		boolean nowDisabled = !tag.getBooleanOr("disabled", false);
		tag.putBoolean("disabled", nowDisabled);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		if (!level.isClientSide()) {
			Component state = nowDisabled
					? Component.translatable("options.off").withStyle(ChatFormatting.RED)
					: Component.translatable("options.on").withStyle(ChatFormatting.GREEN);
			player.displayClientMessage(Component.translatable("info.meteors.magnetization_controller.state", state), true);
		}
		return InteractionResult.SUCCESS;
	}
}
