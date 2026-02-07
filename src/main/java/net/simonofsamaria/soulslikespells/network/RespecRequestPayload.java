package net.simonofsamaria.soulslikespells.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record RespecRequestPayload() implements CustomPacketPayload {

    public static final Type<RespecRequestPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "respec_request"));

    public static final StreamCodec<FriendlyByteBuf, RespecRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new RespecRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RespecRequestPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            if (!hasRespecItem(player)) {
                PacketDistributor.sendToPlayer(player, new RespecDeniedPayload("respec.no_item"));
                return;
            }

            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
            PacketDistributor.sendToPlayer(player, new RespecAllowedPayload(data.getSoulLevel(), new HashMap<>(data.getAllocatedPoints())));
        });
    }

    private static boolean hasRespecItem(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Items.DIAMOND) && stack.getCount() >= 1) return true;
        }
        return false;
    }
}
