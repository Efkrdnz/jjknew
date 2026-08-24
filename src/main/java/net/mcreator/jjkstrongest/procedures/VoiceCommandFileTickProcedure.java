package net.mcreator.jjkstrongest.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.Entity;

import java.util.List;

import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class VoiceCommandFileTickProcedure {
	// reads documents/JJKVoiceCommands/command.txt and clears it
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null)
			return;
		if (world.isClientSide())
			return;
		// reduce file io (every 5 ticks)
		if ((entity.level().getGameTime() % 5) != 0)
			return;
		try {
			Path dir = Path.of(System.getProperty("user.home"), "Documents", "JJKVoiceCommands");
			Path file = dir.resolve("command.txt");
			Files.createDirectories(dir);
			if (!Files.exists(file)) {
				Files.writeString(file, "", StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
				return;
			}
			List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
			if (lines.isEmpty())
				return;
			String key = lines.get(0).trim();
			if (key.isEmpty())
				return;
			// clear before executing (prevents double trigger if something crashes)
			Files.writeString(file, "", StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
			// execute your existing logic
			VCTexeProcedure.execute(world, x, y, z, entity, key);
		} catch (Exception ignored) {
		}
	}
}
