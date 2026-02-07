package net.simonofsamaria.soulslikespells.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModRegistries;
import net.simonofsamaria.soulslikespells.scaling.ScalingManager;

import java.util.HashMap;
import java.util.Map;

public record RespecApplyPayload(Map<ResourceLocation, Integer> newAllocation, int targetLevel)
        implements CustomPacketPayload {

    public static final Type<RespecApplyPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "respec_apply"));

    public static final StreamCodec<FriendlyByteBuf, RespecApplyPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RespecApplyPayload decode(FriendlyByteBuf buf) {
            int targetLevel = buf.readVarInt();
            int size = buf.readVarInt();
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(buf.readResourceLocation(), buf.readVarInt());
            }
            return new RespecApplyPayload(map, targetLevel);
        }

        @Override
        public void encode(FriendlyByteBuf buf, RespecApplyPayload payload) {
            buf.writeVarInt(payload.targetLevel());
            buf.writeVarInt(payload.newAllocation().size());
            payload.newAllocation().forEach((k, v) -> {
                buf.writeResourceLocation(k);
                buf.writeVarInt(v);
            });
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RespecApplyPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Validate allocation sums to targetLevel
            int sum = payload.newAllocation().values().stream().mapToInt(Integer::intValue).sum();
            if (sum != payload.targetLevel()) return;

            // Validate each stat
            for (var e : payload.newAllocation().entrySet()) {
                if (e.getValue() <= 0) continue;
                if (!ModRegistries.STAT_TYPE_REGISTRY.containsKey(e.getKey())) return;
                var statType = ModRegistries.STAT_TYPE_REGISTRY.get(e.getKey());
                if (statType != null && e.getValue() > statType.getMaxLevel()) return;
            }

            // Consume diamond only after validation passes
            int consumedSlot = consumeRespecItem(player);
            if (consumedSlot < 0) return;

            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
            data.reset();

            for (var e : payload.newAllocation().entrySet()) {
                if (e.getValue() > 0) {
                    data.setStatLevel(e.getKey(), e.getValue());
                }
            }
            data.setSoulLevel(payload.targetLevel());

            ScalingManager.getInstance().recalculateAll(player);
            SyncSoulDataPayload.sendToPlayer(player);

            // Sync the consumed slot to client (BonfireMenu has no inventory slots, so broadcastChanges won't sync)
            player.connection.send(new ClientboundContainerSetSlotPacket(-2, 0, consumedSlot, player.getInventory().getItem(consumedSlot)));

            SoulslikeSpells.LOGGER.info("{} performed a respec", player.getName().getString());
        });
    }

    /** Returns the slot index where diamond was consumed, or -1 if none found. */
    private static int consumeRespecItem(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.DIAMOND) && stack.getCount() >= 1) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().setItem(i, ItemStack.EMPTY);
                }
                return i;
            }
        }
        return -1;
    }
}
