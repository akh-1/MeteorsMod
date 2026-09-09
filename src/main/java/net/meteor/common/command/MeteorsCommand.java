package net.meteor.common.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.meteor.common.MeteorType;
import net.meteor.common.MeteorsConfig;
import net.meteor.common.ShieldRegistry;
import net.meteor.common.climate.GhostMeteor;
import net.meteor.common.climate.MeteorForecast;
import net.meteor.common.climate.ShieldSavedData;
import net.meteor.common.entity.MeteorEntity;
import net.meteor.common.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec2;

import java.util.Arrays;
import java.util.Locale;

/**
 * Comandos del mod. Sustituye a los cinco del original
 * (/spawnmeteor, /spawncomet, /kittyattack, /debugshields, /debugmeteors)
 * por un único árbol {@code /meteors}.
 */
public final class MeteorsCommand {

	private MeteorsCommand() {}

	private static final SuggestionProvider<CommandSourceStack> TYPES = (ctx, builder) ->
			SharedSuggestionProvider.suggest(
					Arrays.stream(MeteorType.values()).map(t -> t.name().toLowerCase(Locale.ROOT)).toList(),
					builder);

	/**
	 * Quién puede usar /meteors.
	 *
	 * TEMPORAL: en 1.21.11 no resuelven ni {@code CommandSourceStack#hasPermission(int)}
	 * ni {@code PlayerList#isOp(GameProfile)} — este último ahora pide un
	 * {@code NameAndId}. Hasta confirmar cómo obtenerlo, la consola y los bloques
	 * de comandos pasan siempre, y los jugadores solo en un mundo de un jugador.
	 * En servidor dedicado los comandos quedan restringidos a la consola.
	 */
	private static boolean isOperator(CommandSourceStack src) {
		ServerPlayer player = src.getPlayer();
		if (player == null) return true;
		return src.getServer().isSingleplayer();
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, env) ->
				dispatcher.register(build()));
	}

	private static LiteralArgumentBuilder<CommandSourceStack> build() {
		return Commands.literal("meteors")
				.requires(MeteorsCommand::isOperator)

				.then(Commands.literal("spawn")
						.then(Commands.argument("type", StringArgumentType.word()).suggests(TYPES)
								.executes(ctx -> spawn(ctx, 1, null, false))
								.then(Commands.argument("size", IntegerArgumentType.integer(1, 3))
										.executes(ctx -> spawn(ctx, IntegerArgumentType.getInteger(ctx, "size"), null, false))
										.then(Commands.argument("pos", Vec2Argument.vec2())
												.executes(ctx -> spawn(ctx,
														IntegerArgumentType.getInteger(ctx, "size"),
														Vec2Argument.getVec2(ctx, "pos"), false))))))

				.then(Commands.literal("comet")
						.then(Commands.argument("type", StringArgumentType.word()).suggests(TYPES)
								.executes(ctx -> spawn(ctx, 1, null, true))))

				.then(Commands.literal("schedule")
						.then(Commands.argument("type", StringArgumentType.word()).suggests(TYPES)
								.then(Commands.argument("seconds", IntegerArgumentType.integer(1))
										.executes(MeteorsCommand::schedule))))

				.then(Commands.literal("kittyattack").executes(MeteorsCommand::kittyAttack))

				.then(Commands.literal("forecast").executes(MeteorsCommand::forecast))

				.then(Commands.literal("shields").executes(MeteorsCommand::shields))

				.then(Commands.literal("clear").executes(ctx -> {
					MeteorForecast.get(ctx.getSource().getLevel()).clearPending();
					ctx.getSource().sendSuccess(() ->
							Component.literal("Pronóstico vaciado.").withStyle(ChatFormatting.GRAY), true);
					return 1;
				}));
	}

	// ------------------------------------------------------------------

	private static MeteorType parseType(String raw) {
		for (MeteorType t : MeteorType.values()) {
			if (t.name().equalsIgnoreCase(raw)) return t;
		}
		return MeteorType.METEORITE;
	}

	private static int spawn(CommandContext<CommandSourceStack> ctx, int size, Vec2 pos, boolean comet) {
		CommandSourceStack src = ctx.getSource();
		ServerLevel level = src.getLevel();
		MeteorType type = parseType(StringArgumentType.getString(ctx, "type"));

		double x = pos != null ? pos.x : src.getPosition().x;
		double z = pos != null ? pos.y : src.getPosition().z;

		MeteorEntity meteor = ModEntities.METEOR.create(level, EntitySpawnReason.COMMAND);
		if (meteor == null) return 0;
		meteor.setComet(comet);
		meteor.setMeteorType(type);
		meteor.setMeteorSize(comet ? 1 : size);
		meteor.snapTo(x, Math.min(300.0D, level.getMaxY() - 10), z, 0.0F, 0.0F);
		meteor.setDeltaMovement(0.0D, -0.04D, 0.0D);
		level.addFreshEntity(meteor);

		src.sendSuccess(() -> Component.literal(
				(comet ? "Cometa " : "Meteoro ") + type + " lanzado en " + (int) x + ", " + (int) z)
				.withStyle(ChatFormatting.LIGHT_PURPLE), true);
		return 1;
	}

	private static int schedule(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		ServerLevel level = src.getLevel();
		MeteorType type = parseType(StringArgumentType.getString(ctx, "type"));
		int seconds = IntegerArgumentType.getInteger(ctx, "seconds");

		int x = (int) src.getPosition().x;
		int z = (int) src.getPosition().z;
		int size = MeteorsConfig.INSTANCE.maxMeteorSize;

		MeteorForecast.get(level).addGhost(new GhostMeteor(x, z, size, type, seconds));
		src.sendSuccess(() -> Component.literal(
				"Programado " + type + " en " + x + ", " + z + " dentro de " + seconds + " s")
				.withStyle(ChatFormatting.GOLD), true);
		return 1;
	}

	private static int kittyAttack(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		ServerLevel level = src.getLevel();
		int x = (int) src.getPosition().x;
		int z = (int) src.getPosition().z;

		MeteorForecast.get(level).addGhost(new GhostMeteor(x, z, 3, MeteorType.KITTY, 90));
		src.sendSuccess(() -> Component.translatable("event.meteors.kittyAttack")
				.withStyle(ChatFormatting.GREEN), true);
		return 1;
	}

	private static int forecast(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		MeteorForecast forecast = MeteorForecast.get(src.getLevel());

		int next = forecast.getSecondsUntilNewMeteor();
		src.sendSuccess(() -> Component.literal(
				"Siguiente programación en " + (next < 0 ? "?" : next + " s") +
				" — pendientes: " + forecast.getPending().size()).withStyle(ChatFormatting.AQUA), false);

		for (GhostMeteor g : forecast.getPending()) {
			src.sendSuccess(() -> Component.literal("  · " + g).withStyle(ChatFormatting.GRAY), false);
		}

		BlockPos last = forecast.getLastCrash();
		if (last != null) {
			src.sendSuccess(() -> Component.literal(
					"  último impacto: " + forecast.getLastCrashType() + " en " +
					last.getX() + ", " + last.getZ()).withStyle(ChatFormatting.DARK_GRAY), false);
		}
		return 1;
	}

	private static int shields(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack src = ctx.getSource();
		var all = ShieldRegistry.all(src.getLevel());
		src.sendSuccess(() -> Component.literal("Escudos en esta dimensión: " + all.size())
				.withStyle(ChatFormatting.AQUA), false);
		for (ShieldSavedData.Entry e : all) {
			src.sendSuccess(() -> Component.literal(
					"  · " + e.pos().getX() + ", " + e.pos().getY() + ", " + e.pos().getZ() +
					" — rango " + e.range() + " — " + e.owner() +
					(e.blockComets() ? " — bloquea cometas" : "")).withStyle(ChatFormatting.GRAY), false);
		}
		return 1;
	}
}
