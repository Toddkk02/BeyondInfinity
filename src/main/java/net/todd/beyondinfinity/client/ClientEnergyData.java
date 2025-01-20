package net.todd.beyondinfinity.client;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ClientEnergyData {
    private static final Map<BlockPos, Integer> ENERGY_DATA = new HashMap<>();

    public static void setEnergy(BlockPos pos, int energy) {
        ENERGY_DATA.put(pos, energy);
    }

    public static int getEnergy(BlockPos pos) {
        return ENERGY_DATA.getOrDefault(pos, 0);
    }

    public static void clearCache() {
        ENERGY_DATA.clear();
    }
}