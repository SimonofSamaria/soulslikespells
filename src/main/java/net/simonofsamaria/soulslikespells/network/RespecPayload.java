package net.simonofsamaria.soulslikespells.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.scaling.ScalingManager;

/**
 * C2S: Client requests to respec (reset all allocated points).
 * Future: May require a consumable item (e.g., Larval Tear).
 */
public record RespecPayload() implements CustomPacketPayload {

    public static final Type<RespecPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "respec"));

    public static final StreamCodec<ByteBuf, RespecPayload> STREAM_CODEC = StreamCodec.unit(new RespecPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RespecPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
            // TODO Phase 9: Require a respec item (e.g., Larval Tear)
            data.reset();

            ScalingManager.getInstance().recalculateAll(player);
            SyncSoulDataPayload.sendToPlayer(player);

            SoulslikeSpells.LOGGER.info("{} performed a respec", player.getName().getString());
        });
    }
}
