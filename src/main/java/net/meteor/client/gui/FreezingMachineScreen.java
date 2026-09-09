package net.meteor.client.gui;

import net.meteor.common.MeteorsMod;
import net.meteor.common.menu.FreezingMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class FreezingMachineScreen extends AbstractContainerScreen<FreezingMachineMenu> {

	private static final Identifier BG = MeteorsMod.id("textures/gui/freezing_machine.png");

	public FreezingMachineScreen(FreezingMachineMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}

	@Override
	protected void init() {
		super.init();
		this.titleLabelX = 8;
		this.inventoryLabelY = this.imageHeight - 94;
	}

	@Override
	protected void renderBg(GuiGraphics gui, float partialTicks, int mouseX, int mouseY) {
		gui.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);

		// Relleno de agua del tanque (zona 13,7 de 20x69), de abajo hacia arriba
		int wh = this.menu.getWaterScaled(67);
		if (wh > 0) {
			int x = this.leftPos + 14;
			int yBottom = this.topPos + 7 + 68;
			gui.fill(x, yBottom - wh, x + 18, yBottom, 0xFF3F76E4);
		}
		// Marco/cristal del tanque por encima
		gui.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos + 13, this.topPos + 7, 176, 31, 20, 69, 256, 256);

		// Indicador de frío (vertical) entre entrada y refrigerante
		int c = this.menu.getCoolScaled(12);
		gui.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos + 73, this.topPos + 36 + 12 - c, 176, 12 - c, 14, c + 2, 256, 256);
		// Flecha de progreso hacia la salida
		int p = this.menu.getProgressScaled(24);
		gui.blit(RenderPipelines.GUI_TEXTURED, BG, this.leftPos + 96, this.topPos + 34, 176, 14, p + 1, 16, 256, 256);
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.render(gui, mouseX, mouseY, partialTick);
		this.renderTooltip(gui, mouseX, mouseY);
	}
}
