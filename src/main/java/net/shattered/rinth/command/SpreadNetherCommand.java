package net.shattered.rinth.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;
import net.shattered.rinth.event.NetherSpreadEvent;

import java.util.Optional;

public class SpreadNetherCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("spreadnether")
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes(SpreadNetherCommand::run)
        );
    }

    private static int run(CommandContext<ServerCommandSource> context) {
        return executeSpreadNether(context.getSource());
    }

    private static int executeSpreadNether(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) {
            source.sendError(Text.literal("Must be run by a player"));
            return 0;
        }

        if (player.getWorld().getRegistryKey() != World.OVERWORLD) {
            source.sendError(Text.literal("Must be in the overworld"));
            return 0;
        }

        // Find nearest portal blocks within 16 blocks
        BlockPos playerPos = player.getBlockPos();
        Optional<BlockPos> nearestPortal = BlockPos.streamOutwards(playerPos, 16, 16, 16)
                .filter(pos -> player.getWorld().getBlockState(pos).getBlock() == Blocks.NETHER_PORTAL)
                .findFirst();

        if (nearestPortal.isEmpty()) {
            source.sendError(Text.literal("No nether portal found nearby"));
            return 0;
        }

        // Spread the nether using the event
        BlockPos portalPos = nearestPortal.get();
        NetherSpreadEvent.spread((ServerWorld)player.getWorld(), portalPos);

        source.sendFeedback(() -> Text.literal("Successfully spread the nether!"), true);
        return 1;
    }
}