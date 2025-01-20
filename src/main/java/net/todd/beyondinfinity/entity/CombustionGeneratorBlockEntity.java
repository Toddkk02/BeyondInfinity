package net.todd.beyondinfinity.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.todd.beyondinfinity.blocks.CombustionGenerator;
import net.todd.beyondinfinity.networking.ModMessages;
import net.todd.beyondinfinity.networking.packets.EnergySyncS2CPacket;
import net.todd.beyondinfinity.screen.CombustionGeneratorMenu;
import net.todd.beyondinfinity.entity.energy.WireEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CombustionGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    private int progress = 0;
    private int maxProgress = 78;
    private int burnTime = 0;
    private int maxBurnTime = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return ForgeHooks.getBurnTime(stack, null) > 0;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private final WireEnergyStorage energyStorage = new WireEnergyStorage(200, 60000) {
        @Override
        public void onEnergyChanged() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                ModMessages.sendToClients(new EnergySyncS2CPacket(this.getEnergyStored(), worldPosition));
            }
        }
    };

    private LazyOptional<IEnergyStorage> lazyEnergyHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CombustionGeneratorBlockEntity.this.progress;
                case 1 -> CombustionGeneratorBlockEntity.this.maxProgress;
                case 2 -> CombustionGeneratorBlockEntity.this.burnTime;
                case 3 -> CombustionGeneratorBlockEntity.this.maxBurnTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CombustionGeneratorBlockEntity.this.progress = value;
                case 1 -> CombustionGeneratorBlockEntity.this.maxProgress = value;
                case 2 -> CombustionGeneratorBlockEntity.this.burnTime = value;
                case 3 -> CombustionGeneratorBlockEntity.this.maxBurnTime = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public CombustionGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMBUSTION_GENERATOR.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && side != null && level != null) {
            Direction facing = this.level.getBlockState(this.worldPosition).getValue(CombustionGenerator.FACING);

            // Converte il lato relativo al blocco in base alla rotazione
            Direction relativeSide = getRelativeSide(facing, side);

            // Se il lato è EAST (dove c'è la texture right)
            if (relativeSide == Direction.EAST) {
                return LazyOptional.of(() -> new IEnergyStorage() {
                    @Override
                    public int receiveEnergy(int maxReceive, boolean simulate) {
                        return 0; // Non può ricevere energia
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
            return LazyOptional.empty();
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
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
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        lazyEnergyHandler = LazyOptional.of(() -> energyStorage);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyHandler.invalidate();
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            return;
        }

        if (isConsumingFuel()) {
            this.burnTime--;
            generateEnergy();
            setChanged();
        }

        if (!isConsumingFuel()) {
            ItemStack stack = itemHandler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                int burnTime = ForgeHooks.getBurnTime(stack, null);
                if (burnTime > 0) {
                    stack.shrink(1);
                    this.burnTime = burnTime;
                    this.maxBurnTime = burnTime;
                    setChanged();
                }
            }
        }

        if (level != null && !level.isClientSide()) {
            ModMessages.sendToClients(new EnergySyncS2CPacket(energyStorage.getEnergyStored(), worldPosition));
        }
    }

    private void generateEnergy() {
        if (energyStorage.getEnergyStored() < energyStorage.getMaxEnergyStored()) {
            energyStorage.receiveEnergy(200, false); // Genera 200 FE/t
        }
    }

    private boolean isConsumingFuel() {
        return this.burnTime > 0;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("combustion_generator.progress", this.progress);
        nbt.putInt("combustion_generator.burnTime", this.burnTime);
        nbt.putInt("combustion_generator.maxBurnTime", this.maxBurnTime);
        nbt.putInt("energy", energyStorage.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("combustion_generator.progress");
        burnTime = nbt.getInt("combustion_generator.burnTime");
        maxBurnTime = nbt.getInt("combustion_generator.maxBurnTime");
        energyStorage.setEnergy(nbt.getInt("energy"));
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Combustion Generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        ModMessages.sendToClients(new EnergySyncS2CPacket(energyStorage.getEnergyStored(), worldPosition));
        return new CombustionGeneratorMenu(id, inventory, this, this.data);
    }

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    public void setEnergyLevel(int energy) {
        energyStorage.setEnergy(energy);
    }

    public boolean hasFuel() {
        return this.burnTime > 0;
    }
}