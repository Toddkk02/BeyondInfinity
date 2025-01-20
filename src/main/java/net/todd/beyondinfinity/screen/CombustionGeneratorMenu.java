package net.todd.beyondinfinity.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.todd.beyondinfinity.blocks.ModBlocks;
import net.todd.beyondinfinity.entity.CombustionGeneratorBlockEntity;
import net.todd.beyondinfinity.screen.slot.ModFuelSlot;
import org.jetbrains.annotations.NotNull;

public class CombustionGeneratorMenu extends AbstractContainerMenu {
    public final CombustionGeneratorBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    public CombustionGeneratorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public CombustionGeneratorMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.COMBUSTION_GENERATOR_MENU.get(), id);
        checkContainerSize(inv, 1);
        blockEntity = ((CombustionGeneratorBlockEntity) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new ModFuelSlot(handler, 0, 15, 35));  // Slot carbone spostato a sinistra
        });

        addDataSlots(data);
    }

    public boolean hasFuel() {
        return blockEntity.hasFuel();
    }

    public int getScaledTransfer() {
        if(!blockEntity.hasFuel()) {
            return 0;
        }

        int transferRate = 120; // FE/t fisso quando attivo
        int maxTransferRate = 120;
        int barHeight = 50; // Altezza totale della barra

        return maxTransferRate != 0 ? (int)(((float)transferRate / (float)maxTransferRate) * barHeight) : 0;
    }

    public int getTransferRate() {
        if(!blockEntity.hasFuel()) {
            return 0;
        }
        return 120; // FE/t fisso quando attivo
    }

    public int getBurnProgress() {
        int burnTime = this.data.get(2);
        int maxBurnTime = this.data.get(3);
        if (maxBurnTime == 0 || burnTime == 0) {
            return 0;
        }
        return burnTime * 13 / maxBurnTime;
    }

    public int getScaledEnergy() {
        int energy = blockEntity.getEnergy();
        int maxEnergy = blockEntity.getMaxEnergy();
        int barHeight = 50; // Altezza totale della barra

        return maxEnergy != 0 ? (int)(((float)energy / (float)maxEnergy) * barHeight) : 0;
    }

    public int getEnergy() {
        return blockEntity.getEnergy();
    }

    public int getMaxEnergy() {
        return blockEntity.getMaxEnergy();
    }

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int TE_INVENTORY_SLOT_COUNT = 1;

    @Override
    public @NotNull ItemStack quickMoveStack(Player playerIn, int index) {
        Slot sourceSlot = slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (index < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX
                    + TE_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + index);
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player, ModBlocks.COMBUSTION_GENERATOR.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}