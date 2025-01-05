package net.shattered.rinth.event;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ItemEntity;
import net.shattered.rinth.item.custom.UpgradedTridentItem;
import net.shattered.rinth.item.custom.TridentCollectorData;

public class PlayerDeathHandler {
    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof PlayerEntity player) {
                System.out.println("Player about to die: " + player.getName().getString());
                handlePlayerDeath(player);
            }
            return true; // Allow the death to proceed
        });
    }

    private static void handlePlayerDeath(PlayerEntity player) {
        System.out.println("Checking inventory size: " + player.getInventory().size());

        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            System.out.println("Checking slot " + i + ": " + stack.getItem().toString());

            if (stack.getItem() instanceof UpgradedTridentItem) {
                System.out.println("Found trident in slot " + i);
                String id = TridentCollectorData.getTridentId(stack);
                System.out.println("Trident ID: " + id);

                if (TridentCollectorData.canCollectPlayerDrops(stack)) {
                    System.out.println("Trident can collect drops - storing items");
                    TridentCollectorData.storePlayerDrops(player, stack);

                    // Drop the trident in the world after storing items
                    ItemEntity tridentEntity = player.dropItem(stack, true);
                    if (tridentEntity != null) {
                        tridentEntity.setPickupDelay(40);
                        System.out.println("Dropped trident with stored items");
                    } else {
                        System.out.println("Failed to drop trident!");
                    }
                    break;
                } else {
                    System.out.println("Trident is on cooldown");
                }
            }
        }
    }
}