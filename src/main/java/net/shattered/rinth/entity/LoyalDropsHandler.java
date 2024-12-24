package net.shattered.rinth.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoyalDropsHandler {
    /**
     * Handles drops when a living entity dies
     * @param entity The entity that died
     * @param damageSource The source of damage
     * @param drops The collection of item drops
     * @return Modified list of drops
     */
    public static List<ItemEntity> handleLivingDrops(LivingEntity entity, DamageSource damageSource, Collection<ItemEntity> drops) {
        // Get the owner of the trident or player who caused the damage
        Entity owner = damageSource.getAttacker();

        // Check if the owner is a player and the attack was via a trident
        if (owner instanceof PlayerEntity player) {
            // Determine loyalty level
            int loyaltyLevel = getLoyaltyLevel(damageSource);

            if (loyaltyLevel > 0) {
                ServerWorld world = (ServerWorld) entity.getWorld();

                // List to store new loyal item entities
                List<ItemEntity> loyalDrops = new ArrayList<>();

                // Replace each drop with a loyal item entity
                for (ItemEntity itemEntity : drops) {
                    LoyalItemEntity loyalItemEntity = new LoyalItemEntity(world, itemEntity, player, loyaltyLevel);
                    world.spawnEntity(loyalItemEntity);
                    loyalDrops.add(loyalItemEntity);
                }

                return loyalDrops;
            }
        }

        // If no loyalty, return original drops
        return new ArrayList<>(drops);
    }

    /**
     * Get the loyalty level from the damage source
     * @param damageSource The damage source
     * @return Loyalty level (0 if not applicable)
     */
    private static int getLoyaltyLevel(DamageSource damageSource) {
        // TODO: Implement proper loyalty level detection for your custom trident
        // This is a placeholder method
        Entity attacker = damageSource.getAttacker();

        // You'll need to replace this with actual logic to detect loyalty level
        // For example, check if the attacking entity is your custom trident
        return 1; // Default to 1 for now
    }
}