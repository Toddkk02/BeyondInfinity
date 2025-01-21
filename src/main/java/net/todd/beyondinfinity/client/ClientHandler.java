package net.todd.beyondinfinity.client;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.client.renderer.MoonSkyRenderer;

@Mod.EventBusSubscriber(modid = BeyondInfinity.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientHandler {
    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        ResourceLocation moonKey = new ResourceLocation(BeyondInfinity.MODID, "moon");
        event.register(moonKey, new MoonSkyRenderer());
        BeyondInfinity.LOGGER.info("Sky renderer registered for dimension: {}", moonKey);
    }
}
