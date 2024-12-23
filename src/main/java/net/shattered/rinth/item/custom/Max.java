package net.shattered.rinth.item.custom;

import net.minecraft.item.ItemStack;

public interface Max {
    boolean isFireproof();

    int getMaxUseTime(ItemStack stack);
}
