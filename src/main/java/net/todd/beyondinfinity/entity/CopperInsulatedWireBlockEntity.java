package net.todd.beyondinfinity.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.todd.beyondinfinity.entity.energy.WireEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CopperInsulatedWireBlockEntity extends BlockEntity {
    private static final int MAX_TRANSFER = 200;
    private static final int MAX_ENERGY = 1000;

    private final WireEnergyStorage energyStorage;
    private LazyOptional<IEnergyStorage> energy;

    public CopperInsulatedWireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COPPER_INSULATED_WIRE.get(), pos, state);
        this.energyStorage = new WireEnergyStorage(MAX_TRANSFER, MAX_ENERGY) {
            @Override
            public void setEnergy(int energy) {
                super.setEnergy(energy);
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        };
        this.energy = LazyOptional.of(() -> energyStorage);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CopperInsulatedWireBlockEntity blockEntity) {
        if (level.isClientSide) return;

        // Debug ogni 20 tick

        // Lista di tutti i ricevitori validi
        List<IEnergyStorage> receivers = new ArrayList<>();
        List<Direction> validDirections = new ArrayList<>();

        // Prima identifichiamo tutti i ricevitori validi
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighborEntity = level.getBlockEntity(neighborPos);

            if (neighborEntity == null) continue;
            if (neighborEntity instanceof CopperInsulatedWireBlockEntity otherWire) {
                // Se l'altro cavo ha meno energia di questo, è un ricevitore valido
                if (otherWire.energyStorage.getEnergyStored() < blockEntity.energyStorage.getEnergyStored()) {
                    receivers.add(otherWire.energyStorage);
                    validDirections.add(direction);
                }
            } else {
                neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                    if (handler.canReceive()) {
                        receivers.add(handler);
                        validDirections.add(direction);
                    }
                });
            }
        }

        // Se abbiamo energia da distribuire e ricevitori validi
        if (!receivers.isEmpty() && blockEntity.energyStorage.getEnergyStored() > 0) {
            int energyPerReceiver = blockEntity.energyStorage.getEnergyStored() / receivers.size();
            energyPerReceiver = Math.min(energyPerReceiver, MAX_TRANSFER);

            // Distribuisci l'energia equamente
            for (int i = 0; i < receivers.size(); i++) {
                IEnergyStorage receiver = receivers.get(i);
                Direction direction = validDirections.get(i);
                BlockPos neighborPos = pos.relative(direction);

                int toTransfer = Math.min(energyPerReceiver, blockEntity.energyStorage.getEnergyStored());
                if (toTransfer > 0) {
                    int simulated = receiver.receiveEnergy(toTransfer, true);
                    if (simulated > 0) {
                        int extracted = blockEntity.energyStorage.extractEnergy(simulated, false);
                        int actualTransferred = receiver.receiveEnergy(extracted, false);

                        if (actualTransferred > 0) {
                            blockEntity.setChanged();
                        }
                    }
                }
            }
        }

        // Raccogli energia dai fornitori vicini
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockEntity neighborEntity = level.getBlockEntity(neighborPos);

            if (neighborEntity == null) continue;
            if (neighborEntity instanceof CopperInsulatedWireBlockEntity) continue;

            neighborEntity.getCapability(ForgeCapabilities.ENERGY, direction.getOpposite()).ifPresent(handler -> {
                if (handler.canExtract()) {
                    int toExtract = Math.min(MAX_TRANSFER,
                            Math.min(handler.getEnergyStored(),
                                    blockEntity.energyStorage.getMaxEnergyStored() - blockEntity.energyStorage.getEnergyStored()));

                    if (toExtract > 0) {
                        int extracted = handler.extractEnergy(toExtract, false);
                        int accepted = blockEntity.energyStorage.receiveEnergy(extracted, false);

                        if (accepted > 0) {
                            blockEntity.setChanged();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("energy")) {
            energyStorage.setEnergy(tag.getInt("energy"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energy.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energy.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        energy = LazyOptional.of(() -> energyStorage);
    }
}