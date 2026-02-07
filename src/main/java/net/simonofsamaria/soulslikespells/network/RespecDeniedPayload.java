package net.simonofsamaria.soulslikespells.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

public record RespecDeniedPayload(String reason) implements CustomPacketPayload {

    public static final Type<RespecDeniedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "respec_denied"));

    public static final StreamCodec<FriendlyByteBuf, RespecDeniedPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RespecDeniedPayload decode(FriendlyByteBuf buf) {
            return new RespecDeniedPayload(buf.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buf, RespecDeniedPayload payload) {
            buf.writeUtf(payload.reason());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RespecDeniedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(null); // 关闭面板
            if (mc.player != null) {
                mc.player.displayClientMessage(Component.translatable(payload.reason()), true);
            }
        });
    }
}
