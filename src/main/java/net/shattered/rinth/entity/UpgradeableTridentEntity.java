package net.shattered.rinth.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shattered.rinth.item.custom.TridentCollectorData;
// Add this


import java.util.List;

public class UpgradeableTridentEntity extends CustomTridentEntity {
    private static final double TARGETING_RANGE = 32.0; // Range for looking at mobs
    private static final float HOMING_SPEED = 0.8f;
    private static final double TARGET_LOCK_ANGLE = Math.PI / 6; // 30 degrees for targeting tolerance

    private LivingEntity lockedTarget;

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
            // Get the targeted entity when thrown
            if (owner instanceof PlayerEntity player) {
                this.lockedTarget = getTargetedEntity(player, world);
            }
        }
        if (stack != null) {
            this.setStack(stack.copy());
        }
    }

    @Override
    protected void initDataTracker() {
    }

    // New method to find which entity the player is looking at
    public static LivingEntity getTargetedEntity(PlayerEntity player, World world) {
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVector();
        Vec3d targetVec = eyePos.add(lookVec.multiply(TARGETING_RANGE));

        Box searchBox = player.getBoundingBox().expand(TARGETING_RANGE);
        List<LivingEntity> nearbyEntities = player.getWorld().getEntitiesByClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive() && !entity.isSpectator()
        );

        LivingEntity closest = null;
        double closestAngle = TARGET_LOCK_ANGLE;

        for (LivingEntity entity : nearbyEntities) {
            Vec3d toEntity = entity.getPos().add(0, entity.getHeight() * 0.5, 0).subtract(eyePos).normalize();
            double angle = Math.acos(toEntity.dotProduct(lookVec));

            if (angle < closestAngle) {
                closestAngle = angle;
                closest = entity;
            }
        }

        return closest;
    }

    @Override
    protected void onEntityHit(EntityHitResult hitResult) {
        Entity entity = hitResult.getEntity();
        float damage = 10.0F;

        if (entity instanceof LivingEntity target) {
            float preHitHealth = target.getHealth();

            super.onEntityHit(hitResult);

            if (target.isDead() || target.getHealth() <= 0) {
                if (!this.getWorld().isClient) {
                    this.getWorld().getServer().execute(() -> {
                        List<ItemEntity> drops = this.getWorld().getEntitiesByClass(
                                ItemEntity.class,
                                target.getBoundingBox().expand(2.0),
                                item -> item.age <= 1  // Changed from getAge() to age
                        );

                        TridentCollectorData.storeDrops(drops, this.getItemStack());
                    });
                }
            }
        }

        this.dealtDamage = true;
    }

    @Override
    public void tick() {
        // Handle loyalty return behavior
        if (this.dealtDamage && this.getWorld() instanceof ServerWorld serverWorld) {
            Entity owner = this.getOwner();
            if (owner instanceof PlayerEntity) {
                int loyaltyLevel = EnchantmentHelper.getTridentReturnAcceleration(serverWorld, this.getItemStack(), this);
                if (loyaltyLevel > 0) {
                    this.startPulling();
                    Vec3d vec3d = owner.getEyePos().subtract(this.getPos());
                    this.setPos(this.getX(), this.getY() + vec3d.y * 0.015D * loyaltyLevel, this.getZ());
                    if (this.getWorld().isClient) {
                        this.lastRenderY = this.getY();
                    }

                    double d = 0.05D * loyaltyLevel;
                    this.setVelocity(this.getVelocity().multiply(0.95D).add(
                            vec3d.normalize().multiply(d)
                    ));
                }
            }
        }

        // Handle homing behavior only for locked target
        if (!this.inGround && !this.dealtDamage && this.lockedTarget != null && this.lockedTarget.isAlive()) {
            Vec3d targetPos = this.lockedTarget.getPos().add(0, this.lockedTarget.getHeight() * 0.5, 0);
            Vec3d currentPos = this.getPos();
            Vec3d toTarget = targetPos.subtract(currentPos).normalize();

            Vec3d currentVel = this.getVelocity();
            double speed = Math.max(currentVel.length(), 0.1);
            Vec3d newVel = toTarget.multiply(speed * HOMING_SPEED);

            this.setVelocity(newVel);

            // Trail particle effect
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

        super.tick();
    }
}