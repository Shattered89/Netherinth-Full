package net.shattered.rinth.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.UUID;

public class LoyalItemEntity extends ItemEntity {
    private final UUID owner;
    private final int loyaltyLevel;

    public LoyalItemEntity(World world, ItemEntity original, PlayerEntity owner, int loyaltyLevel) {
        super(world, original.getX(), original.getY(), original.getZ(), original.getStack());
        this.owner = owner.getUuid();
        this.loyaltyLevel = loyaltyLevel;

        // Use the method to set pickup delay
        this.setPickupDelay(1);
    }

    @Override
    public void tick() {
        // Ensure we're on the server side
        if (this.getWorld().isClient()) {
            super.tick();
            return;
        }

        PlayerEntity owner = this.getWorld().getPlayerByUuid(this.owner);

        // If no owner or owner is invalid, behave like a normal item
        if (owner == null || !owner.isAlive() || owner.isSpectator()) {
            super.tick();
            return;
        }

        // Move towards the owner
        double pullStrength = 0.05 * this.loyaltyLevel;
        double yOffset = 0.015 * this.loyaltyLevel;

        // Calculate vector to owner
        double dx = owner.getX() - this.getX();
        double dy = owner.getEyeY() - this.getY();
        double dz = owner.getZ() - this.getZ();

        // Adjust velocity
        this.setVelocity(
                this.getVelocity().multiply(0.95)
                        .add(dx * pullStrength, dy * pullStrength, dz * pullStrength)
        );

        // Try to pick up when close to owner
        if (this.distanceTo(owner) < 1.5) {
            if (owner.getInventory().insertStack(this.getStack())) {
                this.discard();
            } else {
                // If inventory is full, drop the item
                this.dropStack(this.getStack());
                this.discard();
            }
        }

        super.tick();
    }
}