package net.todd.beyondinfinity.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.entity.RocketEntity;
import net.todd.beyondinfinity.blocks.ModBlocks;

public class RocketRenderer extends EntityRenderer<RocketEntity> {
    private static final ResourceLocation HEAVY_DUTY_PLATE =
            new ResourceLocation(BeyondInfinity.MODID, "textures/block/heavy_duty_plate.png");
    private static final ResourceLocation STEEL_ALLOY =
            new ResourceLocation(BeyondInfinity.MODID, "textures/block/steel_alloy.png");

    private final BlockRenderDispatcher blockRenderer;

    public RocketRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blockRenderer = Minecraft.getInstance().getBlockRenderer();
        this.shadowRadius = 0.8F;
    }

    @Override
    public void render(RocketEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Posiziona il blocco
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        // Applica la rotazione in base alla direzione del razzo
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entityYaw));

        // Renderizza il blocco con le texture corrette
        blockRenderer.renderSingleBlock(
                ModBlocks.ROCKET_BLOCK.get().defaultBlockState(),
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        // Renderizza il giocatore all'interno del razzo
        if (!entity.getPassengers().isEmpty()) {
            Player player = (Player) entity.getPassengers().get(0);
            poseStack.pushPose();
            // Aggiustato il posizionamento del player per allinearlo meglio con il modello
            poseStack.translate(0.5D, 1.0D, 0.5D);
            Minecraft.getInstance().getEntityRenderDispatcher().render(
                    player,
                    0, 0, 0,
                    player.getYRot(),
                    partialTicks,
                    poseStack,
                    buffer,
                    packedLight
            );
            poseStack.popPose();
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity entity) {
        // Ritorna la texture principale del razzo
        return HEAVY_DUTY_PLATE;
    }
}