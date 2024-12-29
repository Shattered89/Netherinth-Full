package net.shattered.rinth.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;

public class ModNetworking {
    public static final Identifier TRIDENT_DROP_ID = Identifier.of("netherinth:trident_drop");

    public static class TridentDropPayload implements CustomPayload {
        public static final CustomPayload.Id<TridentDropPayload> TYPE =
                new CustomPayload.Id<>(TRIDENT_DROP_ID);

        public static final PacketCodec<RegistryByteBuf, TridentDropPayload> CODEC =
                PacketCodec.unit(new TridentDropPayload());

        @Override
        public CustomPayload.Id<?> getId() {
            return TYPE;
        }
    }

    public static void register() {
        PayloadTypeRegistry.playC2S().register(
                TridentDropPayload.TYPE,
                TridentDropPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(TridentDropPayload.TYPE, (payload, context) -> {
            var player = context.player();
            // Create and handle the player action packet directly
            PlayerActionC2SPacket packet = new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.DROP_ITEM,
                    new BlockPos((int) player.getX(), (int) player.getY(), (int) player.getZ()),
                    Direction.UP
            );
            player.networkHandler.onPlayerAction(packet);
        });
    }

    // Helper method to send packet from client
    public static void sendTridentDropPacket() {
        ClientPlayNetworking.send(new TridentDropPayload());
    }
}