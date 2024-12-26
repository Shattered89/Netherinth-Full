package net.shattered.rinth.item.custom;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface tooltip {
    void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, Item.TooltipContext context);
}
