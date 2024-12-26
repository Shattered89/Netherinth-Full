package net.shattered.rinth.item.custom;

import dev.emi.trinkets.api.*;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.shattered.rinth.item.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ConduitHeartItem extends TrinketItem implements last {
    private static final int BASE_MAX_DURABILITY = 720; // 12 minutes * 60 seconds
    private static final int BASE_WATER_REGEN_AMOUNT = 3;
    private static final int AIR_DRAIN_AMOUNT = 1;
    private static final int TICKS_PER_UPDATE = 20;
    private static final int EFFECT_DURATION = 400; // 20 seconds (20 ticks * 20)
    private final boolean isPowered;

    private int tickCounter = 0;

    public ConduitHeartItem(Settings settings, boolean powered) {
        super(settings.maxDamage(powered ? 1440 : 720));
        this.isPowered = powered;
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!(entity instanceof PlayerEntity player)) return;

        tickCounter++;
        if (tickCounter < TICKS_PER_UPDATE) return;

        tickCounter = 0;
        int maxDurability = isPowered ? 1440 : BASE_MAX_DURABILITY;
        int waterRegenAmount = isPowered ? BASE_WATER_REGEN_AMOUNT * 3 : BASE_WATER_REGEN_AMOUNT;

        if (player.isSubmergedInWater()) {
            // Remove water breathing effect if it exists
            if (player.hasStatusEffect(StatusEffects.WATER_BREATHING)) {
                player.removeStatusEffect(StatusEffects.WATER_BREATHING);
            }
            // Only regenerate if we're not at max durability
            if (stack.getDamage() > 0) {
                stack.setDamage(Math.max(0, stack.getDamage() - waterRegenAmount));
            }
        } else {
            // Only apply effect and drain durability if we have more than 1 durability left
            if (stack.getDamage() < maxDurability - 1) {
                stack.setDamage(stack.getDamage() + AIR_DRAIN_AMOUNT);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WATER_BREATHING, EFFECT_DURATION, 0, true, false));
            }
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return isPowered;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (isPowered) {
            tooltip.add(Text.translatable("item.rinth.conduit_heart.powered").formatted(Formatting.AQUA));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        // Check if player is looking at a Conduit block for the unpowered version
        if (!isPowered && user.isSneaking()) {
            BlockHitResult hitResult = (BlockHitResult) user.raycast(5.0D, 0.0F, false);
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);

            if (blockState.getBlock() == Blocks.CONDUIT) {
                // Give the powered version
                ItemStack poweredStack = new ItemStack(ModItems.POWERED_CONDUIT_HEART);
                user.setStackInHand(hand, poweredStack);

                // Play effects
                world.playSound(null, user.getX(), user.getY(), user.getZ(),
                        SoundEvents.BLOCK_CONDUIT_ACTIVATE, SoundCategory.PLAYERS, 1.0F, 1.0F);
                world.addParticle(ParticleTypes.ENCHANTED_HIT,
                        blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5,
                        0.0D, 0.0D, 0.0D);

                return TypedActionResult.success(poweredStack);
            }
            return TypedActionResult.pass(stack);
        }

        // Normal trinket equipping logic
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(user);
        if (component.isPresent()) {
            TrinketInventory inventory = component.get().getInventory().get("chest").get("necklace");
            if (inventory != null && inventory.isEmpty()) {
                inventory.setStack(0, stack.copy());
                stack.setCount(0);
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.fail(stack);
    }
}