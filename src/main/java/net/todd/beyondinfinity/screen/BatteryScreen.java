package net.todd.beyondinfinity.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.todd.beyondinfinity.BeyondInfinity;
import java.util.List;
import java.util.Optional;

public class BatteryScreen extends AbstractContainerScreen<BatteryMenu> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/gui/battery_gui.png");

    public BatteryScreen(BatteryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (isHoveringEnergyBar(mouseX, mouseY)) {
            int energy = menu.getEnergy();
            int maxEnergy = menu.getMaxEnergy();
            List<Component> tooltipList = List.of(
                    Component.literal(String.format("%,d / %,d FE", energy, maxEnergy))
            );
            guiGraphics.renderTooltip(this.font, tooltipList, Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderEnergyBar(guiGraphics, x, y);
    }

    private void renderEnergyBar(GuiGraphics guiGraphics, int x, int y) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        if (maxEnergy > 0) {
            int barHeight = 55;
            int barX = x + 82;
            int barY = y + 18;

            float energyPercent = (float) energy / maxEnergy;
            int scaledHeight = (int) (energyPercent * barHeight);

            if (scaledHeight > 0) {
                guiGraphics.blit(TEXTURE,
                        barX, barY + (barHeight - scaledHeight),
                        176, 0,
                        16, scaledHeight);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int energy = menu.getEnergy();
        int maxEnergy = menu.getMaxEnergy();

        guiGraphics.drawString(font,
                Component.literal(String.format("%,d / %,d FE", energy, maxEnergy)),
                8,
                8,
                4210752,
                false);
    }

    private boolean isHoveringEnergyBar(int mouseX, int mouseY) {
        int barX = (width - imageWidth) / 2 + 82;
        int barY = (height - imageHeight) / 2 + 18;
        return mouseX >= barX && mouseX < barX + 16 &&
                mouseY >= barY && mouseY < barY + 55;
    }
}