package net.todd.beyondinfinity.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.todd.beyondinfinity.BeyondInfinity;
import org.joml.Matrix4f;

public class MoonSkyRenderer extends DimensionSpecialEffects {
    private static final ResourceLocation EARTH_TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/environment/earth.png");
    private static final ResourceLocation STARS_TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/environment/stars.png");
    private static final ResourceLocation SUN_TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/environment/white_sun.png");
    private static final ResourceLocation MILKYWAY_TEXTURE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/environment/milkyway.png");

    public MoonSkyRenderer() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        return color.multiply(0.15F, 0.15F, 0.15F);
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Renderizza lo sfondo nero dello spazio
        renderBlackBackground(poseStack);

        // Renderizza le stelle fisse
        renderStars(level, partialTick, poseStack);

        // Renderizza la Via Lattea
        renderMilkyWay(level, partialTick, poseStack);

        // Renderizza la Terra
        renderEarth(level, partialTick, poseStack);

        // Renderizza il Sole bianco
        renderWhiteSun(level, partialTick, poseStack);

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();

        return true;
    }

    private void renderBlackBackground(PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(-100.0D, -100.0D, -100.0D).endVertex();
        bufferbuilder.vertex(-100.0D, 100.0D, -100.0D).endVertex();
        bufferbuilder.vertex(100.0D, 100.0D, -100.0D).endVertex();
        bufferbuilder.vertex(100.0D, -100.0D, -100.0D).endVertex();

        Tesselator.getInstance().end();
    }

    private void renderStars(ClientLevel level, float partialTick, PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, STARS_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));

        float scale = 100.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        renderCelestialPlane(bufferbuilder, scale);
        Tesselator.getInstance().end();

        poseStack.popPose();
    }

    private void renderMilkyWay(ClientLevel level, float partialTick, PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MILKYWAY_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));

        float scale = 150.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        renderCelestialPlane(bufferbuilder, scale);
        Tesselator.getInstance().end();

        poseStack.popPose();
    }

    private void renderEarth(ClientLevel level, float partialTick, PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float earthRotation = (level.getDayTime() + partialTick) * 0.1F;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(earthRotation));
        poseStack.translate(50.0D, 20.0D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-23.5F)); // Inclinazione asse terrestre

        float scale = 30.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        renderCelestialBody(bufferbuilder, scale);
        Tesselator.getInstance().end();

        poseStack.popPose();
    }

    private void renderWhiteSun(ClientLevel level, float partialTick, PoseStack poseStack) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        float celestialAngle = level.getTimeOfDay(partialTick);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(celestialAngle * 360.0F));
        poseStack.translate(100.0D, 0.0D, 0.0D);

        float scale = 20.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        renderCelestialBody(bufferbuilder, scale);
        Tesselator.getInstance().end();

        poseStack.popPose();
    }

    private void renderCelestialPlane(BufferBuilder buffer, float size) {
        buffer.vertex(-size, 0, -size).uv(0.0F, 0.0F).endVertex();
        buffer.vertex(size, 0, -size).uv(1.0F, 0.0F).endVertex();
        buffer.vertex(size, 0, size).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(-size, 0, size).uv(0.0F, 1.0F).endVertex();
    }

    private void renderCelestialBody(BufferBuilder buffer, float size) {
        buffer.vertex(-size, -size, 0).uv(0.0F, 0.0F).endVertex();
        buffer.vertex(size, -size, 0).uv(1.0F, 0.0F).endVertex();
        buffer.vertex(size, size, 0).uv(1.0F, 1.0F).endVertex();
        buffer.vertex(-size, size, 0).uv(0.0F, 1.0F).endVertex();
    }
}