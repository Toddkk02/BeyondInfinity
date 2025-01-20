package net.todd.beyondinfinity.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.todd.beyondinfinity.blocks.BatteryBlock;
import net.todd.beyondinfinity.networking.ModMessages;
import net.todd.beyondinfinity.networking.packets.EnergySyncS2CPacket;
import net.todd.beyondinfinity.screen.BatteryMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BatteryBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_TRANSFER = 1000;

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        private int energy = 0;
        private final int maxEnergy = 500000;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = Math.min(maxEnergy - energy, Math.min(MAX_TRANSFER, maxReceive));
            if (!simulate) {
                energy += energyReceived;
                setChanged();
                if (level != null && !level.isClientSide()) {
                    ModMessages.sendToClients(new EnergySyncS2CPacket(energy, worldPosition));
                }
            }
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int energyExtracted = Math.min(energy, Math.min(MAX_TRANSFER, maxExtract));
            if (!simulate) {
                energy -= energyExtracted;
                setChanged();
                if (level != null && !level.isClientSide()) {
                    ModMessages.sendToClients(new EnergySyncS2CPacket(energy, worldPosition));
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
    };

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> BatteryBlockEntity.this.getEnergy();
                case 1 -> BatteryBlockEntity.this.getMaxEnergy();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                BatteryBlockEntity.this.setEnergy(value);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public BatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BATTERY_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && side != null && level != null) {
            Direction facing = this.level.getBlockState(this.worldPosition).getValue(BatteryBlock.FACING);

            // Converti il lato relativo al blocco in base alla rotazione
            Direction relativeSide = getRelativeSide(facing, side);

            if (relativeSide == Direction.WEST) {  // Lato input (WEST - texture battery_block_input)
                return LazyOptional.of(() -> new IEnergyStorage() {
                    @Override
                    public int receiveEnergy(int maxReceive, boolean simulate) {
                        return energyStorage.receiveEnergy(maxReceive, simulate);
                    }

                    @Override
                    public int extractEnergy(int maxExtract, boolean simulate) {
                        return 0; // Non può estrarre dal lato input
                    }

                    @Override
                    public int getEnergyStored() {
                        return energyStorage.getEnergyStored();
                    }

                    @Override
                    public int getMaxEnergyStored() {
                        return energyStorage.getMaxEnergyStored();
                    }

                    @Override
                    public boolean canExtract() {
                        return false;
                    }

                    @Override
                    public boolean canReceive() {
                        return true;
                    }
                }).cast();
            } else if (relativeSide == Direction.EAST) {  // Lato output (EAST - texture battery_block_output)
                return LazyOptional.of(() -> new IEnergyStorage() {
                    @Override
                    public int receiveEnergy(int maxReceive, boolean simulate) {
                        return 0; // Non può ricevere dal lato output
                    }

                    @Override
                    public int extractEnergy(int maxExtract, boolean simulate) {
                        return energyStorage.extractEnergy(maxExtract, simulate);
                    }

                    @Override
                    public int getEnergyStored() {
                        return energyStorage.getEnergyStored();
                    }

                    @Override
                    public int getMaxEnergyStored() {
                        return energyStorage.getMaxEnergyStored();
                    }

                    @Override
                    public boolean canExtract() {
                        return true;
                    }

                    @Override
                    public boolean canReceive() {
                        return false;
                    }
                }).cast();
            }
        }
        return super.getCapability(cap, side);
    }

    // Metodo helper per convertire i lati in base alla rotazione
    private Direction getRelativeSide(Direction facing, Direction side) {
        // Se il blocco guarda a nord (default state)
        if (facing == Direction.NORTH) {
            return side;
        }

        // Rotazione necessaria per allineare con la direzione del blocco
        return switch (facing) {
            case SOUTH -> side.getOpposite();
            case WEST -> side.getClockWise();
            case EAST -> side.getCounterClockWise();
            default -> side;
        };
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyEnergyHandler = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyEnergyHandler.invalidate();
    }

    public void tick() {
        if (level != null && !level.isClientSide()) {
            ModMessages.sendToClients(new EnergySyncS2CPacket(energyStorage.getEnergyStored(), worldPosition));
            setChanged();
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        nbt.putInt("energy", energyStorage.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        if (nbt.contains("energy")) {
            setEnergy(nbt.getInt("energy"));
        }
    }

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    public void setEnergy(int energy) {
        ((IEnergyStorage)energyStorage).receiveEnergy(energy, false);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.beyondinfinity.battery_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new BatteryMenu(windowId, inventory, this, this.data);
    }
}