package net.todd.beyondinfinity.world.dimension;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.todd.beyondinfinity.BeyondInfinity;

@Mod.EventBusSubscriber(modid = BeyondInfinity.MODID)
public class MoonGravityHandler {
    private static final double MOON_GRAVITY = 0.125; // 1/8 della gravità terrestre

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().dimension().equals(ModDimensions.MOON_KEY_LEVEL)) {
            Entity entity = event.getEntity();
            if (!entity.onGround() && entity.getDeltaMovement().y() < 0) {
                entity.setDeltaMovement(
                        entity.getDeltaMovement().x(),
                        entity.getDeltaMovement().y() * MOON_GRAVITY,
                        entity.getDeltaMovement().z()
                );
            }
        }
    }
}