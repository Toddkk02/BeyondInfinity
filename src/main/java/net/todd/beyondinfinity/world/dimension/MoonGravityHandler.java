package net.todd.beyondinfinity.world.dimension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.todd.beyondinfinity.BeyondInfinity;

@Mod.EventBusSubscriber(modid = BeyondInfinity.MODID)
public class MoonGravityHandler {
    private static final double MOON_GRAVITY = 0.65f;
    private static final double JUMP_BOOST = 1.2f;

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().level().dimension().equals(ModDimensions.MOON_KEY_LEVEL)) {
            if (event.getEntity() instanceof Player) {
                event.getEntity().setDeltaMovement(
                        event.getEntity().getDeltaMovement().x(),
                        JUMP_BOOST,
                        event.getEntity().getDeltaMovement().z()
                );
            }
        }
    }

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().dimension().equals(ModDimensions.MOON_KEY_LEVEL)) {
            Entity entity = event.getEntity();

            // Gestione della gravità aumentata
            if (!entity.onGround() && entity.getDeltaMovement().y() < 0) {
                entity.setDeltaMovement(
                        entity.getDeltaMovement().x(),
                        entity.getDeltaMovement().y() * MOON_GRAVITY,
                        entity.getDeltaMovement().z()
                );
            }
        }
    }

    // Gestione degli item droppati
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel().dimension().equals(ModDimensions.MOON_KEY_LEVEL)) {
            if (event.getEntity() instanceof ItemEntity) {
                ItemEntity item = (ItemEntity) event.getEntity();
                item.setDeltaMovement(
                        item.getDeltaMovement().x(),
                        item.getDeltaMovement().y() * MOON_GRAVITY,
                        item.getDeltaMovement().z()
                );
            }
        }
    }
}