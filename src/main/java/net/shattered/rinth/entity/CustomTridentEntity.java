package net.shattered.rinth.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shattered.rinth.item.ModItems;
import org.jetbrains.annotations.Nullable;

public class CustomTridentEntity extends PersistentProjectileEntity {
    private static final TrackedData<Byte> LOYALTY = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Boolean> ENCHANTED = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final TrackedData<Boolean> FROM_DROP = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> IS_BEING_PULLED = DataTracker.registerData(CustomTridentEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private boolean dealtDamage;
    public int returnTimer;
    private boolean isPersistent = false;
    private int chunkX;
    private int chunkZ;
    private boolean isChunkForced = false;

    public void setPersistent(boolean persistent) {
        this.isPersistent = persistent;
    }

    public boolean isPersistent() {
        return this.isPersistent;
    }

    private void forceLoadChunk() {
        if (!this.getWorld().isClient && !isChunkForced) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            chunkX = (int) this.getX() >> 4;
            chunkZ = (int) this.getZ() >> 4;
            serverWorld.setChunkForced(chunkX, chunkZ, true);
            isChunkForced = true;
        }
    }

    private void unforceLoadChunk() {
        if (!this.getWorld().isClient && isChunkForced) {
            ServerWorld serverWorld = (ServerWorld) this.getWorld();
            serverWorld.setChunkForced(chunkX, chunkZ, false);
            isChunkForced = false;
        }
    }

    private void checkDiscard() {
        if (this.isRemoved()) {
            unforceLoadChunk();
        }
    }

    public CustomTridentEntity(EntityType<? extends CustomTridentEntity> entityType, World world) {
        super(entityType, world);
        ItemStack defaultStack = new ItemStack(ModItems.PITCHFORK);
        this.setStack(defaultStack);
    }

    public CustomTridentEntity(World world, LivingEntity owner, ItemStack stack) {
        super((EntityType<? extends PersistentProjectileEntity>) ModEntityTypes.CUSTOM_TRIDENT, owner, world, stack, null);
        if (stack != null) {
            this.setStack(stack.copy());
            this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
            this.dataTracker.set(ENCHANTED, stack.hasGlint());
        }
    }

    public CustomTridentEntity(World world, double x, double y, double z, ItemStack stack) {
        super((EntityType<? extends PersistentProjectileEntity>) ModEntityTypes.CUSTOM_TRIDENT, x, y, z, world, stack, stack);
        if (stack != null) {
            this.setStack(stack.copy());
            this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
            this.dataTracker.set(ENCHANTED, stack.hasGlint());
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LOYALTY, (byte)0);
        builder.add(ENCHANTED, false);
        builder.add(FROM_DROP, false);
        builder.add(IS_BEING_PULLED, false);
    }

    public void startPulling() {
        this.dataTracker.set(IS_BEING_PULLED, true);
        this.setNoClip(true);
    }

    public void stopPulling() {
        this.dataTracker.set(IS_BEING_PULLED, false);
        this.setNoClip(false);
    }
    @Override
    public void tick() {
        // Check if we need to unforce chunks due to removal
        checkDiscard();

        // If persistent and about to be removed by game mechanics, prevent it
        if (this.isPersistent && this.isRemoved() && !this.isOnGround()) {
            this.unsetRemoved();
        }

        if (this.inGroundTime > 4) {
            this.dealtDamage = true;
        }

        // Handle chunk loading
        if (this.inGround && !isChunkForced) {
            forceLoadChunk();
        }

        // Update chunk forcing if the trident moves to a new chunk
        if (isChunkForced) {
            int newChunkX = (int) this.getX() >> 4;
            int newChunkZ = (int) this.getZ() >> 4;
            if (newChunkX != chunkX || newChunkZ != chunkZ) {
                unforceLoadChunk();
                chunkX = newChunkX;
                chunkZ = newChunkZ;
                forceLoadChunk();
            }
        }

        // Add particle effects while flying
        if (!this.inGround) {
            if (this.getWorld().isClient) {
                // Spawn smoke particles
                for (int i = 0; i < 2; i++) {
                    this.getWorld().addParticle(
                            ParticleTypes.LARGE_SMOKE,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.2,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.2,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.2,
                            0, 0, 0
                    );
                }

                // Spawn dripping lava particles
                if (this.random.nextFloat() < 0.3f) { // 30% chance each tick
                    this.getWorld().addParticle(
                            ParticleTypes.DRIPPING_LAVA,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            0, 0, 0
                    );
                }
            }
        }

        Entity entity = this.getOwner();
        boolean fromDrop = this.dataTracker.get(FROM_DROP);
        int i = this.dataTracker.get(LOYALTY);
        boolean isBeingPulled = this.dataTracker.get(IS_BEING_PULLED);

        // Handle manual pulling
        if (isBeingPulled && entity instanceof PlayerEntity) {
            this.setNoClip(true);
            Vec3d vec3d = entity.getEyePos().subtract(this.getPos());
            this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * 10.0, this.getZ());

            if (this.getWorld().isClient) {
                this.lastRenderY = this.getY();
            }

            double pullStrength = 3.0;
            this.setVelocity(this.getVelocity().multiply(0.8).add(vec3d.normalize().multiply(pullStrength)));

            if (this.returnTimer == 0) {
                this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
            }
            this.returnTimer++;

            if (this.distanceTo(entity) < 2.0) {
                if (!this.getWorld().isClient) {
                    ItemStack stack = this.getItemStack();
                    if (stack != null && ((PlayerEntity)entity).getInventory().insertStack(stack)) {
                        this.discard();
                    }
                }
            }
        }
        // Normal loyalty behavior for thrown tridents
        else if (!fromDrop && i > 0 && (this.dealtDamage || this.isNoClip()) && entity != null) {
            if (!this.isOwnerAlive()) {
                if (!this.getWorld().isClient && this.pickupType == PickupPermission.ALLOWED) {
                    this.dropStack(this.asItemStack(), 0.1F);
                }
                this.discard();
            } else {
                this.setNoClip(true);
                Vec3d vec3d = entity.getEyePos().subtract(this.getPos());
                this.setPos(this.getX(), this.getY() + vec3d.y * 0.015 * (double)i, this.getZ());
                if (this.getWorld().isClient) {
                    this.lastRenderY = this.getY();
                }

                double d = 0.05 * (double)i;
                this.setVelocity(this.getVelocity().multiply(0.95).add(vec3d.normalize().multiply(d)));
                if (this.returnTimer == 0) {
                    this.playSound(SoundEvents.ITEM_TRIDENT_RETURN, 10.0F, 1.0F);
                }
                this.returnTimer++;
            }
        }

        super.tick();
    }

    private boolean isOwnerAlive() {
        Entity entity = this.getOwner();
        return entity != null && entity.isAlive() && (!(entity instanceof ServerPlayerEntity) || !entity.isSpectator());
    }

    public boolean isEnchanted() {
        return this.dataTracker.get(ENCHANTED);
    }

    @Override
    protected boolean tryPickup(PlayerEntity player) {
        // Only allow pickup if the player is the owner
        if (this.isOwner(player)) {
            return player.getInventory().insertStack(this.asItemStack());
        }
        return false;
    }

    public static CustomTridentEntity createFromDrop(World world, PlayerEntity player, ItemStack stack) {
        CustomTridentEntity tridentEntity = new CustomTridentEntity(world, player, stack);

        // Calculate random velocities similar to regular item drops
        double xVel = world.random.nextDouble() * 0.2 - 0.1; // Random spread between -0.1 and 0.1
        double yVel = 0.2; // Consistent upward velocity
        double zVel = world.random.nextDouble() * 0.2 - 0.1; // Random spread between -0.1 and 0.1

        tridentEntity.setVelocity(xVel, yVel, zVel);

        // Position slightly above the player
        tridentEntity.setPosition(
                player.getX(),
                player.getY() + player.getStandingEyeHeight() * 0.5, // Spawn at roughly chest height
                player.getZ()
        );

        // Set entity properties for persistence
        tridentEntity.setPersistent(true);
        tridentEntity.setOwner(player);
        tridentEntity.pickupType = PickupPermission.CREATIVE_ONLY;
        tridentEntity.dataTracker.set(FROM_DROP, true);

        return tridentEntity;
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(ModItems.PITCHFORK);
    }

    private byte getLoyalty(ItemStack stack) {
        return this.getWorld() instanceof ServerWorld serverWorld ?
                (byte)MathHelper.clamp(EnchantmentHelper.getTridentReturnAcceleration(serverWorld, stack, this), 0, 127) : 0;
    }

    @Override
    protected float getDragInWater() {
        return 0.99F;
    }

    @Override
    protected SoundEvent getHitSound() {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        return true;
    }

    @Override
    public boolean hasNoGravity() {
        // When on ground, disable gravity to prevent further movement
        return this.inGround;
    }

    @Override
    public boolean shouldSave() {
        // Always return true to ensure the entity is saved with the chunk
        return true;
    }

    @Override
    public boolean isInvulnerable() {
        // Make the entity invulnerable to damage
        return true;
    }

    @Override
    public void age() {
        // Override age method to prevent despawning
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        float damage = 18.0F;
        Entity owner = this.getOwner();
        DamageSource damageSource = this.getDamageSources().trident(this, (Entity)(owner == null ? this : owner));

        ItemStack weaponStack = this.getWeaponStack();
        if (weaponStack != null && this.getWorld() instanceof ServerWorld serverWorld) {
            damage = EnchantmentHelper.getDamage(serverWorld, weaponStack, entity, damageSource, damage);
        }

        this.dealtDamage = true;
        if (entity.damage(damageSource, damage)) {
            // Set entity on fire for 100 ticks (5 seconds)
            entity.setOnFireFor(100);

            if (entity.getType() == EntityType.ENDERMAN) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                if (weaponStack != null && this.getWorld() instanceof ServerWorld serverWorld) {
                    EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource, weaponStack);
                }
                this.knockback(livingEntity, damageSource);
                this.onHit(livingEntity);
            }
        }

        this.setVelocity(this.getVelocity().multiply(-0.01, -0.1, -0.01));
        this.playSound(SoundEvents.ITEM_TRIDENT_HIT, 1.0F, 1.0F);
    }

    @Nullable
    @Override
    protected EntityHitResult getEntityCollision(Vec3d currentPosition, Vec3d nextPosition) {
        return this.dealtDamage ? null : super.getEntityCollision(currentPosition, nextPosition);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dealtDamage = nbt.getBoolean("DealtDamage");
        this.dataTracker.set(FROM_DROP, nbt.getBoolean("FromDrop"));
        this.setPersistent(nbt.getBoolean("Persistent"));
        this.chunkX = nbt.getInt("ForcedChunkX");
        this.chunkZ = nbt.getInt("ForcedChunkZ");
        boolean wasForced = nbt.getBoolean("IsChunkForced");
        if (wasForced) {
            forceLoadChunk();
        }
        ItemStack stack = this.getItemStack();
        if (stack != null) {
            this.dataTracker.set(LOYALTY, this.getLoyalty(stack));
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("DealtDamage", this.dealtDamage);
        nbt.putBoolean("FromDrop", this.dataTracker.get(FROM_DROP));
        nbt.putBoolean("Persistent", this.isPersistent);
        nbt.putInt("ForcedChunkX", chunkX);
        nbt.putInt("ForcedChunkZ", chunkZ);
        nbt.putBoolean("IsChunkForced", isChunkForced);
    }
}