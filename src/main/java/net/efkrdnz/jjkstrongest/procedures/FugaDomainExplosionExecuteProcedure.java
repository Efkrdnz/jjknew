package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

import net.efkrdnz.jjkstrongest.init.JjkStrongestModEntities;
import net.efkrdnz.jjkstrongest.entity.MalevolentShrineEntity;

public class FugaDomainExplosionExecuteProcedure {
	// triggers fuga inside the owner's malevolent shrine
	public static void execute(LevelAccessor world, Entity entity) {
		if (!(world instanceof ServerLevel serverLevel))
			return;
		if (entity == null)
			return;
		String playerUUID = entity.getStringUUID();
		AABB searchBox = AABB.ofSize(entity.position(), 260, 260, 260);
		for (MalevolentShrineEntity shrineEntity : serverLevel.getEntitiesOfClass(MalevolentShrineEntity.class, searchBox, e -> true)) {
			CompoundTag data = shrineEntity.getPersistentData();
			if (!data.contains("ownerUUID"))
				continue;
			if (!playerUUID.equals(data.getString("ownerUUID")))
				continue;
			if (data.getBoolean("fugaTriggered"))
				return;
			data.putBoolean("fugaTriggered", true);
			executeFugaExplosion(serverLevel, shrineEntity, entity);
			data.putInt("domainLifetimeTicks", 600); // collapses next tick
			return;
		}
	}

	// spawns shader fx + applies 100 block radius damage
	private static void executeFugaExplosion(ServerLevel world, MalevolentShrineEntity shrine, Entity caster) {
		Vec3 center = shrine.position();
		double radius = 100.0;
		double radiusSq = radius * radius;
		// shader fx entity (re-using flame_arrow_explosion)
		Entity fx = JjkStrongestModEntities.FLAME_ARROW_EXPLOSION.get().create(world, null, BlockPos.containing(center.x, center.y, center.z), MobSpawnType.MOB_SUMMONED, false, false);
		if (fx != null) {
			if (fx instanceof TamableAnimal _toTame && caster instanceof Player _owner)
				_toTame.tame(_owner);
			fx.getPersistentData().putDouble("life", 0);
			fx.getPersistentData().putDouble("fugaScale", 2.5); // optional renderer patch below uses this
			world.addFreshEntity(fx);
		}
		DamageSource jujutsuDamage = new DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("jjk_strongest:technique_fuga"))), caster);
		double minY = center.y - 30;
		double maxY = center.y + 80;
		AABB box = new AABB(center.x - radius, minY, center.z - radius, center.x + radius, maxY, center.z + radius);
		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
			if (target == caster)
				continue;
			double dx = target.getX() - center.x;
			double dz = target.getZ() - center.z;
			double distSq = dx * dx + dz * dz;
			if (distSq > radiusSq)
				continue;
			double dist = Math.sqrt(distSq);
			float damage = (float) (120.0 - (dist / radius) * 80.0); // 120 center -> 40 edge
			if (damage < 40.0f)
				damage = 40.0f;
			target.hurt(jujutsuDamage, damage);
			target.igniteForSeconds(8);
			target.setDeltaMovement(target.getDeltaMovement().add(0, 1.2, 0));
			target.hurtMarked = true;
		}
		world.playSound(null, center.x, center.y, center.z, net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 10.0f, 0.4f);
	}
}
