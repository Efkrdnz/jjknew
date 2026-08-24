package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Comparator;

public class LapseBlueOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double attraction = 0;
		boolean move = !entity.getPersistentData().getBoolean("stay");
		entity.setNoGravity(true);
		if (move) {
			LapseBlueMoveProcedure.execute(entity);
		}
		entity.setDeltaMovement(new Vec3(0, 0, 0));
		entity.getPersistentData().putDouble("BlueLife", (entity.getPersistentData().getDouble("BlueLife") + 1));
		if (entity.getPersistentData().getDouble("BlueLife") >= 200) {
			if (!world.isClientSide()) {
				if (!entity.level().isClientSide())
					entity.discard();
				{
					final Vec3 _center = new Vec3(x, y, z);
					List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getPersistentData().getDouble("TechniquePower") * 9) / 2d), e -> true).stream()
							.sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
					for (Entity entityiterator : _entfound) {
						if (!(entity == entityiterator) && !(entity.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString())
								&& !entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique")))) {
							entityiterator.setOnGround(false);
							entityiterator.setDeltaMovement(new Vec3((Mth.nextDouble(RandomSource.create(), -1.5, 1.5)), (Mth.nextDouble(RandomSource.create(), -0.5, 0.5)), (Mth.nextDouble(RandomSource.create(), -1.5, 1.5))));
						}
					}
				}
			}
		}
		// break blocks and spawn falling blocks, skip liquids
		int horizontalRadiusSphere = (int) (entity.getPersistentData().getDouble("TechniquePower") * 5) - 1;
		int verticalRadiusSphere = (int) (entity.getPersistentData().getDouble("TechniquePower") * 5) - 1;
		if (horizontalRadiusSphere < 1)
			horizontalRadiusSphere = 1;
		if (verticalRadiusSphere < 1)
			verticalRadiusSphere = 1;
		int yIterationsSphere = verticalRadiusSphere;
		// 20% become FallingBlockEntity, 80% are destroyed
		final double FALLING_BLOCK_CHANCE = 0.20;
		for (int yi = -yIterationsSphere; yi <= yIterationsSphere; yi++) {
			for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
				for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
					double distanceSq = (xi * xi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere) + (yi * yi) / (double) (verticalRadiusSphere * verticalRadiusSphere)
							+ (zi * zi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere);
					if (distanceSq > 1.0)
						continue;
					BlockPos blockPos = BlockPos.containing(x + xi, y + yi, z + zi);
					BlockState blockState = world.getBlockState(blockPos);
					// skip air
					if (world.isEmptyBlock(blockPos))
						continue;
					// skip liquids
					if (!blockState.getFluidState().isEmpty())
						continue;
					// skip unbreakable
					if (blockState.getDestroySpeed(world, blockPos) == -1)
						continue;
					// server only
					if (!(world instanceof ServerLevel level))
						continue;
					if (level.random.nextDouble() < FALLING_BLOCK_CHANCE) {
						FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, blockPos, blockState);
						double dx = x - (x + xi);
						double dy = y - (y + yi);
						double dz = z - (z + zi);
						double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
						if (dist > 0) {
							double speed = 0.5;
							fallingBlock.setDeltaMovement(new Vec3((dx / dist) * speed, (dy / dist) * speed, (dz / dist) * speed));
						}
					} else {
						level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
					}
				}
			}
		}
		{
			BlueParticleExecuteProcedure.execute(world, entity);
			final Vec3 _center = new Vec3(x, y, z);
			List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getPersistentData().getDouble("TechniquePower") * 9) / 2d), e -> true).stream()
					.sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
			for (Entity entityiterator : _entfound) {
				if (!(entity == entityiterator) && !(entity.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString())
						&& !entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse("technique")))) {
					entityiterator.setOnGround(false);
					attraction = Math.sqrt(Math.pow(entity.getX() - entityiterator.getX(), 2) + Math.pow(entity.getY() - entityiterator.getY(), 2) + Math.pow(entity.getZ() - entityiterator.getZ(), 2));
					if (attraction > 0) {
						entityiterator.setDeltaMovement(new Vec3(((entity.getX() - entityiterator.getX()) / attraction), ((entity.getY() - entityiterator.getY()) / attraction), ((entity.getZ() - entityiterator.getZ()) / attraction)));
						entityiterator.fallDistance = 0;
					}
					if (world.getLevelData().getGameTime() % 5 == 0) {
						if (!(entityiterator instanceof ItemEntity) && !(entityiterator instanceof FallingBlockEntity)) {
							entityiterator.hurt(
									new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_blue"))), entity),
									(float) (entity.getPersistentData().getDouble("TechniquePower") * 2));
						}
					}
				}
			}
		}
	}
}
