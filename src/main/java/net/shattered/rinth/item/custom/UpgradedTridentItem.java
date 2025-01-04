package net.shattered.rinth.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shattered.rinth.entity.UpgradeableTridentEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;

import java.util.List;

public class UpgradedTridentItem extends CustomTridentItem {
    // Constants for base weapon stats
    private static final float UPGRADED_ATTACK_DAMAGE = 13.0F;  // Increased to compensate
    private static final float UPGRADED_ATTACK_SPEED = -2.7F;   // This gives 1.3 attack speed (4.0 - 2.7 = 1.3)

    public UpgradedTridentItem(Item.Settings settings) {
        super(settings.maxDamage(500));
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(
                                BASE_ATTACK_DAMAGE_MODIFIER_ID,
                                UPGRADED_ATTACK_DAMAGE - 2, // Minecraft adds 2 to the displayed value
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(
                                BASE_ATTACK_SPEED_MODIFIER_ID,
                                UPGRADED_ATTACK_SPEED,
                                EntityAttributeModifier.Operation.ADD_VALUE
                        ),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHit(stack, target, attacker);
        // Check if the item has no enchantments and add them
        if (EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            if (attacker.getWorld() != null) {
                applyDefaultEnchantments(stack, attacker.getWorld());
            }
        }
        return false;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        super.postMine(stack, world, state, pos, miner);
        // Check if the item has no enchantments and add them
        if (EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            applyDefaultEnchantments(stack, world);
        }
        return false;
    }

    private void applyDefaultEnchantments(ItemStack stack, World world) {
        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        // Combat enchantments
        RegistryEntry<Enchantment> sharpness = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.SHARPNESS);
        RegistryEntry<Enchantment> smite = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.SMITE);
        RegistryEntry<Enchantment> looting = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.LOOTING);
        RegistryEntry<Enchantment> fireAspect = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.FIRE_ASPECT);

        // Trident specific enchantments
        RegistryEntry<Enchantment> impaling = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.IMPALING);
        RegistryEntry<Enchantment> loyalty = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.LOYALTY);
        RegistryEntry<Enchantment> channeling = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.CHANNELING);

        // Durability enchantments
        RegistryEntry<Enchantment> mending = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.MENDING);
        RegistryEntry<Enchantment> unbreaking = world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.UNBREAKING);

        // Add max level enchantments
        builder.add(sharpness, 5);     // Sharpness V
        builder.add(smite, 5);         // Smite V
        builder.add(looting, 3);       // Looting III// Sweeping Edge III
        builder.add(fireAspect, 2);    // Fire Aspect II
        builder.add(impaling, 5);      // Impaling V
        builder.add(loyalty, 3);       // Loyalty III
        builder.add(channeling, 1);    // Channeling
        builder.add(mending, 1);       // Mending
        builder.add(unbreaking, 3);    // Unbreaking III

        // Apply all enchantments
        EnchantmentHelper.set(stack, builder.build());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        // Check if player is sneaking (shift)
        if (user.isSneaking()) {
            if (!world.isClient) {
                // Get current enchantments and create new component
                ItemEnchantmentsComponent currentEnchants = EnchantmentHelper.getEnchantments(itemStack);
                ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

                // Get registry entries for the enchantments
                RegistryEntry<Enchantment> loyaltyEntry = world.getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT)
                        .entryOf(Enchantments.LOYALTY);

                RegistryEntry<Enchantment> riptideEntry = world.getRegistryManager()
                        .get(RegistryKeys.ENCHANTMENT)
                        .entryOf(Enchantments.RIPTIDE);

                // Check current levels using registry entries
                int loyaltyLevel = currentEnchants.getLevel(loyaltyEntry);

                // Copy all enchantments except Loyalty and Riptide
                currentEnchants.getEnchantmentEntries().forEach((entry) -> {
                    RegistryEntry<Enchantment> enchantEntry = entry.getKey();
                    if (!enchantEntry.equals(loyaltyEntry) && !enchantEntry.equals(riptideEntry)) {
                        builder.add(enchantEntry, entry.getIntValue());
                    }
                });

                // Switch between Loyalty and Riptide
                if (loyaltyLevel > 0) {
                    builder.add(riptideEntry, 3);
                    user.sendMessage(Text.literal("Switched to Riptide Mode").formatted(Formatting.AQUA), true);
                } else {
                    builder.add(loyaltyEntry, 3);
                    user.sendMessage(Text.literal("Switched to Loyalty Mode").formatted(Formatting.AQUA), true);
                }

                // Apply the new enchantments
                ItemEnchantmentsComponent newEnchants = builder.build();
                EnchantmentHelper.set(itemStack, newEnchants);

                return TypedActionResult.success(itemStack);
            }
            return TypedActionResult.success(itemStack);
        }

        // Normal trident usage
        if (isAboutToBreak(itemStack)) {
            return TypedActionResult.fail(itemStack);
        } else if (EnchantmentHelper.getLevel(world.getRegistryManager()
                .get(RegistryKeys.ENCHANTMENT)
                .entryOf(Enchantments.RIPTIDE), itemStack) > 0
                && !isInWaterOrRainOrLava(user)) { // Changed condition here
            return TypedActionResult.fail(itemStack);
        } else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    private boolean isInWaterOrRainOrLava(PlayerEntity player) {
        return player.isTouchingWaterOrRain() || player.isInLava();
    }


    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
            if (i >= 10) {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
                if (!(f > 0.0F) || isInWaterOrRainOrLava(playerEntity)) {
                    if (!isAboutToBreak(stack)) {
                        RegistryEntry<SoundEvent> registryEntry = (RegistryEntry<SoundEvent>) EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND)
                                .orElse(SoundEvents.ITEM_TRIDENT_THROW);

                        if (!world.isClient) {
                            stack.damage(1, playerEntity, LivingEntity.getSlotForHand(user.getActiveHand()));
                            if (f == 0.0F) {
                                UpgradeableTridentEntity tridentEntity = new UpgradeableTridentEntity(world, playerEntity, stack) {
                                    @Override
                                    protected void initDataTracker() {
                                    }
                                };
                                tridentEntity.setVelocity(playerEntity, playerEntity.getPitch(), playerEntity.getYaw(), 0.0F, THROW_SPEED * 1.5F, 1.0F);

                                if (playerEntity.isInCreativeMode()) {
                                    tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                                }

                                world.spawnEntity(tridentEntity);
                                world.playSoundFromEntity(null, tridentEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);

                                if (!playerEntity.isInCreativeMode()) {
                                    playerEntity.getInventory().removeOne(stack);
                                }
                            }
                        }

                        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
                        if (f > 0.0F) {
                            float g = playerEntity.getYaw();
                            float h = playerEntity.getPitch();
                            float j = -MathHelper.sin(g * ((float) Math.PI / 180F)) * MathHelper.cos(h * ((float) Math.PI / 180F));
                            float k = -MathHelper.sin(h * ((float) Math.PI / 180F));
                            float l = MathHelper.cos(g * ((float) Math.PI / 180F)) * MathHelper.cos(h * ((float) Math.PI / 180F));
                            float m = MathHelper.sqrt(j * j + k * k + l * l);
                            float n = 3.0F * ((1.0F + 3) / 4.0F);
                            j *= n / m;
                            k *= n / m;
                            l *= n / m;
                            playerEntity.addVelocity(j, k, l);
                            playerEntity.useRiptide(20, 8.0F, stack);

                            if (playerEntity.isOnGround()) {
                                playerEntity.move(MovementType.SELF, new Vec3d(0.0F, 1.1999999F, 0.0F));
                            }

                            world.playSoundFromEntity(null, playerEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0F, 1.0F);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {

        // Check if enchantments need to be applied
        if (EnchantmentHelper.getEnchantments(stack).isEmpty()) {
            applyDefaultEnchantments(stack, world);
        }

        if (selected && entity instanceof PlayerEntity player) {
            // Only show targeting effect when holding the trident
            LivingEntity target = UpgradeableTridentEntity.getTargetedEntity(player, world);

            if (target != null && world.isClient) {
                // Create a circle of particles around the targeted entity
                double radius = target.getWidth() + 0.3; // Reduced radius to be closer to mob
                int particles = 8; // Reduced number of particles

                // Only spawn particles every other tick to reduce frequency
                if (world.getTime() % 2 == 0) {
                    for (int i = 0; i < particles; i++) {
                        double angle = ((world.getTime() * 0.1) + (2 * Math.PI * i) / particles);
                        double x = target.getX() + radius * Math.cos(angle);
                        double z = target.getZ() + radius * Math.sin(angle);
                        double y = target.getY() + target.getHeight() * 0.5;

                        world.addParticle(
                                ParticleTypes.SOUL_FIRE_FLAME,
                                x, y, z,
                                0, 0.02, 0  // Slight upward velocity
                        );
                    }
                }
            }
        }
    }

    @Override
    public boolean canBeEnchanted(ItemStack itemStack, Enchantment enchantment) {
        return true; // Allow all enchantments
    }

    /**
     * @param stack
     * @param world
     * @param tooltip
     * @param context
     */
    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {

    }
}