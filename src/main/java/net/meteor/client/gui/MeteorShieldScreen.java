package net.meteor.client.gui;

import net.meteor.common.MeteorsMod;
import net.meteor.common.block.MeteorShieldBlockEntity;
import net.meteor.common.menu.MeteorShieldMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class MeteorShieldScreen extends AbstractContainerScreen<MeteorShieldMenu> {

	private static final Identifier BG = MeteorsMod.id("textures/gui/meteor_shield.png");

	private Button cometButton;

	public MeteorShieldScreen(MeteorShieldMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}

	@Override
	protected void init() {
		super.init();
		this.cometButton = Button.builder(cometButtonLabel(), b -> {
			if (this.minecraft != null && this.minecraft.gameMode != null) {
				this.minecraft.gameMode.handleInventoryButtonClick(
						this.menu.containerId, MeteorShieldBlockEntity.BUTTON_TOGGLE_COMETS);
			}
		}).bounds(this.leftPos + 74, this.topPos + 43, 94, 14).build();
		this.cometButton.setTooltip(Tooltip.create(Component.translatable("gui.meteors.shield.comets.tooltip")));
		this.addRenderableWidget(this.cometButton);
	}

	private Component cometButtonLabel() {
		return Component.translatable(this.menu.getBlockComets()
				? "gui.meteors.shield.comets.blocked"
				: "gui.meteors.shield.comets.allowed");
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		if (this.cometButton != null) this.cometButton.setMessage(cometButtonLabel());
	}

	@Override
	protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
		gui.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
		// X roja sobre el slot del chip solo cuando ya está cargado (el chip no hace falta)
		if (this.menu.getCharged()) {
			gui.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos + 47, this.topPos + 60, 0, 166, 16, 16, 256, 256);
		}
	}

	/** Sin título ni etiqueta de inventario (como el original). */
	@Override
	protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.render(gui, mouseX, mouseY, partialTick);
		int x = this.leftPos + 74;
		int color = 0xFFE0E0E0;
		if (!this.menu.getCharged()) {
			gui.drawString(this.font, Component.translatable("gui.meteors.shield.charging", this.menu.getChargePercent()), x, this.topPos + 8, 0xFF66CCFF, false);
			gui.drawString(this.font, Component.translatable("gui.meteors.shield.charging.hint"), x, this.topPos + 20, color, false);
		} else {
			gui.drawString(this.font, Component.translatable("gui.meteors.shield.level", this.menu.getPowerLevel(), MeteorShieldBlockEntity.MAX_LEVEL), x, this.topPos + 8, color, false);
			gui.drawString(this.font, Component.translatable("gui.meteors.shield.range", this.menu.getRange()), x, this.topPos + 20, color, false);
		}
		gui.drawString(this.font, Component.translatable("gui.meteors.shield.owner", this.menu.getOwner()), x, this.topPos + 32, color, false);

		// Detector de cometas: coordenadas del último avistamiento en el radio.
		if (this.menu.hasComet()) {
			Component comet = Component.translatable("gui.meteors.shield.comet",
					this.menu.getCometType().getDisplayName(), this.menu.getCometX(), this.menu.getCometZ())
					.withStyle(ChatFormatting.AQUA);
			gui.drawString(this.font, comet, this.leftPos + 8, this.topPos + 74, 0xFF66CCFF, false);
		}
	}
}
