package net.shattered.rinth;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class RightClickTracker {
    private static final Map<ServerPlayerEntity, Integer> holdDurations = new HashMap<>();
    private static final int REQUIRED_HOLD_TIME = 60; // 3 seconds at 20 ticks per second

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isUsingItem() && player.getMainHandStack().isEmpty()) {
                    holdDurations.put(player, holdDurations.getOrDefault(player, 0) + 1);
                    if (holdDurations.get(player) >= REQUIRED_HOLD_TIME) {
                        executeCommand(player);
                        holdDurations.remove(player); // Reset after execution
                    }
                } else {
                    holdDurations.remove(player); // Reset if the player stops holding
                }
            }
        });
    }

    private static void executeCommand(ServerPlayerEntity player) {
        player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource(), "/give @s golden_carrot 32");
    }
}
