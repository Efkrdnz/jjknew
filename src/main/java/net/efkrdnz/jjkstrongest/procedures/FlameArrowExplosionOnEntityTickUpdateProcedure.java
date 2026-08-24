package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;

import java.util.List;
import java.util.Comparator;

public class FlameArrowExplosionOnEntityTickUpdateProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		double life = entity.getPersistentData().getDouble("life");
		entity.getPersistentData().putDouble("life", life + 1);
		entity.setNoGravity(true);
		// get owner
		Entity owner = (entity instanceof TamableAnimal _tamEnt ? (Entity) _tamEnt.getOwner() : null);
		// lifetime check - 30 ticks total (1.5 seconds)
		if (life >= 30 || owner == null) {
			if (!entity.level().isClientSide())
				entity.discard();
			return;
		}
		// === TICK 0: DEEP EXPLOSION SOUND ===
		if (life == 0) {
			if (world instanceof Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.generic.explode")), SoundSource.NEUTRAL, 3.0f, 0.4f); // LOUDER, DEEPER
				}
			}
		}
		// === TICK 1: IMPACT FRAME + LONGER SCREEN SHAKE ===
		else if (life == 1) {
			if (world instanceof Level _level && _level.isClientSide && owner != null) {
				// bright orange/yellow fire impact frame
				ImpactFrameStateProcedure.INSTANCE.triggerCharged(2, 0.0f, 2.2f, 1.8f); // 2 ticks, more intense
				// LONGER, more intense screen shake
				TriggerScreenShakeProcedure.execute(_level, owner, 15, 5.5f); // 15 ticks (0.75s), very intense
			}
		}
		// === PARTICLES - EXTENDED FOR 30 TICKS ===
		if (world instanceof ServerLevel _level) {
			if (life < 12) {
				// phase 1: INTENSE initial explosion - TALL
				int particleCount = (int) (180 - life * 10); // MORE particles
				double spread = 10.0 + life * 2.0; // BIGGER spread
				_level.sendParticles(ParticleTypes.FLAME, x, y + 30, z, particleCount, spread, 60.0, spread, 0.6); // MUCH HIGHER (60 blocks up)
				_level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 30, z, particleCount / 2, spread, 60.0, spread, 0.5);
				_level.sendParticles(ParticleTypes.LAVA, x, y + 5, z, 12, 5.0, 3.0, 5.0, 0.0); // lava drips at base
			} else if (life < 25) {
				// phase 2: sustained MASSIVE fire column - VERY TALL
				_level.sendParticles(ParticleTypes.FLAME, x, y + 60, z, 80, 15.0, 60.0, 15.0, 0.4); // HUGE column (60 blocks up)
				_level.sendParticles(ParticleTypes.SMOKE, x, y + 60, z, 40, 15.0, 60.0, 15.0, 0.3);
			} else {
				// phase 3: lingering HUGE smoke cloud
				_level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y + 30, z, 30, 12.0, 20.0, 12.0, 0.12);
			}
		}
		// === DAMAGE - EXTENDED OVER 30 TICKS ===
		if (life == 3) {
			// main blast damage at tick 3 (after flash) - MATCHES VISUAL SCALE
			final Vec3 _center = new Vec3(x, y, z);
			List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(50 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
			for (Entity entityiterator : _entfound) {
				if (!(entity == entityiterator) && !(entity instanceof TamableAnimal _tamIsTamedBy && entityiterator instanceof LivingEntity _livEnt ? _tamIsTamedBy.isOwnedBy(_livEnt) : false)) {
					entityiterator.invulnerableTime = 0;
					entityiterator.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_fuga"))), owner), 50); // MASSIVE DAMAGE
					entityiterator.setSecondsOnFire(25); // VERY LONG BURN
				}
			}
			// SPHERICAL BLOCK DESTRUCTION
			if (world instanceof ServerLevel _level) {
				BlockPos centerPos = BlockPos.containing(x, y, z);
				double destructionRadius = 20.0; // 20-block sphere destruction radius
				int iRadius = (int) Math.ceil(destructionRadius);
				for (int dx = -iRadius; dx <= iRadius; dx++) {
					for (int dy = -iRadius; dy <= iRadius; dy++) {
						for (int dz = -iRadius; dz <= iRadius; dz++) {
							BlockPos pos = centerPos.offset(dx, dy, dz);
							// calculate exact distance from center
							double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
							// only destroy if within sphere radius
							if (distance <= destructionRadius) {
								BlockState state = _level.getBlockState(pos);
								if (state.getDestroySpeed(_level, pos) != -1 && !state.isAir()) {
									// destroy block using command
									_level.getServer().getCommands().performPrefixedCommand(
											new CommandSourceStack(CommandSource.NULL, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
											"setblock " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " air replace");
								}
							}
						}
					}
				}
			}
		} else if (life > 3 && life % 4 == 0 && life <= 20) {
			// periodic damage - extended duration, every 4 ticks - MATCHES CORE SIZE
			final Vec3 _center = new Vec3(x, y, z);
			List<Entity> _entfound = world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(40 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList();
			for (Entity entityiterator : _entfound) {
				if (!(entity == entityiterator) && !(entity instanceof TamableAnimal _tamIsTamedBy && entityiterator instanceof LivingEntity _livEnt ? _tamIsTamedBy.isOwnedBy(_livEnt) : false)) {
					entityiterator.invulnerableTime = 0;
					entityiterator.hurt(new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_fuga"))), owner), 5);
					entityiterator.setSecondsOnFire(12);
				}
			}
		}
	}
}
