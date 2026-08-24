package net.mcreator.jjkstrongest.client.gui;

import org.checkerframework.checker.units.qual.min;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.GuiGraphics;

import net.mcreator.jjkstrongest.world.inventory.ArmAnimationEditorMenu;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class ArmAnimationEditorScreen extends AbstractContainerScreen<ArmAnimationEditorMenu> {
	private final static HashMap<String, Object> guistate = ArmAnimationEditorMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean editingLeftArm = false; // false = right arm, true = left arm

	public ArmAnimationEditorScreen(ArmAnimationEditorMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 320;
		this.imageHeight = 240;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	public void containerTick() {
		super.containerTick();
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		String armText = editingLeftArm ? "Left Arm" : "Right Arm";
		guiGraphics.drawString(this.font, Component.literal("Arm Editor - " + armText), 10, 5, -12829636, false);
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public void init() {
		super.init();
		
		int leftMargin = 10;
		int startY = this.topPos + 40;
		int spacing = 30;
		int sliderWidth = 200;
		
		// arm toggle buttons
		this.addRenderableWidget(Button.builder(Component.literal("Right Arm"), 
			button -> {
				editingLeftArm = false;
				this.minecraft.setScreen(this); // refresh screen
			})
			.bounds(leftMargin, this.topPos + 20, 95, 20).build());
		
		this.addRenderableWidget(Button.builder(Component.literal("Left Arm"), 
			button -> {
				editingLeftArm = true;
				this.minecraft.setScreen(this); // refresh screen
			})
			.bounds(leftMargin + 105, this.topPos + 20, 95, 20).build());
		
		// get the appropriate NBT keys based on which arm is being edited
		String prefix = editingLeftArm ? "left_arm_" : "arm_";
		
		// translate x slider
		this.addRenderableWidget(new ArmSlider(leftMargin, startY, sliderWidth, 20, 
			Component.literal("Translate X"), 
			entity.getPersistentData().getDouble(prefix + "translate_x"), 
			-1.0, 1.0, prefix + "translate_x", entity));
		
		// translate y slider
		this.addRenderableWidget(new ArmSlider(leftMargin, startY + spacing, sliderWidth, 20, 
			Component.literal("Translate Y"), 
			entity.getPersistentData().getDouble(prefix + "translate_y"), 
			-1.0, 1.0, prefix + "translate_y", entity));
		
		// translate z slider
		this.addRenderableWidget(new ArmSlider(leftMargin, startY + spacing * 2, sliderWidth, 20, 
			Component.literal("Translate Z"), 
			entity.getPersistentData().getDouble(prefix + "translate_z"), 
			-1.0, 1.0, prefix + "translate_z", entity));
		
		// rotate x slider
		this.addRenderableWidget(new ArmSlider(leftMargin, startY + spacing * 3, sliderWidth, 20, 
			Component.literal("Rotate X"), 
			entity.getPersistentData().getDouble(prefix + "rotate_x"), 
			-90.0, 90.0, prefix + "rotate_x", entity));
		
		// rotate y slider
		this.addRenderableWidget(new ArmSlider(leftMargin, startY + spacing * 4, sliderWidth, 20, 
			Component.literal("Rotate Y"), 
			entity.getPersistentData().getDouble(prefix + "rotate_y"), 
			-90.0, 90.0, prefix + "rotate_y", entity));
		
		// rotate z slider
		this.addRenderableWidget(new ArmSlider(leftMargin, startY + spacing * 5, sliderWidth, 20, 
			Component.literal("Rotate Z"), 
			entity.getPersistentData().getDouble(prefix + "rotate_z"), 
			0.0, 90.0, prefix + "rotate_z", entity));
	}

	// custom slider with proper drag support
	private static class ArmSlider extends AbstractSliderButton {
		private final String nbtKey;
		private final double min;
		private final double max;
		private final Component label;
		private final Player player;

		public ArmSlider(int x, int y, int width, int height, Component label, double value, double min, double max, String nbtKey, Player player) {
			super(x, y, width, height, Component.empty(), (value - min) / (max - min));
			this.label = label;
			this.min = min;
			this.max = max;
			this.nbtKey = nbtKey;
			this.player = player;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			double current = min + (max - min) * this.value;
			this.setMessage(Component.literal(label.getString() + ": " + String.format("%.2f", current)));
		}

		@Override
		protected void applyValue() {
			if (player != null) {
				double current = min + (max - min) * this.value;
				player.getPersistentData().putDouble(nbtKey, current);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			this.setValueFromMouse(mouseX);
		}

		@Override
		protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
			this.setValueFromMouse(mouseX);
			super.onDrag(mouseX, mouseY, dragX, dragY);
		}

		private void setValueFromMouse(double mouseX) {
			this.value = (mouseX - (double) (this.getX() + 4)) / (double) (this.width - 8);
			this.value = Math.max(0.0, Math.min(1.0, this.value));
			this.updateMessage();
			this.applyValue();
		}
	}
}