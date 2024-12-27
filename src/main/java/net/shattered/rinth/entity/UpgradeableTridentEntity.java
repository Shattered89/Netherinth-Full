package net.shattered.rinth.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

public class UpgradeableTridentEntity extends CustomTridentEntity {
    private static final double TARGETING_RANGE = 8.0;
    private static final float HOMING_SPEED = 0.8f;

    public UpgradeableTridentEntity(EntityType<? extends CustomTridentEntity> entityType, World world) {
        super(entityType, world);
    }

    public UpgradeableTridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super((EntityType<? extends CustomTridentEntity>) ModEntityTypes.UPGRADEABLE_TRIDENT, world);
        if (owner != null) {
            this.setOwner(owner);
            this.setPosition(
                    owner.getX(),
                    owner.getEyeY() - 0.1,
                    owner.getZ()
            );
        }
        if (stack != null) {
            this.setStack(stack.copy());
        }
    }

    @Override
    protected void initDataTracker() {
        // Empty implementation required by abstract method
    }

    @Override
    public void tick() {
        if (!this.inGround && !this.dealtDamage && this.getOwner() != null) {
            // Only seek targets if we haven't hit anything yet
            // Find nearest target
            LivingEntity target = findNearestTarget();

            if (target != null) {
                // Get position to aim at (center of target)
                Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.5, 0);
                Vec3d currentPos = this.getPos();
                Vec3d toTarget = targetPos.subtract(currentPos).normalize();

                // Set new velocity towards target
                Vec3d currentVel = this.getVelocity();
                double speed = Math.max(currentVel.length(), 0.1); // Avoid zero-length velocity
                Vec3d newVel = toTarget.multiply(speed * HOMING_SPEED);

                this.setVelocity(newVel);

                // Debug particles to show target location
                if (this.getWorld().isClient && this.age % 10 == 0) { // Reduce frequency
                    this.getWorld().addParticle(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            target.getX(),
                            target.getY() + target.getHeight() * 0.5,
                            target.getZ(),
                            0, 0, 0
                    );
                }

                // Debug message for owner
                if (this.getOwner() instanceof PlayerEntity player && this.age % 20 == 0) { // Reduce message spam
                    player.sendMessage(Text.literal("Tracking target: " + target.getName().getString()), true);
                }
            }
        }

        super.tick();

        // Add special particle effects
        if (!this.inGround && this.getWorld().isClient) {
            this.getWorld().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0, 0, 0
            );
        }
    }

    private LivingEntity findNearestTarget() {
        if (this.getWorld().isClient) return null;

        Box searchBox = Box.from(this.getPos()).expand(TARGETING_RANGE);
        List<LivingEntity> nearbyEntities = this.getWorld().getEntitiesByClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != this.getOwner() && entity.isAlive() && !entity.isSpectator()
        );

        return nearbyEntities.stream()
                .min(Comparator.comparingDouble(entity -> entity.squaredDistanceTo(this)))
                .orElse(null);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float damage = 18.0F;
        Entity owner = this.getOwner();

        if (entity instanceof LivingEntity target) {
            target.setOnFireFor(100); // Set target on fire for 5 seconds
        }

        super.onEntityHit(entityHitResult);

        // Mark as dealt damage to stop targeting
        this.dealtDamage = true;

        // Start return behavior if it has loyalty
        if (owner instanceof PlayerEntity && this.getWorld() instanceof ServerWorld serverWorld) {
            int loyaltyLevel = MathHelper.clamp(EnchantmentHelper.getTridentReturnAcceleration(serverWorld, this.getItemStack(), this), 0, 127);
            if (loyaltyLevel > 0) {
                this.startPulling();
            }
        }
    }

    @Override
    public double getDamage() {
        return super.getDamage() * 1.5;
    }
}