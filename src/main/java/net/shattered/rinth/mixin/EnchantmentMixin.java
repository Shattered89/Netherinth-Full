package net.shattered.rinth.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.shattered.rinth.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
    @Inject(method = "isAcceptableItem", at = @At("HEAD"), cancellable = true)
    private boolean isAcceptableItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Enchantment enchantment = (Enchantment) (Object) this;

        if (stack.isOf(ModItems.PITCHFORK)) {
            return enchantment.equals(Enchantments.SHARPNESS) ||
                    enchantment.equals(Enchantments.LOOTING) ||
                    enchantment.equals(Enchantments.KNOCKBACK);
        }
        return false;
    }
}