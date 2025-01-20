package net.todd.beyondinfinity.entity.energy;

import net.minecraftforge.energy.IEnergyStorage;

public class WireEnergyStorage implements IEnergyStorage {
    private int energy;
    private final int maxTransfer;
    private final int maxEnergy;

    public WireEnergyStorage(int maxTransfer, int maxEnergy) {
        this.maxTransfer = maxTransfer;
        this.maxEnergy = maxEnergy;
        this.energy = 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyReceived = Math.min(maxEnergy - energy, Math.min(maxTransfer, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            if (energyReceived > 0) {
                onEnergyChanged();
            }
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyExtracted = Math.min(energy, Math.min(maxTransfer, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            if (energyExtracted > 0) {
                onEnergyChanged();
            }
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return energy;
    }

    @Override
    public int getMaxEnergyStored() {
        return maxEnergy;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    public void setEnergy(int energy) {
        int oldEnergy = this.energy;
        this.energy = Math.min(energy, maxEnergy);
        if (oldEnergy != this.energy) {
            onEnergyChanged();
        }
    }

    public void onEnergyChanged() {
        // Override questo metodo nelle sottoclassi quando necessario
    }
}