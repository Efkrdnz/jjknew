package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;

public class SukunaMarkSlashBarrageProcedure {
	public static void execute(LevelAccessor world, Player player, Entity target) {
		if (player == null || target == null || !(world instanceof Level))
			return;
		// Initialize barrage on target
		CompoundTag data = target.getPersistentData();
		data.putString("mark_barrage_attacker", player.getStringUUID());
		data.putInt("mark_barrage_count", 0);
		data.putInt("mark_barrage_max", 12);
		data.putInt("mark_barrage_timer", 0);
		data.putInt("mark_barrage_interval", 3); // 3 ticks between slashes
		data.putDouble("mark_barrage_output", 1 + ReturnOutputDismantleProcedure.execute(world, player));
		data.putBoolean("mark_barrage_active", true);
	}
}
