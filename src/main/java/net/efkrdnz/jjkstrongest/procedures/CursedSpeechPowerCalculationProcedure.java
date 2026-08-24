package net.efkrdnz.jjkstrongest.procedures;

import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.Minecraft;

public class CursedSpeechPowerCalculationProcedure {
	public static double execute(Entity caster, Entity target) {
		if (caster == null || target == null)
			return 0;
		Entity cstr = null;
		Entity trgt = null;
		double targetPower = 0;
		double casterPower = 0;
		double health = 0;
		double casterSize = 0;
		double targetSize = 0;
		cstr = caster;
		trgt = target;
		if (cstr == null || trgt == null) {
			return 1;
		}
		casterPower = 1;
		targetPower = 1;
		if (cstr instanceof Player) {
			casterPower = casterPower + 20;
			if (new Object() {
				public boolean checkGamemode(Entity _ent) {
					if (_ent instanceof ServerPlayer _serverPlayer) {
						return _serverPlayer.gameMode.getGameModeForPlayer() == GameType.CREATIVE;
					} else if (_ent.level().isClientSide() && _ent instanceof Player _player) {
						return Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()) != null
								&& Minecraft.getInstance().getConnection().getPlayerInfo(_player.getGameProfile().getId()).getGameMode() == GameType.CREATIVE;
					}
					return false;
				}
			}.checkGamemode(cstr)) {
				casterPower = casterPower + 10000;
			}
		} else if (cstr instanceof Monster) {
			casterPower = casterPower + 10;
		} else if (cstr instanceof Animal) {
			casterPower = casterPower + 0;
		} else {
			casterPower = casterPower + 5;
		}
		if (cstr instanceof LivingEntity) {
			health = cstr instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1;
			casterPower = casterPower + (health / 10) * 5;
		}
		casterSize = cstr.getBbHeight() * cstr.getBbWidth();
		casterPower = casterPower + casterSize * 10;
		if (trgt instanceof Player) {
			targetPower = targetPower + 20;
		} else if (trgt instanceof Monster) {
			targetPower = targetPower + 10;
		} else if (trgt instanceof Animal) {
			targetPower = targetPower + 0;
		} else {
			targetPower = targetPower + 5;
		}
		if (trgt instanceof LivingEntity) {
			health = trgt instanceof LivingEntity _livEnt ? _livEnt.getHealth() : -1;
			targetPower = targetPower + (health / 10) * 5;
		}
		targetSize = trgt.getBbHeight() * trgt.getBbWidth();
		targetPower = targetPower + targetSize * 10;
		if ((trgt instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) > (cstr instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)) {
			targetPower = targetPower + ((trgt instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / (cstr instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)) * 10;
		} else if ((cstr instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) > (trgt instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)) {
			targetPower = targetPower + ((cstr instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1) / (trgt instanceof LivingEntity _livEnt ? _livEnt.getMaxHealth() : -1)) * 10;
		}
		if (casterPower == 0) {
			return 999;
		}
		return targetPower / casterPower;
	}
}
