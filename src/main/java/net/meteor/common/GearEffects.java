package net.meteor.common;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.meteor.common.registry.ModItems;
import net.minecraft.core.BlockPos;
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
		boolean meteoriteTool = main.is(ModItems.METEORITE_SWORD) || main.is(ModItems.METEORITE_PICKAXE)
				|| main.is(ModItems.METEORITE_AXE) || main.is(ModItems.METEORITE_SHOVEL) || main.is(ModItems.METEORITE_HOE);
		int level = (anyMeteoriteArmor(player) || meteoriteTool) ? 1 : 0;
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
		boolean coldWeapon = weapon.is(ModItems.FREZARITE_SWORD) || weapon.is(ModItems.FREZARITE_PICKAXE)
				|| weapon.is(ModItems.FREZARITE_AXE) || weapon.is(ModItems.FREZARITE_SHOVEL) || weapon.is(ModItems.FREZARITE_HOE)
				|| enchantLevel(sl, weapon, COLD_TOUCH_KEY) > 0;
		if (coldWeapon) {
			living.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 0));
		}
		return InteractionResult.PASS;
	}

	// === Helpers ===
	private static boolean anyMeteoriteArmor(Player player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.METEORITE_HELMET)
				|| player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.METEORITE_CHESTPLATE)
				|| player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.METEORITE_LEGGINGS)
				|| player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.METEORITE_BOOTS);
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
	private static int enchantLevel(ServerLevel world, ItemStack stack, ResourceKey<Enchantment> key) {
		if (stack.isEmpty()) return 0;
		Optional<Holder.Reference<Enchantment>> holder =
				world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
		if (holder.isEmpty()) return 0;
		return stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).getLevel(holder.get());
	}
}
