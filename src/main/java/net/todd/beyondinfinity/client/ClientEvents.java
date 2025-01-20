package net.todd.beyondinfinity.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.todd.beyondinfinity.BeyondInfinity;
import net.todd.beyondinfinity.entity.RocketEntity;
import net.todd.beyondinfinity.networking.ModMessages;
import net.todd.beyondinfinity.networking.packets.LaunchRocketC2SPacket;

@Mod.EventBusSubscriber(modid = BeyondInfinity.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (KeyBindings.LAUNCH_ROCKET_KEY.consumeClick()) {
            ModMessages.sendToServer(new LaunchRocketC2SPacket());
        }
    }

    @SubscribeEvent
    public static void onPlayerMount(EntityMountEvent event) {
        if (event.getEntityMounting() == Minecraft.getInstance().player &&
                event.getEntityBeingMounted() instanceof RocketEntity) {
            Minecraft.getInstance().options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
        }
    }
}