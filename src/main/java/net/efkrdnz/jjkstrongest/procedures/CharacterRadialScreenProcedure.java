package net.efkrdnz.jjkstrongest.procedures;

import net.neoforged.neoforge.network.PacketDistributor;

import org.joml.Matrix4f;


import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import net.efkrdnz.jjkstrongest.network.JjkStrongestModVariables;
import net.efkrdnz.jjkstrongest.network.AbilitymenuMessage;
import net.efkrdnz.jjkstrongest.JjkStrongestMod;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.systems.RenderSystem;

public class CharacterRadialScreenProcedure {
	public static class CharacterRadialScreen extends Screen {
		private static final int RADIUS_IN = 40;
		private static final int RADIUS_OUT = 110;
		private static final int CENTER_CIRCLE_RADIUS = 30;
		private static final int OUTLINE_WIDTH = 3;
		private List<AbilityItem> items = new ArrayList<>();
		private int hovered = -1;
		private String character;
		private long openTime;
		private boolean rtcUnlocked = false;
		private boolean BW_WCS = false;

		public CharacterRadialScreen(String character) {
			super(Component.literal(""));
			this.minecraft = Minecraft.getInstance();
			this.character = character;
			this.openTime = System.currentTimeMillis();
		}

		@Override
		protected void init() {
			super.init();
			items.clear();
			if (this.minecraft == null || this.minecraft.player == null)
				return;
			Player player = this.minecraft.player;
			loadAbilitiesForCharacter(player);
		}

		private void loadAbilitiesForCharacter(Player player) {
			if (character.equals("gojo")) {
				loadGojoAbilities(player);
			} else if (character.equals("sukuna")) {
				loadSukunaAbilities(player);
			} else if (character.equals("yuji")) {
				loadYujiAbilities(player);
			} else if (character.equals("inumaki")) {
				loadInumakiAbilities(player);
			}
		}

		private void loadGojoAbilities(Player player) {
			AtomicBoolean rtcUnlockedTemp = new AtomicBoolean(false);
			{
				JjkStrongestModVariables.PlayerVariables capability = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				rtcUnlockedTemp.set(capability.RTC_unlocked);
			}
			this.rtcUnlocked = rtcUnlockedTemp.get();
			// debug: print to console
			if (this.rtcUnlocked) {
				items.add(new AbilityItem("Purple", 0x9933FF, 3, "gojo_purple"));
			}
			items.add(new AbilityItem("Blue", 0x00BFFF, 0, "gojo_blue"));
			items.add(new AbilityItem("Generic", 0xFFFFFF, 11, "all_generic"));
			items.add(new AbilityItem("Melee", 0xFFAA00, 4, "gojo_melee"));
			items.add(new AbilityItem("Limitless", 0xFFFFFF, 1, "gojo_limitless"));
			if (this.rtcUnlocked) {
				items.add(new AbilityItem("RCT", 0x32A848, 21, "reverse_cursed_technique"));
				items.add(new AbilityItem("Red", 0xFF4444, 2, "gojo_red"));
			}
		}

		private void loadSukunaAbilities(Player player) {
			AtomicBoolean BW_WCSTemp = new AtomicBoolean(false);
			{
				JjkStrongestModVariables.PlayerVariables capability = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				BW_WCSTemp.set(capability.vow_wcsact);
			}
			this.BW_WCS = BW_WCSTemp.get();
			AtomicBoolean rtcUnlockedTemp = new AtomicBoolean(false);
			{
				JjkStrongestModVariables.PlayerVariables capability = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				rtcUnlockedTemp.set(capability.RTC_unlocked);
			}
			this.rtcUnlocked = rtcUnlockedTemp.get();
			items.add(new AbilityItem("Dismantle", 0xCC0000, 6, "sukuna_dismantle"));
			items.add(new AbilityItem("Cleave", 0xFF0000, 5, "sukuna_cleave"));
			items.add(new AbilityItem("Generic", 0xFFFFFF, 11, "all_generic"));
			items.add(new AbilityItem("Shrine", 0xFFFFFF, 9, "sukuna_shrine"));
			if (this.rtcUnlocked) {
				items.add(new AbilityItem("RCT", 0x32A848, 21, "reverse_cursed_technique"));
			}
			items.add(new AbilityItem("Melee", 0xFFAA00, 10, "sukuna_melee"));
			items.add(new AbilityItem("Fuga", 0xFF6600, 7, "sukuna_fuga"));
			if (this.BW_WCS) {
				items.add(new AbilityItem("World Slash", 0xDD0000, 8, "sukuna_wcs"));
			}
		}

		private void loadYujiAbilities(Player player) {
			AtomicBoolean rtcUnlockedTemp = new AtomicBoolean(false);
			{
				JjkStrongestModVariables.PlayerVariables capability = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				rtcUnlockedTemp.set(capability.RTC_unlocked);
			}
			this.rtcUnlocked = rtcUnlockedTemp.get();
			items.add(new AbilityItem("Blood Manipulation", 0x6E160F, 12, "yuji_bloodmanipulation"));
			items.add(new AbilityItem("Shrine", 0xF25207, 13, "yuji_shrine"));
			items.add(new AbilityItem("Generic", 0xFFFFFF, 11, "all_generic"));
			if (this.rtcUnlocked) {
				items.add(new AbilityItem("RCT", 0x32A848, 21, "reverse_cursed_technique"));
			}
			items.add(new AbilityItem("Melee", 0xFFAA00, 15, "yuji_melee"));
			items.add(new AbilityItem("Divergent\nFist", 0x0B93BD, 14, "yuji_divergentfist"));
		}

		private void loadInumakiAbilities(Player player) {
			AtomicBoolean rtcUnlockedTemp = new AtomicBoolean(false);
			{
				JjkStrongestModVariables.PlayerVariables capability = player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				rtcUnlockedTemp.set(capability.RTC_unlocked);
			}
			this.rtcUnlocked = rtcUnlockedTemp.get();
			items.add(new AbilityItem("Assault", 0xCC00CC, 16, "inumaki_assault"));
			items.add(new AbilityItem("Control", 0x9933FF, 17, "inumaki_control"));
			items.add(new AbilityItem("General", 0xFFFFFF, 11, "all_generic"));
			if (this.rtcUnlocked) {
				items.add(new AbilityItem("RCT", 0x32A848, 21, "reverse_cursed_technique"));
			}
			items.add(new AbilityItem("Melee", 0xFFAA00, 20, "inumaki_melee"));
			items.add(new AbilityItem("Binding", 0x6600CC, 18, "inumaki_binding"));
			items.add(new AbilityItem("Utility", 0xCC66FF, 19, "inumaki_utility"));
		}

		public void selectAndClose() {
			if (this.hovered >= 0 && this.hovered < this.items.size()) {
				AbilityItem item = this.items.get(this.hovered);
				PacketDistributor.sendToServer(new AbilitymenuMessage(item.id, 0));
			}
		}

		@Override
		public boolean isPauseScreen() {
			return false;
		}

		@Override
		public boolean shouldCloseOnEsc() {
			return true;
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (keyCode == 256) {
				this.onClose();
				return true;
			}
			return false;
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (button == 0) {
				selectAndClose();
				Minecraft.getInstance().setScreen(null);
				return true;
			}
			return false;
		}

		/**
		 * 1.21 made Screen#renderBackground run a gaussian blur over the world
		 * behind the screen. This radial menu is an in-world overlay that must
		 * leave the scene crisp, so the background pass is a no-op.
		 */
		@Override
		public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		}

		@Override
		public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			int centerX = this.width / 2;
			int centerY = this.height / 2;
			updateHover(mouseX, mouseY, centerX, centerY);
			long currentTime = System.currentTimeMillis();
			float timeSinceOpen = (currentTime - openTime) / 1000.0F;
			float pulse = (float) (Math.sin(timeSinceOpen * 3.0) * 0.15 + 0.85);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			guiGraphics.pose().pushPose();
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder buffer;
			Matrix4f matrix = guiGraphics.pose().last().pose();
			// center polygon with pulse
			buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
			int centerColor = toRGB24(20, 20, 20, (int) (180 * pulse));
			int cr = (centerColor >> 16) & 0xFF;
			int cg = (centerColor >> 8) & 0xFF;
			int cb = centerColor & 0xFF;
			int ca = (centerColor >> 24) & 0xFF;
			buffer.addVertex(matrix, centerX, centerY, 0.0F).setColor(cr, cg, cb, ca);
			int sides = Math.max(4, items.size());
			for (int i = 0; i <= sides; i++) {
				float angle = (float) ((i * Mth.TWO_PI / sides) - (Math.PI / 2));
				float x = centerX + CENTER_CIRCLE_RADIUS * (float) Math.cos(angle);
				float y = centerY + CENTER_CIRCLE_RADIUS * (float) Math.sin(angle);
				buffer.addVertex(matrix, x, y, 0.0F).setColor(cr, cg, cb, ca);
			}
			{
				MeshData mesh = buffer.build();
				if (mesh != null)
					BufferUploader.drawWithShader(mesh);
			}
			// main slots
			buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			for (int i = 0; i < items.size(); i++) {
				float startAngle = getAngleFor(i - 0.5F);
				float endAngle = getAngleFor(i + 0.5F);
				AbilityItem item = items.get(i);
				boolean isActive = isAbilityActive(item.key);
				int fillColor;
				if (this.hovered == i) {
					int hoverColor = item.color;
					int hr = (hoverColor >> 16) & 0xFF;
					int hg = (hoverColor >> 8) & 0xFF;
					int hb = hoverColor & 0xFF;
					fillColor = toRGB24(hr, hg, hb, 150);
				} else if (isActive) {
					fillColor = toRGB24(255, 255, 255, 120);
				} else {
					fillColor = toRGB24(0, 0, 0, 150);
				}
				drawSlot(guiGraphics.pose(), buffer, centerX, centerY, startAngle, endAngle, fillColor);
			}
			{
				MeshData mesh = buffer.build();
				if (mesh != null)
					BufferUploader.drawWithShader(mesh);
			}
			// colored outlines
			buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			for (int i = 0; i < items.size(); i++) {
				float startAngle = getAngleFor(i - 0.5F);
				float endAngle = getAngleFor(i + 0.5F);
				AbilityItem item = items.get(i);
				int abilityColor = item.color;
				int alpha = this.hovered == i ? 255 : 180;
				int outlineColor = toRGB24((abilityColor >> 16) & 0xFF, (abilityColor >> 8) & 0xFF, abilityColor & 0xFF, alpha);
				drawOutline(guiGraphics.pose(), buffer, centerX, centerY, startAngle, endAngle, outlineColor);
			}
			{
				MeshData mesh = buffer.build();
				if (mesh != null)
					BufferUploader.drawWithShader(mesh);
			}
			// inner glow for hovered
			if (this.hovered >= 0 && this.hovered < items.size()) {
				buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
				float startAngle = getAngleFor(this.hovered - 0.5F);
				float endAngle = getAngleFor(this.hovered + 0.5F);
				AbilityItem hoveredItem = items.get(this.hovered);
				int glowColor = hoveredItem.color;
				int gr = (glowColor >> 16) & 0xFF;
				int gg = (glowColor >> 8) & 0xFF;
				int gb = glowColor & 0xFF;
				int innerGlow = toRGB24(gr, gg, gb, 100);
				drawSlot(guiGraphics.pose(), buffer, centerX, centerY, startAngle, endAngle, innerGlow);
				{
					MeshData mesh = buffer.build();
					if (mesh != null)
						BufferUploader.drawWithShader(mesh);
				}
			}
			guiGraphics.pose().popPose();
			RenderSystem.disableBlend();
			// text with shadow - conditional positioning based on rtc unlock
			float radius = (RADIUS_IN + RADIUS_OUT) / 2.0F - 10;
			if (character.equals("gojo") && !this.rtcUnlocked) {
				radius = (RADIUS_IN + RADIUS_OUT) / 2.0F - 20;
			}
			for (int i = 0; i < items.size(); i++) {
				float start = getAngleFor(i - 0.5F);
				float end = getAngleFor(i + 0.5F);
				float middle = (start + end) / 2.0F;
				int posX = (int) (centerX + radius * Math.cos(middle));
				int posY = (int) (centerY + radius * Math.sin(middle));
				AbilityItem item = items.get(i);
				int textColor = this.hovered == i ? 0xFFFFFF : item.color;
				// special positioning for divergent fist
				float adjustedRadius = radius;
				if (item.name.contains("Divergent")) {
					adjustedRadius = radius - 8; // move 8 pixels closer to center
					posX = (int) (centerX + adjustedRadius * Math.cos(middle));
					posY = (int) (centerY + adjustedRadius * Math.sin(middle));
				}
				// handle multi-line text
				if (item.name.contains("\n")) {
					String[] lines = item.name.split("\n");
					int totalHeight = lines.length * this.font.lineHeight;
					int startY = posY - totalHeight / 2;
					for (int lineIdx = 0; lineIdx < lines.length; lineIdx++) {
						int textWidth = this.font.width(lines[lineIdx]);
						int lineY = startY + lineIdx * this.font.lineHeight;
						guiGraphics.drawString(this.font, lines[lineIdx], posX - textWidth / 2, lineY, textColor, true);
					}
				} else {
					int textWidth = this.font.width(item.name);
					guiGraphics.drawString(this.font, item.name, posX - textWidth / 2, posY - this.font.lineHeight / 2, textColor, true);
				}
			}
		}

		private void updateHover(int mouseX, int mouseY, int centerX, int centerY) {
			double mouseAngle = Math.atan2(mouseY - centerY, mouseX - centerX);
			double mousePos = Math.sqrt(Math.pow(mouseX - centerX, 2.0D) + Math.pow(mouseY - centerY, 2.0D));
			if (!items.isEmpty()) {
				float startAngle = getAngleFor(-0.5F);
				float endAngle = getAngleFor(items.size() - 0.5F);
				while (mouseAngle < startAngle)
					mouseAngle += Mth.TWO_PI;
				while (mouseAngle >= endAngle)
					mouseAngle -= Mth.TWO_PI;
				this.hovered = -1;
				for (int i = 0; i < items.size(); i++) {
					float currentStart = getAngleFor(i - 0.5F);
					float currentEnd = getAngleFor(i + 0.5F);
					if (mouseAngle >= currentStart && mouseAngle < currentEnd && mousePos >= RADIUS_IN && mousePos < RADIUS_OUT) {
						this.hovered = i;
						break;
					}
				}
			}
		}

		private void drawSlot(PoseStack poseStack, BufferBuilder buffer, float centerX, float centerY, float startAngle, float endAngle, int color) {
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			int a = (color >> 24) & 0xFF;
			Matrix4f matrix = poseStack.last().pose();
			float x1 = centerX + RADIUS_IN * (float) Math.cos(startAngle);
			float y1 = centerY + RADIUS_IN * (float) Math.sin(startAngle);
			float x2 = centerX + RADIUS_OUT * (float) Math.cos(startAngle);
			float y2 = centerY + RADIUS_OUT * (float) Math.sin(startAngle);
			float x3 = centerX + RADIUS_OUT * (float) Math.cos(endAngle);
			float y3 = centerY + RADIUS_OUT * (float) Math.sin(endAngle);
			float x4 = centerX + RADIUS_IN * (float) Math.cos(endAngle);
			float y4 = centerY + RADIUS_IN * (float) Math.sin(endAngle);
			buffer.addVertex(matrix, x2, y2, 0.0F).setColor(r, g, b, a);
			buffer.addVertex(matrix, x1, y1, 0.0F).setColor(r, g, b, a);
			buffer.addVertex(matrix, x4, y4, 0.0F).setColor(r, g, b, a);
			buffer.addVertex(matrix, x3, y3, 0.0F).setColor(r, g, b, a);
		}

		private void drawOutline(PoseStack poseStack, BufferBuilder buffer, float centerX, float centerY, float startAngle, float endAngle, int color) {
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			int a = (color >> 24) & 0xFF;
			Matrix4f matrix = poseStack.last().pose();
			float x1 = centerX + RADIUS_OUT * (float) Math.cos(startAngle);
			float y1 = centerY + RADIUS_OUT * (float) Math.sin(startAngle);
			float x2 = centerX + (RADIUS_OUT + OUTLINE_WIDTH) * (float) Math.cos(startAngle);
			float y2 = centerY + (RADIUS_OUT + OUTLINE_WIDTH) * (float) Math.sin(startAngle);
			float x3 = centerX + (RADIUS_OUT + OUTLINE_WIDTH) * (float) Math.cos(endAngle);
			float y3 = centerY + (RADIUS_OUT + OUTLINE_WIDTH) * (float) Math.sin(endAngle);
			float x4 = centerX + RADIUS_OUT * (float) Math.cos(endAngle);
			float y4 = centerY + RADIUS_OUT * (float) Math.sin(endAngle);
			buffer.addVertex(matrix, x2, y2, 0.0F).setColor(r, g, b, a);
			buffer.addVertex(matrix, x1, y1, 0.0F).setColor(r, g, b, a);
			buffer.addVertex(matrix, x4, y4, 0.0F).setColor(r, g, b, a);
			buffer.addVertex(matrix, x3, y3, 0.0F).setColor(r, g, b, a);
		}

		private float getAngleFor(double i) {
			if (items.isEmpty())
				return 0;
			int sides = Math.max(4, items.size());
			return (float) (((i / sides) * Mth.TWO_PI) - (Math.PI / 2));
		}

		private boolean isAbilityActive(String key) {
			if (this.minecraft == null || this.minecraft.player == null)
				return false;
			AtomicBoolean active = new AtomicBoolean(false);
			{
				JjkStrongestModVariables.PlayerVariables capability = this.minecraft.player.getData(JjkStrongestModVariables.PLAYER_VARIABLES);
				active.set(capability.current_moveset.equals(key));
			}
			return active.get();
		}

		private static int toRGB24(int r, int g, int b, int a) {
			return (a << 24) | (r << 16) | (g << 8) | b;
		}
	}

	public static class AbilityItem {
		String name;
		int color;
		int id;
		String key;

		public AbilityItem(String name, int color, int id, String key) {
			this.name = name;
			this.color = color;
			this.id = id;
			this.key = key;
		}
	}
}
