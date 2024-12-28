package net.shattered.rinth.item.custom;

import dev.emi.trinkets.api.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.Optional;

public class GoldenRing extends TrinketItem {

    public GoldenRing(Settings settings) {
        super(settings.maxCount(1)); // Ensure maxCount is 1 for trinkets
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Try to equip the ring in the "ring" slot
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(user);
        if (component.isPresent()) {
            var accessoryGroup = component.get().getInventory().get("accessory");
            if (accessoryGroup != null) {
                TrinketInventory ringInventory = accessoryGroup.get("ring");
                if (ringInventory != null && ringInventory.isEmpty()) {
                    ringInventory.setStack(0, stack.copy());
                    stack.setCount(0);
                    return TypedActionResult.success(stack);
                }
            }
        }

        return TypedActionResult.fail(stack);
    }

    @Override
    public boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        // Allow equipping only in the "ring" slot
        if (slot.inventory() != null && slot.inventory().getSlotType() != null) {
            return "ring".equals(slot.inventory().getSlotType().getName());
        }
        return false;
    }
}
