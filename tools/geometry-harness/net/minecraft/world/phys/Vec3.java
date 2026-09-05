package net.minecraft.world.phys;

/** Faithful stand-in for Minecraft's Vec3, so the domain geometry can be run for real. */
public class Vec3 {
	public final double x, y, z;

	public Vec3(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }

	public Vec3 add(Vec3 o) { return new Vec3(x + o.x, y + o.y, z + o.z); }
	public Vec3 add(double dx, double dy, double dz) { return new Vec3(x + dx, y + dy, z + dz); }
	public Vec3 subtract(Vec3 o) { return new Vec3(x - o.x, y - o.y, z - o.z); }
	public Vec3 scale(double f) { return new Vec3(x * f, y * f, z * f); }
	public double dot(Vec3 o) { return x * o.x + y * o.y + z * o.z; }
	public double lengthSqr() { return x * x + y * y + z * z; }
	public double length() { return Math.sqrt(lengthSqr()); }
	public Vec3 normalize() { double l = length(); return l < 1.0E-4 ? new Vec3(0, 0, 0) : scale(1.0 / l); }
	public double distanceTo(Vec3 o) { return subtract(o).length(); }
	public double distanceToSqr(Vec3 o) { return subtract(o).lengthSqr(); }
	public double distanceToSqr(double ox, double oy, double oz) {
		double dx = x - ox, dy = y - oy, dz = z - oz;
		return dx * dx + dy * dy + dz * dz;
	}
	@Override public String toString() { return String.format("(%.4f, %.4f, %.4f)", x, y, z); }
}
