package net.shattered.rinth.item.custom;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.entity.LivingEntity;
import net.shattered.rinth.entity.UpgradeableTridentEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.sound.SoundCategory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import java.util.List;

public class UpgradedTridentItem extends CustomTridentItem {
    public static final float UPGRADED_ATTACK_DAMAGE = 27.0F; // Higher than base 18.0F
    public static final float UPGRADED_ATTACK_SPEED = -2.7F;  // Slightly faster than base -2.9F

    public UpgradedTridentItem(Item.Settings settings) {
        super(settings.maxDamage(500));
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
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (user instanceof PlayerEntity playerEntity) {
            int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
            if (i >= MIN_DRAW_DURATION) {
                if (!world.isClient) {
                    stack.damage(1, playerEntity, LivingEntity.getSlotForHand(user.getActiveHand()));

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
                    world.playSoundFromEntity(null, tridentEntity, (SoundEvent) SoundEvents.ITEM_TRIDENT_THROW, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    if (!playerEntity.isInCreativeMode()) {
                        playerEntity.getInventory().removeOne(stack);
                    }
                }
            }
        }
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
