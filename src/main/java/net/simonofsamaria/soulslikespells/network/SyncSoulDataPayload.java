package net.simonofsamaria.soulslikespells.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;

import java.util.HashMap;
import java.util.Map;

public record SyncSoulDataPayload(int soulLevel, int experience, Map<ResourceLocation, Integer> allocatedPoints)
        implements CustomPacketPayload {

    public static final Type<SyncSoulDataPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "sync_soul_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncSoulDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SyncSoulDataPayload decode(FriendlyByteBuf buf) {
            int soulLevel = buf.readVarInt();
            int experience = buf.readVarInt();
            int mapSize = buf.readVarInt();
            Map<ResourceLocation, Integer> points = new HashMap<>();
            for (int i = 0; i < mapSize; i++) {
                ResourceLocation key = buf.readResourceLocation();
                int value = buf.readVarInt();
                points.put(key, value);
            }
            return new SyncSoulDataPayload(soulLevel, experience, points);
        }

        @Override
        public void encode(FriendlyByteBuf buf, SyncSoulDataPayload payload) {
            buf.writeVarInt(payload.soulLevel());
            buf.writeVarInt(payload.experience());
            buf.writeVarInt(payload.allocatedPoints().size());
            payload.allocatedPoints().forEach((key, value) -> {
                buf.writeResourceLocation(key);
                buf.writeVarInt(value);
            });
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncSoulDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            var player = context.player();
            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
            data.setSoulLevel(payload.soulLevel());
            data.setExperience(payload.experience());
            data.getAllocatedPoints().clear();
            data.getAllocatedPoints().putAll(payload.allocatedPoints());
        });
    }

    /**
     * Send the current soul data to a specific player.
     */
    public static void sendToPlayer(ServerPlayer player) {
        PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        SyncSoulDataPayload payload = new SyncSoulDataPayload(
                data.getSoulLevel(),
                data.getExperience(),
                new HashMap<>(data.getAllocatedPoints())
        );
        PacketDistributor.sendToPlayer(player, payload);
    }
}
