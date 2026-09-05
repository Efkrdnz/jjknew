package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.efkrdnz.jjkstrongest.domain.DomainRegistry;
import net.efkrdnz.jjkstrongest.domain.DomainSphere;
import net.efkrdnz.jjkstrongest.entity.DomainUVEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * The shrine eating the ground beneath it.
 *
 * <p>Two things were wrong here. It cleared terrain <em>through</em> a rival domain's
 * barrier, which contradicts the rule the rest of the system now follows — a closed
 * barrier stops what an open domain throws at it, and that has to include the ground.
 * And it destroyed each block by building a {@code CommandSourceStack} and parsing
 * {@code "setblock ~ ~ ~ air"} for it, one command dispatch per block, in a volume that
 * grows every four ticks. That was comfortably the most expensive thing in the mod.
 *
 * <p>Blocks are written directly now, with client updates only: neighbour notification
 * across a volume this size sets off gravity and redstone cascades for no benefit.
 */
public class MalevolentShrineTickBlockBreakingProcedure {

	private static final int SET_FLAGS = Block.UPDATE_CLIENTS;

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;

		double life = entity.getPersistentData().getDouble("life") + 1;
		entity.getPersistentData().putDouble("life", life);

		if (life == 1 && world instanceof Level level) {
			if (!level.isClientSide())
				level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:sukuna_domain_ost")), SoundSource.AMBIENT, 1, 1);
			else
				level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("jjk_strongest:sukuna_domain_ost")), SoundSource.AMBIENT, 1, 1, false);
		}
		if (life == 60)
			entity.getPersistentData().putBoolean("active", true);

		if (life <= 60 || life % 4 != 0)
			return;
		if (!(world instanceof ServerLevel level))
			return;

		double previousRadius = entity.getPersistentData().getDouble("domainBBRadius");
		double radius = previousRadius + 1;
		entity.getPersistentData().putDouble("domainBBRadius", radius);
		carve(level, x, y, z, (int) radius, (int) previousRadius);
	}

	private static void carve(ServerLevel level, double x, double y, double z, int radius, int previousRadius) {
		int horizontal = radius - 1;
		if (horizontal <= 0)
			return;

		// Every closed barrier in the level, resolved once. Testing per block against the
		// registry would repeat this lookup tens of thousands of times a pass.
		List<DomainSphere> barriers = new ArrayList<>();
		for (DomainUVEntity domain : DomainRegistry.voidsIn(level)) {
			if (!domain.isAlive())
				continue;
			DomainSphere sphere = domain.volume();
			if (sphere.isUsable())
				barriers.add(sphere);
		}

		int previousHorizontal = Math.max(0, previousRadius - 1);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < radius; i++) {
			for (int xi = -horizontal; xi <= horizontal; xi++) {
				for (int zi = -horizontal; zi <= horizontal; zi++) {
					double normalised = (xi * xi) / (double) (horizontal * horizontal) + (i * i) / (double) (radius * radius) + (zi * zi) / (double) (horizontal * horizontal);
					if (normalised > 1.0)
						continue;
					// Anything within last pass's ellipsoid was already cleared, so only the
					// newly exposed shell can still hold blocks.
					if (previousHorizontal > 0 && previousRadius > 0) {
						double before = (xi * xi) / (double) (previousHorizontal * previousHorizontal) + (i * i) / (double) (previousRadius * previousRadius)
								+ (zi * zi) / (double) (previousHorizontal * previousHorizontal);
						if (before <= 1.0)
							continue;
					}

					pos.set(x + xi, y + i - 1, z + zi);
					BlockState state = level.getBlockState(pos);
					if (state.isAir() || state.getDestroySpeed(level, pos) == -1)
						continue;
					if (isSheltered(barriers, pos))
						continue;
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), SET_FLAGS);
				}
			}
		}
	}

	/** A closed barrier protects the ground inside it, exactly as it protects the people. */
	private static boolean isSheltered(List<DomainSphere> barriers, BlockPos pos) {
		for (int i = 0; i < barriers.size(); i++) {
			if (barriers.get(i).withinRadius(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5))
				return true;
		}
		return false;
	}
}
