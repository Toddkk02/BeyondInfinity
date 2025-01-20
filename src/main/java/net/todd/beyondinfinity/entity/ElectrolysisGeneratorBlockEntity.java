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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.todd.beyondinfinity.blocks.ElectrolysisGenerator;
import net.todd.beyondinfinity.item.ModItems;
import net.todd.beyondinfinity.item.armor.SpaceSuitChestplate;
import net.todd.beyondinfinity.networking.ModMessages;
import net.todd.beyondinfinity.networking.packets.EnergySyncS2CPacket;
import net.todd.beyondinfinity.screen.ElectrolysisGeneratorMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectrolysisGeneratorBlockEntity extends BlockEntity implements MenuProvider {
    private static final int MAX_TRANSFER = 200;
    private static final int MAX_STORAGE = 10000;
    private static final int ENERGY_PER_TICK = 10;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
                case 1 -> stack.getItem() == ModItems.HYDROGEN_TANK.get() ||
                        (stack.getItem() instanceof SpaceSuitChestplate);
                case 2 -> stack.getItem() == ModItems.HYDROGEN_TANK.get() ||
                        (stack.getItem() instanceof SpaceSuitChestplate);
                default -> super.isItemValid(slot, stack);
            };
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!isItemValid(slot, stack)) {
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        private int energy = 0;

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int energyReceived = Math.min(MAX_STORAGE - energy, Math.min(MAX_TRANSFER, maxReceive));
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
            if (!simulate && energyExtracted > 0) {
                energy -= energyExtracted;
                setChanged();
            }
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            return energy;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_STORAGE;
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

    private int progress = 0;
    private final int maxProgress = 200;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> ElectrolysisGeneratorBlockEntity.this.progress;
                case 1 -> ElectrolysisGeneratorBlockEntity.this.maxProgress;
                case 2 -> energyStorage.getEnergyStored();
                case 3 -> energyStorage.getMaxEnergyStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) ElectrolysisGeneratorBlockEntity.this.progress = value;
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public ElectrolysisGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYSIS_GENERATOR_ENTITY.get(), pos, state);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY && side != null) {
            Direction facing = this.getBlockState().getValue(ElectrolysisGenerator.FACING);
            Direction relativeSide = getRelativeSide(facing, side);

            if (relativeSide == Direction.WEST) {
                return LazyOptional.of(() -> new IEnergyStorage() {
                    @Override
                    public int receiveEnergy(int maxReceive, boolean simulate) {
                        return energyStorage.receiveEnergy(maxReceive, simulate);
                    }

                    @Override
                    public int extractEnergy(int maxExtract, boolean simulate) {
                        return 0;
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
            }
        }

        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }

        return super.getCapability(cap, side);
    }

    private Direction getRelativeSide(Direction facing, Direction side) {
        if (facing == Direction.NORTH) return side;
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

    public static void tick(Level level, BlockPos pos, BlockState state, ElectrolysisGeneratorBlockEntity entity) {
        if (level.isClientSide()) return;

        if (entity.hasEnoughEnergy() && entity.canProcess()) {
            entity.progress++;
            entity.energyStorage.extractEnergy(ENERGY_PER_TICK, false);
            setChanged(level, pos, state);

            if (entity.progress >= entity.maxProgress) {
                entity.processElectrolysis();
            }
        } else {
            entity.progress = 0;
            setChanged(level, pos, state);
        }

        ModMessages.sendToClients(new EnergySyncS2CPacket(entity.energyStorage.getEnergyStored(), pos));
    }

    private void processElectrolysis() {
        if (this.canProcess()) {
            ItemStack inputSlot1 = this.itemHandler.getStackInSlot(0);
            ItemStack inputSlot2 = this.itemHandler.getStackInSlot(1);

            if (inputSlot2.getItem() instanceof SpaceSuitChestplate) {
                processSpaceSuitRefill(inputSlot1, inputSlot2);
            } else {
                processHydrogenTankFill(inputSlot1);
            }

            this.progress = 0;
            setChanged();
        }
    }

    private void processSpaceSuitRefill(ItemStack waterBucket, ItemStack spaceSuit) {
        if (spaceSuit.getItem() instanceof SpaceSuitChestplate) {
            // Fill the space suit with oxygen
            ((SpaceSuitChestplate) spaceSuit.getItem()).setOxygen(spaceSuit, 36000);

            // Remove water bucket and give back empty bucket
            this.itemHandler.extractItem(0, 1, false);
            this.itemHandler.insertItem(0, new ItemStack(Items.BUCKET), false);

            // Move the filled suit to output slot
            ItemStack filledSuit = spaceSuit.copy();
            this.itemHandler.extractItem(1, 1, false);
            this.itemHandler.insertItem(2, filledSuit, false);
        }
    }

    private void processHydrogenTankFill(ItemStack waterBucket) {
        ItemStack filledTank = new ItemStack(ModItems.HYDROGEN_TANK.get());
        CompoundTag nbt = filledTank.getOrCreateTag();
        nbt.putInt("stored_hydrogen", 1000);

        ItemStack bucket = new ItemStack(Items.BUCKET);

        this.itemHandler.extractItem(0, 1, false);
        this.itemHandler.extractItem(1, 1, false);

        this.itemHandler.insertItem(0, bucket, false);
        this.itemHandler.insertItem(2, filledTank, false);
    }

    private boolean hasEnoughEnergy() {
        return this.energyStorage.getEnergyStored() >= ENERGY_PER_TICK;
    }

    private boolean canProcess() {
        ItemStack waterBucket = this.itemHandler.getStackInSlot(0);
        ItemStack inputItem = this.itemHandler.getStackInSlot(1);
        ItemStack outputSlot = this.itemHandler.getStackInSlot(2);

        boolean hasWater = waterBucket.getItem() == Items.WATER_BUCKET;
        boolean hasInput = inputItem.getItem() == ModItems.HYDROGEN_TANK.get() ||
                inputItem.getItem() instanceof SpaceSuitChestplate;
        boolean hasSpace = outputSlot.isEmpty();

        return hasWater && hasInput && hasSpace;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("inventory", itemHandler.serializeNBT());
        nbt.putInt("electrolysis.progress", progress);
        nbt.putInt("energy", energyStorage.getEnergyStored());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("inventory"));
        progress = nbt.getInt("electrolysis.progress");
        ((IEnergyStorage)energyStorage).receiveEnergy(nbt.getInt("energy"), false);
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.beyondinfinity.electrolysis_generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ElectrolysisGeneratorMenu(id, inventory, this, this.data);
    }

    public void setEnergyLevel(int energy) {
        ((IEnergyStorage)energyStorage).receiveEnergy(energy, false);
    }

    public int getEnergy() {
        return energyStorage.getEnergyStored();
    }

    public int getMaxEnergy() {
        return energyStorage.getMaxEnergyStored();
    }

    public int getTransferRate() {
        return MAX_TRANSFER;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }
}