package net.todd.beyondinfinity.networking.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.todd.beyondinfinity.client.ClientEnergyData;
import net.todd.beyondinfinity.entity.BatteryBlockEntity;
import net.todd.beyondinfinity.entity.CombustionGeneratorBlockEntity;

import java.util.function.Supplier;

public class EnergySyncS2CPacket {
    private final int energy;
    private final BlockPos pos;

    public EnergySyncS2CPacket(int energy, BlockPos pos) {
        this.energy = energy;
        this.pos = pos;
    }

    public EnergySyncS2CPacket(FriendlyByteBuf buf) {
        this.energy = buf.readInt();
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(energy);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // We are on the client
            if (Minecraft.getInstance().level == null) return;

            BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(pos);
            if (blockEntity instanceof CombustionGeneratorBlockEntity generator) {
                generator.setEnergyLevel(energy);
            } else if (blockEntity instanceof BatteryBlockEntity battery) {
                // Aggiorna i dati lato client
                ClientEnergyData.setEnergy(pos, energy);
            }
        });
        return true;
    }
}