package net.simonofsamaria.soulslikespells.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.gui.bonfire.BonfireDialogScreen;

public record RespecDeniedPayload(String reason, int amount, String itemId) implements CustomPacketPayload {

    public static final Type<RespecDeniedPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "respec_denied"));

    public static final StreamCodec<FriendlyByteBuf, RespecDeniedPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RespecDeniedPayload decode(FriendlyByteBuf buf) {
            return new RespecDeniedPayload(buf.readUtf(), buf.readVarInt(), buf.readUtf());
        }

        @Override
        public void encode(FriendlyByteBuf buf, RespecDeniedPayload payload) {
            buf.writeUtf(payload.reason());
            buf.writeVarInt(payload.amount());
            buf.writeUtf(payload.itemId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RespecDeniedPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            Screen current = mc.screen;
            Screen returnTo = current instanceof BonfireDialogScreen r ? r.getReturnScreen() : current;
            Component message = payload.amount() > 0 && !payload.itemId().isBlank()
                    ? Component.translatable(payload.reason(), payload.amount(),
                            getItemDisplayName(payload.itemId()))
                    : Component.translatable(payload.reason());
            mc.setScreen(BonfireDialogScreen.alert(returnTo,
                    Component.translatable("gui.soulslikespells.bonfire.respec_denied_title"),
                    message));
        });
    }

    private static Component getItemDisplayName(String itemId) {
        try {
            var rl = ResourceLocation.parse(itemId);
            if (BuiltInRegistries.ITEM.containsKey(rl)) {
                return new ItemStack(BuiltInRegistries.ITEM.get(rl)).getHoverName();
            }
        } catch (Exception ignored) {}
        return Component.literal(itemId);
    }
}
