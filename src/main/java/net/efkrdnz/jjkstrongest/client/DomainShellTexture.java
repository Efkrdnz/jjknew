package net.efkrdnz.jjkstrongest.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import net.efkrdnz.jjkstrongest.domain.DomainShell;

/**
 * Hands the barrier's damage grid to the shader as a small texture.
 *
 * <p>Thirty-two by sixteen, one texel per {@link DomainShell} cell, laid out to match the
 * sphere mesh's equirectangular UV so the fragment shader can look up "how broken is the
 * barrier in this direction" with a plain {@code texture(ShellSampler, texCoord)}.
 *
 * <p>One texture, re-uploaded per domain per frame. That is 512 texels — cheaper than
 * tracking which of several domains owns which texture, and there is rarely more than one.
 */
@OnlyIn(Dist.CLIENT)
public final class DomainShellTexture {

	private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("jjk_strongest", "domain_shell_grid");

	private static DynamicTexture texture;

	private DomainShellTexture() {
	}

	/** Uploads the grid and returns its GL id, or -1 if it could not be prepared. */
	public static int upload(DomainShell shell) {
		if (shell == null)
			return -1;
		try {
			if (texture == null) {
				texture = new DynamicTexture(DomainShell.LON_CELLS, DomainShell.LAT_CELLS, true);
				Minecraft.getInstance().getTextureManager().register(ID, texture);
				// Smooth between cells; hard texel edges would read as square blotches
				// rather than damage spreading across the surface.
				texture.setFilter(true, false);
			}
			NativeImage image = texture.getPixels();
			if (image == null)
				return -1;
			for (int lat = 0; lat < DomainShell.LAT_CELLS; lat++) {
				for (int lon = 0; lon < DomainShell.LON_CELLS; lon++) {
					int value = Math.round(shell.integrityAt(lat * DomainShell.LON_CELLS + lon));
					value = Math.max(0, Math.min(255, value));
					// NativeImage packs ABGR, so the low byte is the red channel the
					// shader reads.
					image.setPixelRGBA(lon, lat, 0xFF000000 | value);
				}
			}
			texture.upload();
			return texture.getId();
		} catch (Exception failedToPrepare) {
			return -1;
		}
	}
}
