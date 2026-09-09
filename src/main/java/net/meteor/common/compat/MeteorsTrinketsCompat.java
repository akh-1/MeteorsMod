package net.meteor.common.compat;

import net.minecraft.util.Tuple;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import net.meteor.common.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Integración opcional con Trinkets. Solo se carga si el mod "trinkets" está presente.
 * Permite equipar el Controlador de Magnetización en el cinturón y consultarlo desde GearEffects.
 */
public final class MeteorsTrinketsCompat {

	private MeteorsTrinketsCompat() {}

	public static void init() {
		// Registro (comportamiento delegado a GearEffects); basta con que sea un trinket válido.
		TrinketsApi.registerTrinket(ModItems.MAGNETIZATION_CONTROLLER, new Trinket() {});
	}

	/** Devuelve el controlador equipado como trinket, o ItemStack.EMPTY si no lo lleva. */
	public static ItemStack getWornController(LivingEntity entity) {
		Optional<TrinketComponent> comp = TrinketsApi.getTrinketComponent(entity);
		if (comp.isEmpty()) return ItemStack.EMPTY;
		List<Tuple<SlotReference, ItemStack>> equipped = comp.get().getEquipped(ModItems.MAGNETIZATION_CONTROLLER);
		return equipped.isEmpty() ? ItemStack.EMPTY : equipped.get(0).getB();
	}
}
