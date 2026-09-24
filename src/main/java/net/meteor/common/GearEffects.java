package net.meteor.common;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.meteor.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Efectos de equipo de Falling Meteors.
 *
 *  - Magnetización: atrae objetos. Innata en la armadura de Meteorito (hierro
 *    meteórico) y también activa según el NIVEL del encantamiento data-driven
 *    en cualquier pieza/herramienta equipada.
 *  - Tacto Frío (encantamiento de Frezarito):
 *      · arma -> aplica Lentitud al golpear;
 *      · set completo de Frezarito (o botas con el encantamiento) -> congela el
 *        agua a tu paso, como Paso Helado (Frost Walker).
 *  - Inmunidad al fuego con el set completo de Kreknorito.
 *  - La espada de Kreknorito incendia al enemigo al golpear.
 */
public final class GearEffects {

	private GearEffects() {}

	private static final ResourceKey<Enchantment> MAGNETIZATION_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, MeteorsMod.id("magnetization"));
	private static final ResourceKey<Enchantment> COLD_TOUCH_KEY =
			ResourceKey.create(Registries.ENCHANTMENT, MeteorsMod.id("cold_touch"));

	private static final EquipmentSlot[] ARMOR_SLOTS =
			{ EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

	public static void register() {
		ServerTickEvents.END_WORLD_TICK.register(GearEffects::onWorldTick);
		AttackEntityCallback.EVENT.register(GearEffects::onAttack);
		EnchantmentEvents.ALLOW_ENCHANTING.register(GearEffects::allowEnchanting);
	}

	// === Que objetos ya traen cada efecto de serie ===
	// Una sola fuente de verdad: estos metodos deciden tanto el efecto como que
	// el encantamiento no se pueda poner encima. Si alguno cambia, cambian los dos.

	private static boolean isFrezariteTool(ItemStack s) {
		return s.is(ModItems.FREZARITE_SWORD) || s.is(ModItems.FREZARITE_PICKAXE) || s.is(ModItems.FREZARITE_AXE)
				|| s.is(ModItems.FREZARITE_SHOVEL) || s.is(ModItems.FREZARITE_HOE);
	}

	private static boolean isMeteoriteTool(ItemStack s) {
		return s.is(ModItems.METEORITE_SWORD) || s.is(ModItems.METEORITE_PICKAXE) || s.is(ModItems.METEORITE_AXE)
				|| s.is(ModItems.METEORITE_SHOVEL) || s.is(ModItems.METEORITE_HOE);
	}

	private static boolean isMeteoriteArmor(ItemStack s) {
		return s.is(ModItems.METEORITE_HELMET) || s.is(ModItems.METEORITE_CHESTPLATE)
				|| s.is(ModItems.METEORITE_LEGGINGS) || s.is(ModItems.METEORITE_BOOTS);
	}

	/** Frio de serie: las armas y herramientas de Frezarito, y sus botas (Paso Helado). */
	private static boolean hasInnateColdTouch(ItemStack s) {
		return isFrezariteTool(s) || s.is(ModItems.FREZARITE_BOOTS);
	}

	/** Magnetizacion de serie: toda la armadura y las herramientas de Meteorito. */
	private static boolean hasInnateMagnetization(ItemStack s) {
		return isMeteoriteArmor(s) || isMeteoriteTool(s);
	}

	/**
	 * Impide encantar con Tacto Frio o Magnetizacion lo que ya los trae de serie:
	 * no suman nada y gastarian el encantamiento.
	 *
	 * Tiene que ser codigo y no una etiqueta porque una etiqueta suma objetos pero
	 * no puede restarlos, y estos objetos estan en las etiquetas de armas y
	 * armaduras para poder llevar los encantamientos vanilla. Aplica igual en la
	 * mesa de encantar que en el yunque y en /enchant.
	 */
	private static TriState allowEnchanting(Holder<Enchantment> enchantment, ItemStack target,
											EnchantingContext context) {
		if (enchantment.is(COLD_TOUCH_KEY) && hasInnateColdTouch(target)) return TriState.FALSE;
		if (enchantment.is(MAGNETIZATION_KEY) && hasInnateMagnetization(target)) return TriState.FALSE;
		return TriState.DEFAULT;
	}

	private static void onWorldTick(ServerLevel world) {
		for (ServerPlayer player : world.players()) {
			applyMagnetization(world, player);
			applyColdTouchFrostWalker(world, player);
			applyFrezariteHelmet(player);
			applyKreknoriteFireImmunity(player);
		}
	}

	// === Casco de Frezarito: respiración acuática prolongada ===
	private static void applyFrezariteHelmet(ServerPlayer player) {
		if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.FREZARITE_HELMET) && player.isUnderWater()) {
			player.setAirSupply(player.getMaxAirSupply());
		}
	}

	// === Magnetización ===
	private static void applyMagnetization(ServerLevel world, ServerPlayer player) {
		ItemStack main = player.getItemBySlot(EquipmentSlot.MAINHAND);
		int level = (anyMeteoriteArmor(player) || isMeteoriteTool(main)) ? 1 : 0;
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			level = Math.max(level, enchantLevel(world, player.getItemBySlot(slot), MAGNETIZATION_KEY));
		}
		level = Math.max(level, enchantLevel(world, main, MAGNETIZATION_KEY));

		// Interruptor maestro: si llevas el controlador y está APAGADO, no hay magnetización.
		ItemStack controller = findController(player);
		if (!controller.isEmpty() && !net.meteor.common.item.MagnetizationControllerItem.isEnabled(controller)) {
			return;
		}
		if (level <= 0) return;

		double radius = 8.0 * level;
		List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius));
		Vec3 target = player.position().add(0, player.getEyeHeight(), 0);
		for (ItemEntity item : items) {
			Vec3 dir = target.subtract(item.position());
			double dist = dir.length();
			if (dist < 0.6 || dist > radius) continue;
			double f = 1.0 - dist / radius;
			Vec3 pull = dir.normalize().scale(0.12 * f * f + 0.02);
			item.setDeltaMovement(item.getDeltaMovement().add(pull));
		}
	}

	/** Busca el controlador en el inventario o equipado en el cinturón (si hay Trinkets). EMPTY si no lo lleva. */
	private static ItemStack findController(ServerPlayer player) {
		var inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack st = inv.getItem(i);
			if (st.is(ModItems.MAGNETIZATION_CONTROLLER)) return st;
		}
		if (FabricLoader.getInstance().isModLoaded("trinkets")) {
			ItemStack worn = net.meteor.common.compat.MeteorsTrinketsCompat.getWornController(player);
			if (!worn.isEmpty()) return worn;
		}
		return ItemStack.EMPTY;
	}

	// === Tacto Frío: Frost Walker con set completo de Frezarito (o botas encantadas) ===
	private static void applyColdTouchFrostWalker(ServerLevel world, ServerPlayer player) {
		boolean fullFrezarite = countArmor(player, ModItems.FREZARITE_HELMET, ModItems.FREZARITE_CHESTPLATE,
				ModItems.FREZARITE_LEGGINGS, ModItems.FREZARITE_BOOTS) == 4;
		boolean frezariteBoots = player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.FREZARITE_BOOTS);
		boolean coldBoots = enchantLevel(world, player.getItemBySlot(EquipmentSlot.FEET), COLD_TOUCH_KEY) > 0;
		if (!(fullFrezarite || frezariteBoots || coldBoots) || player.isInWater() || !player.onGround()) return;

		int r = 2;
		int y = (int) Math.floor(player.getY()) - 1;
		int px = (int) Math.floor(player.getX());
		int pz = (int) Math.floor(player.getZ());
		for (int x = px - r; x <= px + r; x++) {
			for (int z = pz - r; z <= pz + r; z++) {
				BlockPos pos = new BlockPos(x, y, z);
				BlockState state = world.getBlockState(pos);
				if (state.is(Blocks.WATER) && world.getBlockState(pos.above()).isAir()) {
					world.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
				}
			}
		}
	}

	// === Congelar, como la nieve en polvo ===
	/**
	 * Frio que añade cada golpe, por nivel. A nivel 1 hacen falta dos golpes para
	 * congelar del todo; a nivel 2 —el de las armas de Frezarito— basta uno.
	 */
	private static final int FREEZE_PER_HIT = 70;
	/** Cuanto se puede pasar del umbral: sin golpear mas, sigue congelado ~1,5 s. */
	private static final int FREEZE_OVERSHOOT = 60;

	/**
	 * Sube el contador de congelacion del objetivo, el mismo que llena la nieve en
	 * polvo. Al llegar al umbral la entidad queda congelada: tiembla, al jugador se
	 * le escarcha la pantalla, y recibe daño de congelacion cada dos segundos. Fuera
	 * de la nieve se descongela sola a dos ticks por tick.
	 *
	 * Respeta las mismas inmunidades que la nieve: canFreeze() es falso para los
	 * mobs inmunes y para quien lleva armadura de cuero.
	 */
	private static void freeze(LivingEntity target, int level) {
		if (!target.canFreeze()) return;
		int limit = target.getTicksRequiredToFreeze() + FREEZE_OVERSHOOT;
		target.setTicksFrozen(Math.min(limit, target.getTicksFrozen() + FREEZE_PER_HIT * level));
	}

	// === Inmunidad al fuego con set completo de Kreknorito ===
	private static void applyKreknoriteFireImmunity(Player player) {
		if (countArmor(player, ModItems.KREKNORITE_HELMET, ModItems.KREKNORITE_CHESTPLATE,
				ModItems.KREKNORITE_LEGGINGS, ModItems.KREKNORITE_BOOTS) == 4) {
			player.setRemainingFireTicks(0);
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false, false));
		}
	}

	// === Golpes: fuego (Kreknorito) y Lentitud (Tacto Frío / espada de Frezarito) ===
	private static InteractionResult onAttack(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
		if (!(entity instanceof LivingEntity living) || !(world instanceof ServerLevel sl)) {
			return InteractionResult.PASS;
		}
		ItemStack weapon = player.getMainHandItem();
		if (weapon.is(ModItems.KREKNORITE_SWORD)) {
			living.setRemainingFireTicks(160); // ~8 segundos
		}
		// Las de Frezarito llevan el frio de serie al nivel maximo del
		// encantamiento, leido de su propia definicion: si cambia max_level en el
		// JSON, cambian con el. Las demas armas, por el nivel que tengan.
		int cold = Math.max(isFrezariteTool(weapon) ? maxLevel(sl, COLD_TOUCH_KEY) : 0,
				enchantLevel(sl, weapon, COLD_TOUCH_KEY));
		if (cold > 0) {
			living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
			freeze(living, cold);
		}
		return InteractionResult.PASS;
	}

	// === Helpers ===
	private static boolean anyMeteoriteArmor(Player player) {
		for (EquipmentSlot slot : ARMOR_SLOTS) {
			if (isMeteoriteArmor(player.getItemBySlot(slot))) return true;
		}
		return false;
	}

	private static int countArmor(Player player, Item helmet, Item chest, Item legs, Item boots) {
		int n = 0;
		if (player.getItemBySlot(EquipmentSlot.HEAD).is(helmet)) n++;
		if (player.getItemBySlot(EquipmentSlot.CHEST).is(chest)) n++;
		if (player.getItemBySlot(EquipmentSlot.LEGS).is(legs)) n++;
		if (player.getItemBySlot(EquipmentSlot.FEET).is(boots)) n++;
		return n;
	}

	/** Lee el nivel de un encantamiento (por su clave) en un stack, sin EnchantmentHelper. */
	private static int maxLevel(ServerLevel world, ResourceKey<Enchantment> key) {
		return world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
				.map(holder -> holder.value().getMaxLevel()).orElse(1);
	}

	private static int enchantLevel(ServerLevel world, ItemStack stack, ResourceKey<Enchantment> key) {
		if (stack.isEmpty()) return 0;
		Optional<Holder.Reference<Enchantment>> holder =
				world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
		if (holder.isEmpty()) return 0;
		return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(holder.get());
	}
}
