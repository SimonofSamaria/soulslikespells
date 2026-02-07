package net.simonofsamaria.soulslikespells.network;

import io.netty.buffer.ByteBuf;
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
import net.simonofsamaria.soulslikespells.scaling.ScalingManager;

public record LevelUpStatPayload(ResourceLocation statId) implements CustomPacketPayload {

    public static final Type<LevelUpStatPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "level_up_stat"));

    public static final StreamCodec<ByteBuf, LevelUpStatPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, LevelUpStatPayload::statId,
                    LevelUpStatPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(LevelUpStatPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // Validate stat type exists
            if (!ModRegistries.STAT_TYPE_REGISTRY.containsKey(payload.statId())) {
                SoulslikeSpells.LOGGER.warn("Player {} tried to level invalid stat: {}",
                        player.getName().getString(), payload.statId());
                return;
            }

            PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
            var statType = ModRegistries.STAT_TYPE_REGISTRY.get(payload.statId());

            // Check max stat level
            int currentStatLevel = data.getStatLevel(payload.statId());
            if (statType != null && currentStatLevel >= statType.getMaxLevel()) {
                return;
            }

            // Check max soul level
            if (data.getSoulLevel() >= SoulslikeCommonConfig.MAX_SOUL_LEVEL.getAsInt()) {
                return;
            }

            // Check experience cost
            int cost = LevelCostCalculator.getCost(data.getSoulLevel());
            if (!data.spendExperience(cost)) {
                return; // Not enough experience
            }

            // Fire cancellable event
            int previousLevel = currentStatLevel;
            int newLevel = currentStatLevel + 1;
            int newSoulLevel = data.getSoulLevel() + 1;
            PlayerLevelUpEvent event = new PlayerLevelUpEvent(player, payload.statId(), previousLevel, newLevel, newSoulLevel);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                data.addExperience(cost); // Refund experience
                return;
            }

            // Apply level up
            data.incrementStat(payload.statId());

            // Recalculate modifiers
            ScalingManager.getInstance().recalculateStat(player, payload.statId());

            // Sync data to client
            SyncSoulDataPayload.sendToPlayer(player);

            SoulslikeSpells.LOGGER.debug("{} leveled up {} to {}",
                    player.getName().getString(), payload.statId(), newLevel);
        });
    }
}
