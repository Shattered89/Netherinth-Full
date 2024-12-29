// TridentPullPower.java
package net.shattered.rinth.power;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import net.shattered.rinth.entity.CustomTridentEntity;

import java.util.List;

public class TridentPullPower {
    private static boolean isPullingTrident = false;
    private static int pullTimer = 0;

    public static void tryPullTrident(PlayerEntity player) {
        if (!player.getWorld().isClient) {
            if (player.isUsingItem() && player.getMainHandStack().isEmpty()) {
                pullTimer++;
                if (pullTimer >= 60) { // 3 seconds
                    // Search in a large radius
                    Box searchBox = player.getBoundingBox().expand(64.0);
                    List<CustomTridentEntity> tridents = player.getWorld().getEntitiesByClass(
                            CustomTridentEntity.class,
                            searchBox,
                            trident -> trident.getOwner() != null &&
                                    trident.getOwner().equals(player) &&
                                    trident.getDataTracker().get(CustomTridentEntity.FROM_DROP)
                    );

                    for (CustomTridentEntity trident : tridents) {
                        if (!isPullingTrident) {
                            trident.startPulling();
                            isPullingTrident = true;
                        }
                    }
                }
            } else {
                pullTimer = 0;
                isPullingTrident = false;
            }
        }
    }
}