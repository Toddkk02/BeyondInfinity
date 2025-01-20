package net.todd.beyondinfinity.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.todd.beyondinfinity.BeyondInfinity;

public class ElectrolysisGeneratorScreen extends AbstractContainerScreen<ElectrolysisGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/gui/electrolysis_generator_gui.png");

    public ElectrolysisGeneratorScreen(ElectrolysisGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Disegna il background principale
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Disegna la progress bar dell'energia
        renderEnergyBar(guiGraphics, x, y);

        // Disegna la progress bar del processo se c'è un processo in corso
        if(menu.isCrafting()) {
            guiGraphics.blit(TEXTURE,
                    x + 8, y + 62,  // Posizione modificata
                    176, 0,          // UV nella texture
                    menu.getScaledProgress(), 17); // Dimensioni
        }

        // Disegna il testo dell'energia
        String energyText = menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE";
        int energyTextWidth = this.font.width(energyText);
        guiGraphics.drawString(this.font, energyText,
                x + (imageWidth - energyTextWidth) / 2,
                y + 10,
                4210752,
                false);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energyBarHeight = getScaledEnergy();

        // Background dell'energia (vuoto)
        guiGraphics.blit(TEXTURE,
                x + 156, y + 13,     // Posizione
                176, 31,            // UV nella texture
                8, 50);            // Dimensioni

        // Barra dell'energia (piena)
        if(energyBarHeight > 0) {
            guiGraphics.blit(TEXTURE,
                    x + 156,
                    y + 13 + (50 - energyBarHeight),
                    184, 31,
                    8,
                    energyBarHeight);
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()) {
            int progress = menu.getScaledProgress();
            guiGraphics.blit(TEXTURE,
                    x + 73, y + 35,  // Posizione
                    176, 0,          // UV nella texture
                    progress + 1, 17); // Dimensioni
        }
    }

    private int getScaledEnergy() {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();
        int barHeight = 50; // Altezza totale della barra

        return maxEnergy != 0 ? (energy * barHeight) / maxEnergy : 0;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Titoli standard
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // Tooltip energia
        if(isMouseAboveArea(mouseX, mouseY, x, y, 156, 13, 8, 50)) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal(menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE"),
                    mouseX - x, mouseY - y);
        }

        // Tooltip progresso
        if(isMouseAboveArea(mouseX, mouseY, x, y, 73, 35, 24, 17)) {
            int progress = (menu.getProgress() * 100) / menu.getMaxProgress();
            guiGraphics.renderTooltip(this.font,
                    Component.literal("Progress: " + progress + "%"),
                    mouseX - x, mouseY - y);
        }
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return pMouseX >= x + offsetX && pMouseX <= x + offsetX + width &&
                pMouseY >= y + offsetY && pMouseY <= y + offsetY + height;
    }
}
