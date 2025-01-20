// LaunchRocketC2SPacket.java
package net.todd.beyondinfinity.networking.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.todd.beyondinfinity.entity.RocketBlockEntity;
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
            // Sul lato server
            ServerPlayer player = context.getSender();
            if (player != null) {
                Entity vehicle = player.getVehicle();
                if (vehicle instanceof RocketEntity) {
                    BlockPos rocketPos = vehicle.blockPosition();
                    if (player.level().getBlockEntity(rocketPos) instanceof RocketBlockEntity rocketBlockEntity) {
                        rocketBlockEntity.handleSpacePress(player);
                    }
                }
            }
        });
        return true;
    }
}