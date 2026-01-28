package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
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
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import java.util.List;
import java.util.Comparator;

public class LapseBlueOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double attraction = 0;
		boolean move = !entity.getPersistentData().getBoolean("stay");
		entity.setNoGravity(true);
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
								&& !entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("technique")))) {
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
		int yIterationsSphere = verticalRadiusSphere;
		for (int i = -yIterationsSphere; i <= yIterationsSphere; i++) {
			for (int xi = -horizontalRadiusSphere; xi <= horizontalRadiusSphere; xi++) {
				for (int zi = -horizontalRadiusSphere; zi <= horizontalRadiusSphere; zi++) {
					double distanceSq = (xi * xi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere) + (i * i) / (double) (verticalRadiusSphere * verticalRadiusSphere)
							+ (zi * zi) / (double) (horizontalRadiusSphere * horizontalRadiusSphere);
					if (distanceSq <= 1.0) {
						BlockPos blockPos = BlockPos.containing(x + xi, y + i, z + zi);
						BlockState blockState = world.getBlockState(blockPos);
						if (!blockState.getFluidState().isEmpty()) {
							continue;
						}
						if (!(blockState.getDestroySpeed(world, blockPos) == -1) && !world.isEmptyBlock(blockPos)) {
							if (Math.random() >= 0.8) {
								if (world instanceof ServerLevel _level) {
									FallingBlockEntity fallingBlock = FallingBlockEntity.fall(_level, blockPos, blockState);
									double blockAttraction = Math.sqrt(Math.pow(x - (x + xi), 2) + Math.pow(y - (y + i), 2) + Math.pow(z - (z + zi), 2));
									if (blockAttraction > 0) {
										fallingBlock.setDeltaMovement(new Vec3(((x - (x + xi)) / blockAttraction) * 0.5, ((y - (y + i)) / blockAttraction) * 0.5, ((z - (z + zi)) / blockAttraction) * 0.5));
									}
								}
							} else {
								if (world instanceof ServerLevel _level)
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3(x + xi, y + i, z + zi), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(), "setblock ~ ~ ~ air replace");
							}
						}
					}
				}
			}
		}
		// attract entities and falling blocks continuously
		{
			BlueParticleExecuteProcedure.execute(world, entity);
			final Vec3 _center = new Vec3(x, y, z);
			List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate((entity.getPersistentData().getDouble("TechniquePower") * 9) / 2d), e -> true).stream()
					.sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
			for (Entity entityiterator : _entfound) {
				if (!(entity == entityiterator) && !(entity.getPersistentData().getString("caster")).equals(entityiterator.getDisplayName().getString())
						&& !entityiterator.getType().is(TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("technique")))) {
					entityiterator.setOnGround(false);
					attraction = Math.sqrt(Math.pow(entity.getX() - entityiterator.getX(), 2) + Math.pow(entity.getY() - entityiterator.getY(), 2) + Math.pow(entity.getZ() - entityiterator.getZ(), 2));
					if (attraction > 0) {
						entityiterator.setDeltaMovement(new Vec3(((entity.getX() - entityiterator.getX()) / attraction), ((entity.getY() - entityiterator.getY()) / attraction), ((entity.getZ() - entityiterator.getZ()) / attraction)));
					}
					if (!(entityiterator instanceof ItemEntity) && !(entityiterator instanceof FallingBlockEntity)) {
						entityiterator.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("jjk_strongest:jujutsu"))), entity),
								(float) (entity.getPersistentData().getDouble("TechniquePower") * 2));
					}
				}
			}
		}
	}
}
