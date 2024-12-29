package net.shattered.rinth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.shattered.rinth.entity.CustomTridentEntity;
import net.shattered.rinth.entity.BasicTridentEntity;
import net.shattered.rinth.entity.UpgradeableTridentEntity;
import net.shattered.rinth.entity.ModEntityTypes;

import java.util.ArrayList;
import java.util.List;

public class RecallTridentCommand {
    private static final double TELEPORT_DISTANCE = 250.0; // Distance in blocks before teleport

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("recalltrident")
                        .executes(RecallTridentCommand::run)
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by players"));
            return 0;
        }

        // Get all loaded server worlds
        List<ServerWorld> worlds = new ArrayList<>();
        for (ServerWorld world : source.getServer().getWorlds()) {
            worlds.add(world);
        }

        boolean found = false;

        // Search each world for the trident
        for (ServerWorld world : worlds) {
            // Search for both types of tridents
            found |= searchAndRecallTridents(world, player, ModEntityTypes.CUSTOM_TRIDENT);
            found |= searchAndRecallTridents(world, player, ModEntityTypes.UPGRADEABLE_TRIDENT);
        }

        if (found) {
            source.sendFeedback(() -> Text.literal("Recalling your trident..."), false);
        } else {
            source.sendError(Text.literal("No trident found to recall"));
        }

        return 1;
    }

    private static <T extends CustomTridentEntity> boolean searchAndRecallTridents(ServerWorld world, ServerPlayerEntity player, EntityType<T> tridentType) {
        boolean foundAny = false;
        List<T> tridents = (List<T>) world.getEntitiesByType(
                tridentType,
                trident -> trident.getOwner() != null && trident.getOwner().equals(player)
        );

        for (T trident : tridents) {
            // Start pulling the trident
            trident.startPulling();
            foundAny = true;

            // Check if we need to teleport the trident
            boolean shouldTeleport = false;
            double distance = 0;

            // If in different dimensions, always teleport
            RegistryKey<World> tridentDim = trident.getWorld().getRegistryKey();
            RegistryKey<World> playerDim = player.getWorld().getRegistryKey();

            if (tridentDim != playerDim) {
                shouldTeleport = true;
            } else {
                // If in same dimension, check distance
                distance = player.squaredDistanceTo(trident);
                if (distance > TELEPORT_DISTANCE * TELEPORT_DISTANCE) {
                    shouldTeleport = true;
                }
            }

            // Handle teleportation if needed
            if (shouldTeleport) {
                // Remove from current world
                trident.remove(Entity.RemovalReason.CHANGED_DIMENSION);

                // Create new trident near player
                CustomTridentEntity newTrident;
                if (trident instanceof UpgradeableTridentEntity) {
                    newTrident = new UpgradeableTridentEntity(player.getWorld(), player, trident.getItemStack());
                } else {
                    newTrident = new BasicTridentEntity(player.getWorld(), player, trident.getItemStack());
                }

                // Set position
                newTrident.setPosition(
                        player.getX(),
                        player.getY() + 2.0, // Slightly above player
                        player.getZ()
                );

                // Copy important data
                newTrident.setOwner(player);
                newTrident.setPersistent(true);
                newTrident.startPulling();

                // Spawn in new world
                player.getWorld().spawnEntity(newTrident);
            }
        }

        return foundAny;
    }
}