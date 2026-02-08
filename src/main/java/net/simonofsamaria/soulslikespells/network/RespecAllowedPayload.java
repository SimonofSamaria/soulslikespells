package net.simonofsamaria.soulslikespells.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.gui.bonfire.BonfireScreen;
import net.simonofsamaria.soulslikespells.gui.bonfire.BonfireDialogScreen;

import java.util.HashMap;
import java.util.Map;

public record RespecAllowedPayload(int originalLevel, Map<ResourceLocation, Integer> originalAllocation)
        implements CustomPacketPayload {

    public static final Type<RespecAllowedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "respec_allowed"));

    public static final StreamCodec<FriendlyByteBuf, RespecAllowedPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RespecAllowedPayload decode(FriendlyByteBuf buf) {
            int level = buf.readVarInt();
            int size = buf.readVarInt();
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(buf.readResourceLocation(), buf.readVarInt());
            }
            return new RespecAllowedPayload(level, map);
        }

        @Override
        public void encode(FriendlyByteBuf buf, RespecAllowedPayload payload) {
            buf.writeVarInt(payload.originalLevel());
            buf.writeVarInt(payload.originalAllocation().size());
            payload.originalAllocation().forEach((k, v) -> {
                buf.writeResourceLocation(k);
                buf.writeVarInt(v);
            });
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RespecAllowedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof BonfireDialogScreen r && r.getReturnScreen() instanceof BonfireScreen bonfire) {
                bonfire.enterRespecMode(payload.originalLevel());
                mc.setScreen(bonfire);
            } else if (mc.screen instanceof BonfireScreen bonfire) {
                bonfire.enterRespecMode(payload.originalLevel());
            }
        });
    }
}
