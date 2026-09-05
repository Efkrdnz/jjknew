package net.efkrdnz.jjkstrongest.command;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import net.efkrdnz.jjkstrongest.domain.DomainNoclip;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.domain.DomainSource;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;
import net.efkrdnz.jjkstrongest.network.DomainNoclipPacket;
import net.efkrdnz.jjkstrongest.procedures.DomainExpansionStartProcedure;
import net.efkrdnz.jjkstrongest.procedures.DomainUVEntityTickProcedure;
import net.efkrdnz.jjkstrongest.procedures.MalevolentShrineSummonProcedure;
import net.efkrdnz.jjkstrongest.procedures.MalevolentShrineTickProcedure;

import com.mojang.brigadier.arguments.FloatArgumentType;

import java.util.Locale;

/**
 * Domain mechanics on the end of a command, because there was no way to see them.
 *
 * <p>Every problem this system has had was invisible from inside the game: a barrier that
 * was not there because of the gamemode, a clash that never ran because nothing looked for
 * it, integrity falling with nothing on screen to say so. These make the engine's state
 * legible and its failure modes reachable without waiting ten minutes for a domain to run
 * its course or finding a second player.
 *
 * <p>Hangs off the existing {@code /jjk} tree, which is already gated at permission 3.
 */
@EventBusSubscriber
public class JjkSimCommand {

	/** Far enough that two 30-block shells overlap without either swallowing the other. */
	private static final double DEFAULT_DISTANCE = 45.0;

	@SubscribeEvent
	public static void registerCommand(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("jjk").requires(source -> source.hasPermission(3)).then(Commands.literal("sim")

				.then(Commands.literal("void").executes(ctx -> spawnVoid(ctx.getSource(), DEFAULT_DISTANCE))
						.then(Commands.argument("distance", FloatArgumentType.floatArg(0f, 512f)).executes(ctx -> spawnVoid(ctx.getSource(), FloatArgumentType.getFloat(ctx, "distance")))))

				.then(Commands.literal("shrine").executes(ctx -> spawnShrine(ctx.getSource(), DEFAULT_DISTANCE))
						.then(Commands.argument("distance", FloatArgumentType.floatArg(0f, 512f)).executes(ctx -> spawnShrine(ctx.getSource(), FloatArgumentType.getFloat(ctx, "distance")))))

				.then(Commands.literal("damage").then(Commands.argument("amount", FloatArgumentType.floatArg(0f, 100000f))
						.executes(ctx -> damage(ctx.getSource(), FloatArgumentType.getFloat(ctx, "amount"), true))
						.then(Commands.literal("here").executes(ctx -> damage(ctx.getSource(), FloatArgumentType.getFloat(ctx, "amount"), true)))
						.then(Commands.literal("even").executes(ctx -> damage(ctx.getSource(), FloatArgumentType.getFloat(ctx, "amount"), false)))))

				.then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))
				.then(Commands.literal("clear").executes(ctx -> clear(ctx.getSource())))
				.then(Commands.literal("noclip").executes(ctx -> noclip(ctx.getSource())))));
	}

	// ---- spawning rivals ----------------------------------------------------

	/**
	 * A rival Void, owned by the level's fake player rather than by you.
	 *
	 * <p>Ownership matters and is not decoration: a domain whose persistent data has no
	 * {@code ownerUUID} discards itself on its first tick, and one owned by you would count
	 * as <em>your</em> domain, so you could not have your own open at the same time — which
	 * is the entire point of the exercise.
	 */
	private static int spawnVoid(CommandSourceStack source, double distance) {
		ServerLevel level = source.getLevel();
		Vec3 at = placement(source, distance);
		Entity owner = FakePlayerFactory.getMinecraft(level);
		// gather = false: the real version drags everything nearby onto its floor and puts
		// the caster on it, which across a field would teleport you into the thing you asked
		// to stand outside of.
		DomainExpansionStartProcedure.execute(level, at.x, at.y, at.z, owner, 0, false);
		source.sendSuccess(() -> Component.literal("Rival Unlimited Void at " + format(at) + " — it seals in about four seconds."), true);
		return 1;
	}

	private static int spawnShrine(CommandSourceStack source, double distance) {
		ServerLevel level = source.getLevel();
		Vec3 at = placement(source, distance);
		Entity owner = FakePlayerFactory.getMinecraft(level);
		MalevolentShrineSummonProcedure.execute(level, owner, at.x, at.y, at.z);
		source.sendSuccess(() -> Component.literal("Rival Malevolent Shrine at " + format(at)
				+ " — it should erode a barrier from outside, never cut terrain inside one."), true);
		return 1;
	}

	/** Straight ahead on the horizontal, so the rival lands where you are looking. */
	private static Vec3 placement(CommandSourceStack source, double distance) {
		Vec3 origin = source.getPosition();
		Entity entity = source.getEntity();
		if (entity == null)
			return origin.add(distance, 0, 0);
		Vec3 look = entity.getLookAngle();
		Vec3 flat = new Vec3(look.x, 0, look.z);
		if (flat.lengthSqr() < 1.0E-6)
			flat = new Vec3(1, 0, 0);
		return origin.add(flat.normalize().scale(distance));
	}

	// ---- barrier damage -----------------------------------------------------

	/**
	 * @param concentrated true drives one patch to zero, which is a breach and a hole you
	 *                     can walk through; false spreads it, which is how a shell shatters
	 *                     as a piece. The two failure modes look and play differently and
	 *                     this is the only way to reach either on demand.
	 */
	private static int damage(CommandSourceStack source, float amount, boolean concentrated) {
		ServerLevel level = source.getLevel();
		Entity entity = source.getEntity();
		if (entity == null) {
			source.sendFailure(Component.literal("Needs to be run by something standing in a domain."));
			return 0;
		}
		DomainUVEntity domain = nearestVoidTo(level, entity);
		if (domain == null) {
			source.sendFailure(Component.literal("No Unlimited Void within 128 blocks."));
			return 0;
		}
		DomainShell shell = domain.shell();
		if (shell == null) {
			source.sendFailure(Component.literal("That domain has no barrier."));
			return 0;
		}
		if (concentrated) {
			Vec3 aim = entity.getLookAngle();
			shell.applyStrike(aim, amount, 2);
		} else {
			shell.applyPressure(amount / DomainShell.CELLS);
		}
		domain.setShellIntegrity(shell.totalIntegrity());
		float integrity = shell.totalIntegrity();
		int breaches = shell.breachCount();
		source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT, "%s %.0f — integrity %.1f%%, %d breach%s",
				concentrated ? "Struck the face you are looking at for" : "Spread", amount, integrity * 100f, breaches, breaches == 1 ? "" : "es")), true);
		return 1;
	}

	// ---- readout ------------------------------------------------------------

	private static int info(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		Vec3 origin = source.getPosition();
		int found = 0;
		for (DomainSource domain : DomainRegistry.domainsIn(level)) {
			if (!domain.isAlive() || !(domain instanceof Entity entity))
				continue;
			if (entity.position().distanceToSqr(origin) > 256.0 * 256.0)
				continue;
			found++;
			source.sendSuccess(() -> Component.literal(describe(domain, entity, origin)), false);
		}
		if (found == 0)
			source.sendSuccess(() -> Component.literal("No domains within 256 blocks."), false);
		return found;
	}

	private static String describe(DomainSource domain, Entity entity, Vec3 origin) {
		DomainSphere sphere = domain.volume();
		StringBuilder out = new StringBuilder();
		out.append(domain.definition().id()).append("  ").append(domain.isClosed() ? "closed" : "open");
		out.append(String.format(Locale.ROOT, "  %s %.0f%%", domain.phase(), domain.phaseProgress() * 100f));
		out.append(String.format(Locale.ROOT, "  r=%.1f/%.1f", sphere.radius(), domain.fullRadius()));
		out.append(String.format(Locale.ROOT, "  %.0fm away", Math.sqrt(entity.position().distanceToSqr(origin))));
		DomainShell shell = domain.shell();
		if (shell != null)
			out.append(String.format(Locale.ROOT, "  integrity %.1f%%  breaches %d%s", shell.totalIntegrity() * 100f, shell.breachCount(), shell.isShattered() ? " SHATTERED" : ""));
		if (domain instanceof DomainUVEntity uv)
			out.append(String.format(Locale.ROOT, "  clashHP %.0f%s", uv.getClashHP(), uv.isClashing() ? " CLASHING" : ""));
		else if (domain instanceof MalevolentShrineEntity shrine)
			out.append(String.format(Locale.ROOT, "  clashHP %.0f%s", shrine.getClashHP(), shrine.isClashing() ? " CLASHING" : ""));
		String owner = domain.domainOwnerUUID();
		out.append("  owner ").append(owner.isEmpty() ? "none" : owner.substring(0, Math.min(8, owner.length())));
		if (entity.getPersistentData().getBoolean("carveComplete"))
			out.append("  carved");
		return out.toString();
	}

	// ---- teardown -----------------------------------------------------------

	/** Collapses rather than discarding, so the carved terrain actually goes back. */
	private static int clear(CommandSourceStack source) {
		ServerLevel level = source.getLevel();
		int closed = 0;
		int open = 0;
		for (DomainSource domain : DomainRegistry.domainsIn(level)) {
			if (!domain.isAlive())
				continue;
			if (domain instanceof DomainUVEntity uv) {
				uv.getPersistentData().putInt("duration", 0);
				DomainUVEntityTickProcedure.beginCollapse(uv);
				closed++;
			} else if (domain instanceof MalevolentShrineEntity shrine) {
				MalevolentShrineTickProcedure.beginCollapse(shrine);
				open++;
			}
		}
		int closedCount = closed;
		int openCount = open;
		source.sendSuccess(() -> Component.literal("Collapsing " + closedCount + " closed and " + openCount + " open — terrain restores as they go."), true);
		return closed + open;
	}

	// ---- the escape hatch ---------------------------------------------------

	private static int noclip(CommandSourceStack source) {
		Entity entity = source.getEntity();
		if (!(entity instanceof ServerPlayer player)) {
			source.sendFailure(Component.literal("Only a player can be exempted."));
			return 0;
		}
		boolean exempt = DomainNoclip.toggle(player.getUUID());
		// Collision is evaluated client-side too, so an exemption the client does not know
		// about is just the server letting you through a wall the client holds you against.
		PacketDistributor.sendToPlayer(player, new DomainNoclipPacket(player.getUUID(), exempt));
		source.sendSuccess(() -> Component.literal(exempt ? "Domain barriers will let you through." : "Domain barriers hold you again."), false);
		return 1;
	}

	// ---- shared -------------------------------------------------------------

	private static DomainUVEntity nearestVoidTo(ServerLevel level, Entity entity) {
		// Whatever you are standing in wins; failing that, the nearest one worth aiming at.
		DomainSphere inside = DomainRegistry.sphereAt(level, entity.getX(), entity.getEyeY(), entity.getZ());
		DomainUVEntity best = null;
		double bestSq = 128.0 * 128.0;
		for (DomainUVEntity candidate : DomainRegistry.voidsIn(level)) {
			if (!candidate.isAlive())
				continue;
			if (inside != null && candidate.volume().center().equals(inside.center()))
				return candidate;
			double distSq = candidate.position().distanceToSqr(entity.position());
			if (distSq <= bestSq) {
				bestSq = distSq;
				best = candidate;
			}
		}
		return best;
	}

	private static String format(Vec3 at) {
		return String.format(Locale.ROOT, "%.0f %.0f %.0f", at.x, at.y, at.z);
	}
}
