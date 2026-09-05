package net.minecraft.world.phys;

public class AABB {
	public final double minX, minY, minZ, maxX, maxY, maxZ;
	public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this.minX = minX; this.minY = minY; this.minZ = minZ;
		this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
	}
}
