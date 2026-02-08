package net.simonofsamaria.soulslikespells.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.api.event.PlayerLevelUpEvent;
import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModRegistries;
import net.simonofsamaria.soulslikespells.scaling.LevelCostCalculator;
import net.simonofsamaria.soulslikespells.service.PlayerStatService;
import net.simonofsamaria.soulslikespells.util.VanillaExperienceHelper;

import java.util.HashMap;
import java.util.Map;

public record ConfirmLevelUpPayload(Map<ResourceLocation, Integer> statDeltas) implements CustomPacketPayload {

    public static final Type<ConfirmLevelUpPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "confirm_level_up"));

    public static final StreamCodec<FriendlyByteBuf, ConfirmLevelUpPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ConfirmLevelUpPayload decode(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Map<ResourceLocation, Integer> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                map.put(buf.readResourceLocation(), buf.readVarInt());
            }
            return new ConfirmLevelUpPayload(map);
        }

        @Override
        public void encode(FriendlyByteBuf buf, ConfirmLevelUpPayload payload) {
            buf.writeVarInt(payload.statDeltas().size());
            payload.statDeltas().forEach((k, v) -> {
                buf.writeResourceLocation(k);
                buf.writeVarInt(v);
            });
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ConfirmLevelUpPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
            int baseLevel = data.getSoulLevel();
            int totalPoints = payload.statDeltas().values().stream().mapToInt(Integer::intValue).sum();

            if (totalPoints <= 0) return;

            // Total cost for adding totalPoints levels
            long totalCost = 0;
            for (int i = 0; i < totalPoints; i++) {
                totalCost += LevelCostCalculator.getCost(baseLevel + i);
            }

            if (!VanillaExperienceHelper.hasExperience(player, (int) totalCost)) return;
            if (data.getSoulLevel() + totalPoints > SoulslikeCommonConfig.MAX_SOUL_LEVEL.getAsInt()) return;

            // Validate each stat
            for (var e : payload.statDeltas().entrySet()) {
                if (e.getValue() <= 0) continue;
                if (!ModRegistries.STAT_TYPE_REGISTRY.containsKey(e.getKey())) return;
                var statType = ModRegistries.STAT_TYPE_REGISTRY.get(e.getKey());
                int current = data.getStatLevel(e.getKey());
                if (statType != null && current + e.getValue() > statType.getMaxLevel()) return;
            }

            // Fire events for each stat change (simplified: we fire one per stat)
            for (var e : payload.statDeltas().entrySet()) {
                if (e.getValue() <= 0) continue;
                int prev = data.getStatLevel(e.getKey());
                int next = prev + e.getValue();
                PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, e.getKey(), prev, next, baseLevel + totalPoints);
                if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    return;
                }
            }

            if (!VanillaExperienceHelper.deductExperience(player, (int) totalCost)) return;

            // Apply all
            for (var e : payload.statDeltas().entrySet()) {
                for (int i = 0; i < e.getValue(); i++) {
                    data.incrementStat(e.getKey());
                }
            }

            PlayerStatService.recalculateAndSync(player);
        });
    }
}
