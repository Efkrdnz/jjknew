package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import net.mcreator.jjkstrongest.network.JjkStrongestModVariables;

public class InfinityProcessCrushProcedure {
	public static void execute(LevelAccessor world, Entity entity, Entity sourceEntity) {
		if (entity == null || sourceEntity == null || !(world instanceof Level _level))
			return;
		CompoundTag nbt = entity.getPersistentData();
		if (!nbt.getBoolean("infinityCrushing"))
			return;
		boolean is_holding = false;
		if (sourceEntity instanceof Player) {
			is_holding = (sourceEntity.getCapability(JjkStrongestModVariables.PLAYER_VARIABLES_CAPABILITY, null).orElse(new JjkStrongestModVariables.PlayerVariables())).infinity_crush;
		}
		BlockPos wallPos = new BlockPos(nbt.getInt("infinityCrushX"), nbt.getInt("infinityCrushY"), nbt.getInt("infinityCrushZ"));
		String directionName = nbt.getString("infinityCrushDirection");
		Direction hitDirection = Direction.byName(directionName);
		BlockPos[] crushBlocks;
		if (hitDirection == Direction.NORTH || hitDirection == Direction.SOUTH) {
			crushBlocks = new BlockPos[]{wallPos, wallPos.offset(1, 0, 0), wallPos.offset(0, 1, 0), wallPos.offset(1, 1, 0)};
		} else {
			crushBlocks = new BlockPos[]{wallPos, wallPos.offset(0, 0, 1), wallPos.offset(0, 1, 0), wallPos.offset(0, 1, 1)};
		}
		float totalHardness = 0;
		int validBlocks = 0;
		for (BlockPos pos : crushBlocks) {
			BlockState state = _level.getBlockState(pos);
			float hardness = state.getDestroySpeed(_level, pos);
			if (hardness >= 0 && hardness < 50) {
				totalHardness += hardness;
				validBlocks++;
			}
		}
		if (validBlocks == 0)
			return;
		float avgHardness = totalHardness / validBlocks;
		float breakTime = Math.min(avgHardness * 1.0f, 5.0f);
		float timer = nbt.getFloat("infinityCrushTimer");
		if (is_holding) {
			timer += 0.05f;
			nbt.putFloat("infinityCrushTimer", timer);
		}
		int breakStage = (int) Math.min((timer / breakTime) * 10, 9);
		if (is_holding && entity instanceof LivingEntity _entity && timer % 0.5f < 0.1f) {
			_entity.hurt(new DamageSource(_entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.CRAMMING)), (float) (4.0 * nbt.getDouble("infinityCrushForce")));
		}
		if (world instanceof ServerLevel _slevel) {
			for (BlockPos pos : crushBlocks) {
				if (is_holding) {
					_slevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, (int) (breakStage * 0.5), 0.3, 0.3, 0.3, 0.01);
				} else {
					_slevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.01);
				}
			}
		}
		if (is_holding && timer >= breakTime) {
			for (BlockPos pos : crushBlocks) {
				BlockState state = _level.getBlockState(pos);
				float hardness = state.getDestroySpeed(_level, pos);
				if (hardness >= 0 && hardness < 50) {
					// drop items before breaking
					_level.destroyBlock(pos, true);
					// use command to ensure client sync
					if (world instanceof ServerLevel _slevel) {
						_slevel.getServer().getCommands().performPrefixedCommand(
								new CommandSourceStack(CommandSource.NULL, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Vec2.ZERO, _slevel, 4, "", Component.literal(""), _slevel.getServer(), null).withSuppressedOutput(),
								"setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " air replace");
					}
				}
			}
			if (entity instanceof LivingEntity _entity) {
				_entity.hurt(new DamageSource(_entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.CRAMMING)), (float) (avgHardness * 6.0));
			}
			nbt.putFloat("infinityCrushTimer", 0);
			nbt.putBoolean("infinityCrushing", false);
		}
		if (!is_holding && timer > 0) {
			timer -= 0.02f;
			if (timer < 0)
				timer = 0;
			nbt.putFloat("infinityCrushTimer", timer);
		}
	}
}
