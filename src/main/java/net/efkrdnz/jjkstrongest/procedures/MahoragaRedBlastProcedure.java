package net.efkrdnz.jjkstrongest.procedures;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;

import net.efkrdnz.jjkstrongest.entity.MahoragaEntity;

public class MahoragaRedBlastProcedure {
	public static void execute(ServerLevel level, Entity entity, LivingEntity target) {
		if (level == null || entity == null || target == null)
			return;
		int t = entity.getPersistentData().getInt("maho_t");
		t++;
		entity.getPersistentData().putInt("maho_t", t);
		MahoragaFaceTargetProcedure.execute(entity, target);
		if (t == 1 && entity instanceof MahoragaEntity m) {
			m.setAnimation("attack_normal"); // EXISTS
			entity.getPersistentData().putInt("maho_anim_clear", 22);
		}
		// simple "blast" placeholder (you can replace with your Red shader projectile later)
		if (t == 8) {
			target.hurt(level.damageSources().mobAttack(entity instanceof LivingEntity le ? le : null), 18.0f);
			target.push(0, 0.9, 0);
			entity.getPersistentData().putInt("maho_cd_red", 100);
			entity.getPersistentData().putInt("maho_cd_global", 10);
		}
		if (t >= 22) {
			entity.getPersistentData().putString("maho_state", "TARGETING");
			entity.getPersistentData().putInt("maho_t", 0);
		}
	}
}
