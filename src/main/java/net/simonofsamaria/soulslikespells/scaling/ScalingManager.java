package net.simonofsamaria.soulslikespells.scaling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.simonofsamaria.soulslikespells.api.stat.StatScaling;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;

import java.util.*;

/**
 * Central manager for all stat scaling relationships.
 * Loads scaling definitions from data packs and applies attribute modifiers to players.
 */
public class ScalingManager {
    private static final ScalingManager INSTANCE = new ScalingManager();
    private final Map<ResourceLocation, List<StatScaling>> scalingsByStatType = new HashMap<>();

    public static ScalingManager getInstance() { return INSTANCE; }

    public void clear() {
        scalingsByStatType.clear();
    }

    public void addScaling(StatScaling scaling) {
        scalingsByStatType.computeIfAbsent(scaling.statId(), k -> new ArrayList<>()).add(scaling);
    }

    public List<StatScaling> getScalingsForStat(ResourceLocation statId) {
        return scalingsByStatType.getOrDefault(statId, List.of());
    }

    public Map<ResourceLocation, List<StatScaling>> getAllScalings() {
        return Collections.unmodifiableMap(scalingsByStatType);
    }

    /**
     * Recalculate and apply all attribute modifiers for a player based on their current soul data.
     */
    public void recalculateAll(ServerPlayer player) {
        PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());

        for (Map.Entry<ResourceLocation, List<StatScaling>> entry : scalingsByStatType.entrySet()) {
            ResourceLocation statId = entry.getKey();
            int statLevel = data.getStatLevel(statId);

            for (StatScaling scaling : entry.getValue()) {
                BonusCalculator.applyModifier(player, scaling, statLevel);
            }
        }
    }

    /**
     * Recalculate modifiers for a specific stat only.
     */
    public void recalculateStat(ServerPlayer player, ResourceLocation statId) {
        PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        int statLevel = data.getStatLevel(statId);

        List<StatScaling> scalings = scalingsByStatType.getOrDefault(statId, List.of());
        for (StatScaling scaling : scalings) {
            BonusCalculator.applyModifier(player, scaling, statLevel);
        }
    }
}
