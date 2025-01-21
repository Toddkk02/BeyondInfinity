package net.todd.beyondinfinity.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
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

    public MoonSkyRenderer() {
        super(Float.NaN, true, SkyType.NORMAL, false, false);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
                             Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        // Set sky color to pure black
        RenderSystem.clear(16384, true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Render multiple layers of stars with better vertical coverage
        float[][] rotations = {
                // Base horizontal coverage
                {-90.0F, -19.0F},
                {0.0F, 0.0F},
                {90.0F, 0.0F},
                {180.0F, 0.0F},
                {270.0F, 0.0F},

                // Vertical coverage
                {0.0F, 90.0F},    // Directly above
                {0.0F, -90.0F},   // Directly below
                {0.0F, 45.0F},    // Upper angles
                {90.0F, 45.0F},
                {180.0F, 45.0F},
                {270.0F, 45.0F},
                {0.0F, -45.0F},   // Lower angles
                {90.0F, -45.0F},
                {180.0F, -45.0F},
                {270.0F, -45.0F}
        };

        float starRotation = level.getTimeOfDay(partialTick) * 360.0F;

        for (float[] rotation : rotations) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation[0]));
            poseStack.mulPose(Axis.XP.rotationDegrees(starRotation));
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotation[1]));
            renderStars(poseStack);
            poseStack.popPose();
        }

        // Sun
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        float celestialAngle = level.getTimeOfDay(partialTick) * 360.0F;
        poseStack.mulPose(Axis.XP.rotationDegrees(celestialAngle));
        renderSun(poseStack);
        poseStack.popPose();

        // Earth
        poseStack.pushPose();
        assert Minecraft.getInstance().player != null;
        float earthRotation = (float) (level.getSharedSpawnPos().getZ() -
                Minecraft.getInstance().player.getZ()) * 0.01F;
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(partialTick) * 360.0F * 0.001F));
        poseStack.mulPose(Axis.XP.rotationDegrees(earthRotation + 200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderEarth(poseStack);
        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        return true;
    }

    private void renderStars(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, STARS_TEXTURE);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // Reduced size for smaller stars and better distribution
        float size = 100.0F;
        float distance = 100.0F;

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, -size, distance, -size).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix, size, distance, -size).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix, size, distance, size).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix, -size, distance, size).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    private void renderSun(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        float size = 15.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, -size, 100.0F, -size).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix, size, 100.0F, -size).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix, size, 100.0F, size).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix, -size, 100.0F, size).uv(0.0F, 1.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    private void renderEarth(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        float size = 10.0F;
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(matrix, -size, -100.0F, size).uv(0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix, size, -100.0F, size).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(matrix, size, -100.0F, -size).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(matrix, -size, -100.0F, -size).uv(0.0F, 0.0F).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float sunHeight) {
        float brightness = calculateBrightness(sunHeight);
        return color.multiply(brightness, brightness, brightness);
    }

    private float calculateBrightness(float sunHeight) {
        return 0.05F + Math.max(0, sunHeight) * 0.25F;
    }

    @Override
    public boolean isFoggyAt(int x, int y) {
        return false;
    }
}