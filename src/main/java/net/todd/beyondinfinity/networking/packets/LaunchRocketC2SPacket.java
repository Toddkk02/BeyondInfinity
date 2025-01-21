package net.todd.beyondinfinity.networking.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.todd.beyondinfinity.entity.RocketEntity;

import java.util.function.Supplier;

public class LaunchRocketC2SPacket {
    public LaunchRocketC2SPacket() {
    }

    public LaunchRocketC2SPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity vehicle = player.getVehicle();
                if (vehicle instanceof RocketEntity rocketEntity) {
                    // Avvia direttamente il lancio del razzo
                    rocketEntity.launch();
                }
            }
        });
        return true;
    }
}