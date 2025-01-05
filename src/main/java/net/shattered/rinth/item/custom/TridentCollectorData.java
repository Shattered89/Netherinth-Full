package net.shattered.rinth.item.custom;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TridentCollectorData {
    private static final Map<String, List<ItemStack>> storedItems = new HashMap<>();
    private static final Map<String, Integer> storedXP = new HashMap<>();
    private static final Map<String, Long> lastDeathCollection = new HashMap<>();
    private static final long DEATH_COLLECTION_COOLDOWN = 3600000; // 1 hour in milliseconds

    public static String getTridentId(ItemStack trident) {
        String name = trident.getName().getString();
        int damage = trident.getDamage();
        int enchants = trident.getEnchantments().getSize();
        return name + "_" + damage + "_" + enchants;
    }

    public static void storeDrops(List<ItemEntity> drops, ItemStack trident) {
        if (drops.isEmpty()) return;

        String id = getTridentId(trident);
        List<ItemStack> items = new ArrayList<>();

        System.out.println("Storing " + drops.size() + " drops for trident: " + id);

        for (ItemEntity itemEntity : drops) {
            items.add(itemEntity.getStack().copy());
            itemEntity.discard();
        }

        storedItems.put(id, items);
        System.out.println("Stored items in map, size: " + storedItems.size());
    }

    public static void storePlayerDrops(PlayerEntity player, ItemStack trident) {
        String id = getTridentId(trident);
        System.out.println("Attempting to store drops for trident: " + id);

        if (!canCollectPlayerDrops(trident)) {
            System.out.println("Trident on cooldown, cannot collect drops");
            return;
        }

        List<ItemStack> items = new ArrayList<>();
        System.out.println("Scanning inventory of size: " + player.getInventory().size());

        // Store XP
        int playerXP = player.experienceLevel;
        storedXP.put(id, playerXP);
        System.out.println("Stored XP levels: " + playerXP);
        player.experienceLevel = 0;  // Reset player's XP

        int itemCount = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && !(stack.getItem() instanceof UpgradedTridentItem)) {
                items.add(stack.copy());
                player.getInventory().removeStack(i);
                itemCount++;
                System.out.println("Added item to storage: " + stack.getItem().toString());
            }
        }

        if (itemCount > 0) {
            storedItems.put(id, items);
            lastDeathCollection.put(id, System.currentTimeMillis());
            System.out.println("Successfully stored " + itemCount + " items for trident " + id);
        } else {
            System.out.println("No items were stored for trident " + id);
        }
    }

    public static void returnItemsToPlayer(PlayerEntity player, ItemStack trident) {
        String id = getTridentId(trident);
        List<ItemStack> items = storedItems.get(id);

        if (items != null && !items.isEmpty() || storedXP.containsKey(id)) {
            // Create portal effect
            if (player.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(
                        ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1, player.getZ(),
                        30,
                        0.5, 0.5, 0.5,
                        0.1
                );
            }

            // Return items to player's inventory
            if (items != null && !items.isEmpty()) {
                for (ItemStack stack : items) {
                    System.out.println("Returning item: " + stack.getItem().toString());
                    if (!player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, true);
                    }
                }
                storedItems.remove(id);
            }

            // Return XP
            Integer xp = storedXP.get(id);
            if (xp != null) {
                player.addExperienceLevels(xp);
                System.out.println("Returned " + xp + " XP levels to player");
                storedXP.remove(id);
            }

            System.out.println("Items and XP returned and removed from storage");
        }
    }

    public static boolean hasStoredItems(ItemStack trident) {
        String id = getTridentId(trident);
        List<ItemStack> items = storedItems.get(id);
        boolean hasXP = storedXP.containsKey(id);
        return (items != null && !items.isEmpty()) || hasXP;
    }

    public static boolean canCollectPlayerDrops(ItemStack trident) {
        String id = getTridentId(trident);
        Long lastCollection = lastDeathCollection.get(id);
        boolean canCollect = lastCollection == null || System.currentTimeMillis() - lastCollection >= DEATH_COLLECTION_COOLDOWN;
        System.out.println("Checking if trident " + id + " can collect drops: " + canCollect);
        if (!canCollect) {
            long timeLeft = (lastCollection + DEATH_COLLECTION_COOLDOWN) - System.currentTimeMillis();
            System.out.println("Time left on cooldown: " + (timeLeft / 1000) + " seconds");
        }
        return canCollect;
    }

    public static void addCooldownTooltip(ItemStack trident, List<Text> tooltip) {
        String id = getTridentId(trident);
        Long lastCollection = lastDeathCollection.get(id);

        if (lastCollection != null) {
            long timeLeft = (lastCollection + DEATH_COLLECTION_COOLDOWN) - System.currentTimeMillis();

            if (timeLeft > 0) {
                long minutesLeft = timeLeft / 60000;
                long secondsLeft = (timeLeft % 60000) / 1000;
                tooltip.add(Text.literal("Death Collection Cooldown: " + minutesLeft + "m" + secondsLeft + "s")
                        .formatted(Formatting.GRAY));
            }
        }
    }
}