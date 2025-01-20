package net.todd.beyondinfinity.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.todd.beyondinfinity.blocks.ModBlocks;
import net.todd.beyondinfinity.entity.BatteryBlockEntity;
import org.jetbrains.annotations.NotNull;

public class BatteryMenu extends AbstractContainerMenu {
    private final BatteryBlockEntity blockEntity;
    private final ContainerData data;
    public static final int ENERGY_DATA_INDEX = 0;
    public static final int MAX_ENERGY_DATA_INDEX = 1;

    public BatteryMenu(int windowId, Inventory inv, BatteryBlockEntity entity, ContainerData data) {
        super(ModMenuTypes.BATTERY_MENU.get(), windowId);
        this.blockEntity = entity;
        this.data = data;
        addDataSlots(this.data);
    }

    public BatteryMenu(int windowId, Inventory inv, FriendlyByteBuf extraData) {
        this(windowId, inv,
                (BatteryBlockEntity) inv.player.level().getBlockEntity(extraData.readBlockPos()),
                new SimpleContainerData(2));
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                player, ModBlocks.BATTERY_BLOCK.get());
    }

    public int getEnergy() {
        return data.get(ENERGY_DATA_INDEX);
    }

    public int getMaxEnergy() {
        return data.get(MAX_ENERGY_DATA_INDEX);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        return ItemStack.EMPTY;
    }
}