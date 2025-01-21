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
        setupFog.run();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Sfondo nero
        renderBlackBackground(poseStack);

        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        float timeOfDay = level.getTimeOfDay(partialTick);
        poseStack.mulPose(Axis.XP.rotationDegrees(timeOfDay * 360.0F));

        // Stelle
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, STARS_TEXTURE);
        renderTexturedSky(100.0F);

        // Via Lattea
        RenderSystem.setShaderTexture(0, MILKYWAY_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
        renderTexturedSky(150.0F);

        poseStack.pushPose();

        // Terra
        RenderSystem.setShaderTexture(0, EARTH_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((level.getDayTime() + partialTick) * 0.1F));
        poseStack.translate(50.0D, 20.0D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-23.5F));
        renderCelestialBody(30.0F);

        poseStack.popPose();

        // Sole
        RenderSystem.setShaderTexture(0, SUN_TEXTURE);
        poseStack.mulPose(Axis.YP.rotationDegrees(timeOfDay * 360.0F));
        poseStack.translate(100.0D, 0.0D, 0.0D);
        renderCelestialBody(20.0F);

        poseStack.popPose();

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        return true;
    }

    private void renderBlackBackground(PoseStack poseStack) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        float distance = 100.0F;

        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(-distance, -distance, -distance).endVertex();
        bufferbuilder.vertex(-distance, distance, -distance).endVertex();
        bufferbuilder.vertex(distance, distance, -distance).endVertex();
        bufferbuilder.vertex(distance, -distance, -distance).endVertex();

        Tesselator.getInstance().end();
    }

    private void renderTexturedSky(float scale) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.vertex(-scale, 0, -scale).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(scale, 0, -scale).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(scale, 0, scale).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(-scale, 0, scale).uv(0.0F, 1.0F).endVertex();

        Tesselator.getInstance().end();
    }

    private void renderCelestialBody(float scale) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.vertex(-scale, -scale, 0).uv(0.0F, 0.0F).endVertex();
        bufferbuilder.vertex(scale, -scale, 0).uv(1.0F, 0.0F).endVertex();
        bufferbuilder.vertex(scale, scale, 0).uv(1.0F, 1.0F).endVertex();
        bufferbuilder.vertex(-scale, scale, 0).uv(0.0F, 1.0F).endVertex();

        Tesselator.getInstance().end();
    }
}