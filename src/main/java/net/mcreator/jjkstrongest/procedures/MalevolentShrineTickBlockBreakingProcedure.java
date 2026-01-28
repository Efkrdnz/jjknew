package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

public class MalevolentShrineTickBlockBreakingProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double radius = 0;
		entity.getPersistentData().putDouble("life", (entity.getPersistentData().getDouble("life") + 1));
		if (entity.getPersistentData().getDouble("life") == 1) {
			if (world instanceof Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:sukuna_domain_ost")), SoundSource.NEUTRAL, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("jjk_strongest:sukuna_domain_ost")), SoundSource.NEUTRAL, 1, 1, false);
				}
			}
		}
		if (entity.getPersistentData().getDouble("life") == 60) {
			entity.getPersistentData().putBoolean("active", true);
		}
		if (entity.getPersistentData().getDouble("life") > 60) {
			if (entity.getPersistentData().getDouble("life") % 4 == 0) {
				entity.getPersistentData().putDouble("domainBBRadius", (entity.getPersistentData().getDouble("domainBBRadius") + 1));
				radius = entity.getPersistentData().getDouble("domainBBRadius");
				if (!world.isClientSide()) {
					int horizontalRadiusHemiTop = (int) radius - 1;
					int verticalRadiusHemiTop = (int) radius;
					int yIterationsHemiTop = verticalRadiusHemiTop;
					for (int i = 0; i < yIterationsHemiTop; i++) {
						if (i == verticalRadiusHemiTop) {
							continue;
						}
						for (int xi = -horizontalRadiusHemiTop; xi <= horizontalRadiusHemiTop; xi++) {
							for (int zi = -horizontalRadiusHemiTop; zi <= horizontalRadiusHemiTop; zi++) {
								double distanceSq = (xi * xi) / (double) (horizontalRadiusHemiTop * horizontalRadiusHemiTop) + (i * i) / (double) (verticalRadiusHemiTop * verticalRadiusHemiTop)
										+ (zi * zi) / (double) (horizontalRadiusHemiTop * horizontalRadiusHemiTop);
								if (distanceSq <= 1.0) {
									if (!((world.getBlockState(BlockPos.containing(x + xi, y + i - 1, z + zi))).getBlock() == Blocks.AIR)
											&& world.getBlockState(BlockPos.containing(x + xi, y + i - 1, z + zi)).getDestroySpeed(world, BlockPos.containing(x + xi, y + i - 1, z + zi)) != -1) {
										if (world instanceof ServerLevel _level)
											_level.getServer().getCommands().performPrefixedCommand(
													new CommandSourceStack(CommandSource.NULL, new Vec3(x + xi, (y + i - 1), z + zi), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
													"setblock ~ ~ ~ air");
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
