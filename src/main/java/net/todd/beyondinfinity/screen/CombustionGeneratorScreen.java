package net.todd.beyondinfinity.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.todd.beyondinfinity.BeyondInfinity;

public class CombustionGeneratorScreen extends AbstractContainerScreen<CombustionGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/gui/combustion_generator_gui.png");

    public CombustionGeneratorScreen(CombustionGeneratorMenu menu, Inventory inventory, Component title) {
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

        // Disegna la barra di trasferimento
        renderTransferBar(guiGraphics, x, y);

        // Disegna la progress bar del combustibile
        if(menu.hasFuel()) {
            int burnProgress = menu.getBurnProgress();
            guiGraphics.blit(TEXTURE,
                    x + 15, y + 54,  // Coordinate dove disegnare
                    176, 0,          // Coordinate UV nella texture
                    burnProgress + 1, 14); // Dimensioni
        }

        // Disegna il testo dell'energia nella barra centrale
        String energyText = menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE";
        int energyTextWidth = this.font.width(energyText);
        guiGraphics.drawString(this.font, energyText,
                x + (imageWidth - energyTextWidth) / 2,
                y + 30,
                4210752, // Colore del testo (grigio scuro)
                false);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energyBarHeight = menu.getScaledEnergy();
        // Background dell'energia (vuoto)
        guiGraphics.blit(TEXTURE,
                x + 45, y + 17,     // Posizione
                176, 31,            // UV nella texture
                85, 50);            // Dimensioni

        // Barra dell'energia (piena)
        if(energyBarHeight > 0) {
            guiGraphics.blit(TEXTURE,
                    x + 45,
                    y + 17 + (50 - energyBarHeight),
                    176, 81,
                    85,
                    energyBarHeight);
        }
    }

    private void renderTransferBar(GuiGraphics guiGraphics, int x, int y) {
        // Background della barra di trasferimento
        guiGraphics.blit(TEXTURE,
                x + 140, y + 17,    // Posizione
                192, 31,            // UV nella texture
                16, 50);            // Dimensioni

        // Barra di trasferimento (piena)
        int transferHeight = menu.getScaledTransfer();
        if(transferHeight > 0) {
            guiGraphics.blit(TEXTURE,
                    x + 140,
                    y + 17 + (50 - transferHeight),
                    208, 31,
                    16,
                    transferHeight);
        }
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

        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // Energy storage tooltip per la barra centrale
        if(isMouseAboveArea(mouseX, mouseY, x, y, 45, 17, 85, 50)) {
            guiGraphics.renderTooltip(this.font, Component.literal(
                            "Energy: " + menu.getEnergy() + " / " + menu.getMaxEnergy() + " FE"),
                    mouseX - x, mouseY - y);
        }

        // Transfer rate tooltip
        if(isMouseAboveArea(mouseX, mouseY, x, y, 140, 17, 16, 50)) {
            guiGraphics.renderTooltip(this.font, Component.literal(
                            "Velocity Of Tranfert: " + menu.getTransferRate() + " FE/t"),
                    mouseX - x, mouseY - y);
        }
    }

    private boolean isMouseAboveArea(int pMouseX, int pMouseY, int x, int y, int offsetX, int offsetY, int width, int height) {
        return pMouseX >= x + offsetX && pMouseX <= x + offsetX + width &&
                pMouseY >= y + offsetY && pMouseY <= y + offsetY + height;
    }
}