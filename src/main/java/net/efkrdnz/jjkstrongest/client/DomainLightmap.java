package net.efkrdnz.jjkstrongest.client;

import org.joml.Vector3f;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.domain.DomainPhase;
import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;
import net.efkrdnz.jjkstrongest.procedures.SetupDimensionProcedure;

/**
 * Lights the inside of a domain by rewriting the lightmap, not by drugging everyone in it.
 *
 * <p>The barrier blocks this system replaced were {@code lightLevel(s -> 15)}, so they lit
 * the interior for free. A carved-out air pocket is at sky-light zero, which would leave
 * every entity in there a silhouette, so the first pass at this handed every living thing
 * inside the sphere a renewing Night Vision. That worked, and it was wrong in three ways:
 * it is a status effect, so it shows in the HUD and the effect list and can be cleansed by
 * milk; it is server state and a packet per entity per second; and Night Vision flattens
 * colour, which is the one thing the interior's palette is built out of.
 *
 * <p>{@code CUSTOM_LIGHTS} is a hook the generated dimension effects have always had and
 * nothing has ever used. It hands us the sixteen-by-sixteen lightmap ramp as it is built,
 * so the interior can have a floor under its brightness and a cold cast, client-side, with
 * no entity ever knowing about it.
 *
 * <p>Scope: this rides on the mod's own {@code DimensionSpecialEffects}, which
 * {@link SetupDimensionProcedure} installs for the Overworld. A domain cast in the Nether
 * or the End renders at whatever ambient light is actually there. Registering the mod's
 * effects for those dimensions would also hand their skies to the shrine's sky override,
 * whose predicate claims every frame, so that is deliberately not done here.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public final class DomainLightmap {

	/** Brightness the interior never falls below, at full strength. */
	private static final float AMBIENT = 0.70f;
	/** Cold cast on that floor: a touch under on red, a touch over on blue. */
	private static final float TINT_R = 0.88f;
	private static final float TINT_G = 0.93f;
	private static final float TINT_B = 1.06f;
	/** How far the rest of the ramp is pulled toward neutral at full strength. */
	private static final float COOL = 0.36f;
	/** Per-tick approach, so walking through the shell is a fade rather than a switch. */
	private static final float EASE = 0.18f;

	/**
	 * How much of the domain's lighting is in force, 0..1.
	 *
	 * <p>Held as a field because {@code adjustLightmapColors} is called for all 256 texels
	 * of the ramp every time it is rebuilt. Asking the registry where the camera is once
	 * per texel would be 256 sphere tests a frame for an answer that cannot change inside
	 * a single frame; this is computed once a tick instead.
	 */
	private static volatile float strength;

	private DomainLightmap() {
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> SetupDimensionProcedure.JjkStrongestModDimensionSpecialEffects.CUSTOM_LIGHTS.add(DomainLightmap::adjust));
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		strength = strength + (target() - strength) * EASE;
		if (strength < 0.002f)
			strength = 0.0f;
	}

	private static float target() {
		if (DomainRegistry.activeCount == 0)
			return 0.0f;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		Entity camera = mc.getCameraEntity();
		if (level == null || camera == null)
			return 0.0f;
		for (DomainUVEntity domain : DomainRegistry.voidsIn(level)) {
			if (!domain.isAlive())
				continue;
			DomainSphere sphere = domain.sphere();
			if (!sphere.isUsable() || !sphere.contains(camera.getX(), camera.getEyeY(), camera.getZ()))
				continue;
			DomainPhase phase = domain.getPhase();
			// The light arrives with the walls and leaves with them.
			if (phase == DomainPhase.COLLAPSING)
				return 1.0f - domain.getPhaseProgress();
			if (phase == DomainPhase.EXPANDING)
				return domain.getPhaseProgress();
			return 1.0f;
		}
		return 0.0f;
	}

	/**
	 * @param params {@code {level, partialTick, skyDarken, blockLightRedFlicker, skyLight,
	 *               pixelX, pixelY, colors}} — the arguments of
	 *               {@code DimensionSpecialEffects#adjustLightmapColors}, boxed by the
	 *               generated effects class.
	 */
	private static void adjust(Object[] params) {
		float s = strength;
		if (s <= 0.0f || params.length < 8 || !(params[7] instanceof Vector3f colors))
			return;

		// A floor, so nothing in the sphere is ever a silhouette however dark the pocket
		// the carve left behind actually is.
		float floor = AMBIENT * s;
		float r = Math.max(colors.x(), floor * TINT_R);
		float g = Math.max(colors.y(), floor * TINT_G);
		float b = Math.max(colors.z(), floor * TINT_B);

		// And a cast over the whole ramp, so a torch carried inside does not read as warm
		// firelight against a place with no colour in it.
		float cool = COOL * s;
		float grey = (r + g + b) * (1.0f / 3.0f);
		colors.set(Mth.lerp(cool, r, grey * TINT_R), Mth.lerp(cool, g, grey * TINT_G), Mth.lerp(cool, b, Math.min(1.0f, grey * TINT_B)));
	}
}
