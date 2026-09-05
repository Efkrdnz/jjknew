package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

import net.efkrdnz.jjkstrongest.domain.DomainShell;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Where an incoming shell grid lands on the client.
 *
 * <p>Writes straight into the entity's own {@link DomainShell} so collision and rendering
 * read the same object the server is driving. The version stamp exists because these
 * arrive several times a second and UDP-ish ordering is not guaranteed — an older grid
 * overwriting a newer one would make cracks flicker back and forth.
 */
@EventBusSubscriber(modid = "jjk_strongest", value = Dist.CLIENT)
public final class DomainShellClient {

	private static final Map<Integer, Integer> VERSIONS = new HashMap<>();

	private DomainShellClient() {
	}

	public static void accept(int entityId, int version, byte[] cells) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;
		Integer seen = VERSIONS.get(entityId);
		if (seen != null && version < seen)
			return;
		Entity entity = mc.level.getEntity(entityId);
		if (!(entity instanceof DomainUVEntity domain))
			return;
		DomainShell shell = domain.shell();
		if (shell == null)
			return;
		shell.applyCells(cells);
		VERSIONS.put(entityId, version);
	}

	/**
	 * Dropped when the domain goes, so the map does not grow across a session.
	 *
	 * <p>Handled here rather than from {@code DomainRegistry} on purpose: that class is
	 * common code, and reaching into a client-only one from it would be a class-loading
	 * hazard on a dedicated server.
	 */
	@SubscribeEvent
	public static void onLeave(EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof DomainUVEntity domain)
			VERSIONS.remove(domain.getId());
	}
}
