package net.mcreator.jjkstrongest.procedures;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;

import net.mcreator.jjkstrongest.entity.MalevolentShrineEntity;

import javax.annotation.Nullable;

import java.util.Comparator;

@Mod.EventBusSubscriber
public class ShrineScreenshakeProcedure {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			execute(event, event.player.level(), event.player.getX(), event.player.getY(), event.player.getZ(), event.player);
		}
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		execute(null, world, x, y, z, entity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (world.getLevelData().getGameTime() % 10 == 0) {
			if (entity instanceof Player) {
				if (!world.getEntitiesOfClass(MalevolentShrineEntity.class, AABB.ofSize(new Vec3((entity.getX()), (entity.getY()), (entity.getZ())), 200, 200, 200), e -> true).isEmpty()) {
					// get nearest malevolent shrine
					Entity nearestShrine = ((Entity) world.getEntitiesOfClass(MalevolentShrineEntity.class, AABB.ofSize(new Vec3(x, y, z), 200, 200, 200), e -> true).stream().sorted(new Object() {
						Comparator<Entity> compareDistOf(double _x, double _y, double _z) {
							return Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_x, _y, _z));
						}
					}.compareDistOf(x, y, z)).findFirst().orElse(null));
					if (nearestShrine != null && nearestShrine.getPersistentData().getBoolean("active")) {
						// check if player is owner
						String ownerUUID = nearestShrine.getPersistentData().getString("ownerUUID");
						boolean isOwner = entity.getStringUUID().equals(ownerUUID);
						// apply different shake intensity
						if (isOwner) {
							TriggerScreenShakeProcedure.execute((Level) world, entity, 20, 1.0f);
						} else {
							TriggerScreenShakeProcedure.execute((Level) world, entity, 20, 4.0f);
						}
					}
				}
			}
		}
	}
}
