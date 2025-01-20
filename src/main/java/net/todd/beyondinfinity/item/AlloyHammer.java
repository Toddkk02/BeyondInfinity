package net.todd.beyondinfinity.item;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AlloyHammer extends Item {
    public AlloyHammer() {
        super(new Item.Properties()
                .durability(64));  // Solo durability, no stacksTo
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        ItemStack retval = itemStack.copy();
        if(retval.hurt(1, RandomSource.create(), null)) {
            return ItemStack.EMPTY;
        }
        return retval;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return 64;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canBeDepleted() {
        return true;
    }
}