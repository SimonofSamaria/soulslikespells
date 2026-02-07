package net.simonofsamaria.soulslikespells.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.simonofsamaria.soulslikespells.api.stat.StatScaling;
import net.simonofsamaria.soulslikespells.api.stat.StatType;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModRegistries;
import net.simonofsamaria.soulslikespells.scaling.LevelCostCalculator;
import net.simonofsamaria.soulslikespells.scaling.ScalingManager;

import java.util.List;
import java.util.Map;

/**
 * Public API for the SoulslikeSpells mod.
 * Other mods can use these methods to interact with the stat system.
 */
public final class SoulslikeSpellsAPI {
    private SoulslikeSpellsAPI() {}

    /** Get a player's soul data. */
    public static PlayerSoulData getSoulData(ServerPlayer player) {
        return player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
    }

    /** Get a player's current soul level. */
    public static int getSoulLevel(ServerPlayer player) {
        return getSoulData(player).getSoulLevel();
    }

    /** Get a player's current vanilla experience (used for level-up costs). */
    public static int getExperience(ServerPlayer player) {
        return player.totalExperience;
    }

    /** Get a player's level in a specific stat. */
    public static int getStatLevel(ServerPlayer player, ResourceLocation statId) {
        return getSoulData(player).getStatLevel(statId);
    }

    /** Get the experience cost to level up from the given level. */
    public static int getLevelUpCost(int currentSoulLevel) {
        return LevelCostCalculator.getCost(currentSoulLevel);
    }

    /** Look up a registered StatType by its ResourceLocation. */
    public static StatType getStatType(ResourceLocation statId) {
        return ModRegistries.STAT_TYPE_REGISTRY.get(statId);
    }

    /** Get all scaling definitions for a specific stat type. */
    public static List<StatScaling> getScalingsForStat(ResourceLocation statId) {
        return ScalingManager.getInstance().getScalingsForStat(statId);
    }

    /** Get all registered scaling definitions. */
    public static Map<ResourceLocation, List<StatScaling>> getAllScalings() {
        return ScalingManager.getInstance().getAllScalings();
    }

    /** Force recalculate all attribute modifiers for a player. */
    public static void recalculateModifiers(ServerPlayer player) {
        ScalingManager.getInstance().recalculateAll(player);
    }
}
