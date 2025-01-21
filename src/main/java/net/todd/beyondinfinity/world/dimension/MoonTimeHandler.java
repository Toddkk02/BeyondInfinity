package net.todd.beyondinfinity.world.dimension;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.todd.beyondinfinity.BeyondInfinity;

@Mod.EventBusSubscriber(modid = BeyondInfinity.MODID)
public class MoonTimeHandler {
    // Regular Minecraft day = 24000 ticks
    // 2 weeks = 14 days = 336000 ticks
    private static final long MOON_DAY_LENGTH = 336000L;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.LevelTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.level.isClientSide()) {
            Level level = event.level;

            if (level.dimension().equals(ModDimensions.MOON_KEY)) {
                ServerLevel serverLevel = (ServerLevel) level;
                long currentTime = serverLevel.getDayTime();

                // Increment by 1 every 14 ticks for slower progression
                if (currentTime % 14 == 0) {
                    serverLevel.setDayTime(currentTime + 1);
                }

                // Reset cycle when reaching moon day length
                if (currentTime >= MOON_DAY_LENGTH) {
                    serverLevel.setDayTime(0);
                }
            }
        }
    }
}