package net.shattered.rinth.item.custom;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.shattered.rinth.entity.UpgradeableTridentEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;

import java.util.List;

public class UpgradedTridentItem extends CustomTridentItem {
    public static final float UPGRADED_ATTACK_DAMAGE = 27.0F;
    public static final float UPGRADED_ATTACK_SPEED = -2.7F;

    public UpgradedTridentItem(Item.Settings settings) {
        super(settings.maxDamage(500));
    }

    @Override
    public void onCraft(ItemStack stack, World world) {
        super.onCraft(stack, world);

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(ItemEnchantmentsComponent.DEFAULT);

        if (world != null) {
            // Add all default enchantments
            RegistryEntry<Enchantment> sharpness = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(Enchantments.SHARPNESS);
            RegistryEntry<Enchantment> looting = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(Enchantments.LOOTING);
            RegistryEntry<Enchantment> mending = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(Enchantments.MENDING);
            RegistryEntry<Enchantment> unbreaking = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(Enchantments.UNBREAKING);
            RegistryEntry<Enchantment> impaling = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(Enchantments.IMPALING);
            RegistryEntry<Enchantment> loyalty = world.getRegistryManager()
                    .get(RegistryKeys.ENCHANTMENT)
                    .entryOf(Enchantments.LOYALTY);

            builder.add(sharpness, 5);    // Max Sharpness V
            builder.add(looting, 3);      // Max Looting III
            builder.add(mending, 1);      // Mending
            builder.add(unbreaking, 3);   // Max Unbreaking III
            builder.add(impaling, 5);     // Max Impaling V
            builder.add(loyalty, 3);      // Start with Loyalty III

            EnchantmentHelper.set(stack, builder.build());
        }
    }

    public static AttributeModifiersComponent createAttributeModifiers() {
        return AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        new EntityAttributeModifier(BASE_ATTACK_DAMAGE_MODIFIER_ID, UPGRADED_ATTACK_DAMAGE, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .add(
                        EntityAttributes.GENERIC_ATTACK_SPEED,
                        new EntityAttributeModifier(BASE_ATTACK_SPEED_MODIFIER_ID, UPGRADED_ATTACK_SPEED, EntityAttributeModifier.Operation.ADD_VALUE),
                        AttributeModifierSlot.MAINHAND
                )
                .build();
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
                .entryOf(Enchantments.RIPTIDE), itemStack) > 0 && !user.isTouchingWaterOrRain()) {
            return TypedActionResult.fail(itemStack);
        } else {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(itemStack);
        }
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
            if (i >= 10) {
                float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
                if (!(f > 0.0F) || playerEntity.isTouchingWaterOrRain()) {
                    if (!isAboutToBreak(stack)) {
                        RegistryEntry<SoundEvent> registryEntry = (RegistryEntry<SoundEvent>)EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND)
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
                            float j = -MathHelper.sin(g * ((float)Math.PI / 180F)) * MathHelper.cos(h * ((float)Math.PI / 180F));
                            float k = -MathHelper.sin(h * ((float)Math.PI / 180F));
                            float l = MathHelper.cos(g * ((float)Math.PI / 180F)) * MathHelper.cos(h * ((float)Math.PI / 180F));
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
    public boolean canBeEnchanted(ItemStack itemStack, Enchantment enchantment) {
        return true; // Allow all enchantments
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        float damageValue = UPGRADED_ATTACK_DAMAGE;
        tooltip.add(Text.translatable("item.rinth.upgraded_trident.tooltip").formatted(Formatting.GRAY));
        tooltip.add(Text.empty());
        tooltip.add(Text.literal(" " + String.format("%.1f", damageValue) + " Attack Damage").formatted(Formatting.DARK_GREEN));
        tooltip.add(Text.literal(" " + String.format("%.1f", 4.0 + UPGRADED_ATTACK_SPEED) + " Attack Speed").formatted(Formatting.DARK_GREEN));
    }
}