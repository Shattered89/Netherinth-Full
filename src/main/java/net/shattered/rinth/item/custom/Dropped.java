package net.shattered.rinth.item.custom;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface Dropped {
    boolean canBeEnchanted(ItemStack itemStack, Enchantment enchantment);

    boolean onStopUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks);
}
