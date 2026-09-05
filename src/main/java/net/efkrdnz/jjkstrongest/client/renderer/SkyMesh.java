package net.efkrdnz.jjkstrongest.client.renderer;

/**
 * The unit sphere the domain skies are painted on.
 *
 * <p>Lived inside {@link DomainUVRenderer} while it was the only thing drawing one. The
 * Shrine's sky dome wants the same mesh, so it is shared here rather than built twice; both
 * fragment stages reconstruct their view ray from the interpolated position, and both want
 * the same resolution for the same reason — at 24 x 48 each quad spans about seven degrees
 * and that faceting shows.
 *
 * <p>Wound inward, as (x, y, z, u, v) per vertex, with u and v the fractions of the segment
 * counts. The Void's damage grid is keyed on exactly that mapping, so it must not change.
 */
public final class SkyMesh {

	public static final int LAT_SEGMENTS = 32;
	public static final int LON_SEGMENTS = 64;
	/** Five floats per vertex, four vertices per quad, LAT x LON quads. */
	public static final float[] UNIT_SPHERE = build();

	private SkyMesh() {
	}

	private static float[] build() {
		float[] data = new float[LAT_SEGMENTS * LON_SEGMENTS * 4 * 5];
		int i = 0;
		for (int lat = 0; lat < LAT_SEGMENTS; lat++) {
			float theta1 = (lat / (float) LAT_SEGMENTS) * (float) Math.PI;
			float theta2 = ((lat + 1) / (float) LAT_SEGMENTS) * (float) Math.PI;
			for (int lon = 0; lon < LON_SEGMENTS; lon++) {
				float phi1 = (lon / (float) LON_SEGMENTS) * 2.0f * (float) Math.PI;
				float phi2 = ((lon + 1) / (float) LON_SEGMENTS) * 2.0f * (float) Math.PI;
				float u1 = lon / (float) LON_SEGMENTS;
				float u2 = (lon + 1) / (float) LON_SEGMENTS;
				float v1 = lat / (float) LAT_SEGMENTS;
				float v2 = (lat + 1) / (float) LAT_SEGMENTS;
				// wound v1, v4, v3, v2 so the faces look inward
				i = put(data, i, theta1, phi1, u1, v1);
				i = put(data, i, theta2, phi1, u1, v2);
				i = put(data, i, theta2, phi2, u2, v2);
				i = put(data, i, theta1, phi2, u2, v1);
			}
		}
		return data;
	}

	private static int put(float[] data, int i, float theta, float phi, float u, float v) {
		data[i++] = (float) (Math.sin(theta) * Math.cos(phi));
		data[i++] = (float) Math.cos(theta);
		data[i++] = (float) (Math.sin(theta) * Math.sin(phi));
		data[i++] = u;
		data[i++] = v;
		return i;
	}
}
