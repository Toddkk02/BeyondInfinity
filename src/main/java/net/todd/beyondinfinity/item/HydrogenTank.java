package net.todd.beyondinfinity.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;
import java.util.List;

public class HydrogenTank extends Item {
    public static final int MAX_HYDROGEN = 1000;
    private static final String TAG_HYDROGEN = "stored_hydrogen";

    public HydrogenTank(Properties properties) {
        super(properties.stacksTo(1).setNoRepair());
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int stored = getStoredHydrogen(stack);
        return Math.round(13.0F * ((float)stored / (float)MAX_HYDROGEN));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float percent = (float)getStoredHydrogen(stack) / (float)MAX_HYDROGEN;
        return 0xFF000000 | Math.round(percent * 120.0F) << 16 | Math.round(percent * 180.0F) << 8 | Math.round(percent * 255.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = new ItemStack(this);
        CompoundTag nbt = stack.getOrCreateTag();
        nbt.putInt(TAG_HYDROGEN, 0);
        return stack;
    }

    public int getStoredHydrogen(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        return nbt != null ? nbt.getInt(TAG_HYDROGEN) : 0;
    }

    public int addHydrogen(ItemStack stack, int amount) {
        if (amount <= 0) return 0;

        int stored = getStoredHydrogen(stack);
        int toAdd = Math.min(amount, MAX_HYDROGEN - stored);

        if (toAdd > 0) {
            CompoundTag nbt = stack.getOrCreateTag();
            nbt.putInt(TAG_HYDROGEN, stored + toAdd);
            return toAdd;
        }
        return 0;
    }

    public int removeHydrogen(ItemStack stack, int amount) {
        if (amount <= 0) return 0;

        int stored = getStoredHydrogen(stack);
        int toRemove = Math.min(amount, stored);

        if (toRemove > 0) {
            CompoundTag nbt = stack.getOrCreateTag();
            nbt.putInt(TAG_HYDROGEN, stored - toRemove);
            return toRemove;
        }
        return 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int stored = getStoredHydrogen(stack);
        tooltip.add(Component.literal("Hydrogen: " + stored + "/" + MAX_HYDROGEN + " mB"));
    }
}