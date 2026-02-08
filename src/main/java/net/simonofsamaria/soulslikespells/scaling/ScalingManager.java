package net.simonofsamaria.soulslikespells.scaling;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.catalyst.CatalystModifierHelper;
import net.simonofsamaria.soulslikespells.catalyst.ScalingEntry;
import net.simonofsamaria.soulslikespells.catalyst.ScalingProfile;
import net.simonofsamaria.soulslikespells.catalyst.ScalingProfileManager;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;

import java.util.Map;

/**
 * Applies stat-based scaling (Mind, Dexterity) from ScalingProfiles.
 * Profiles loaded from scaling_profiles/stat_*.json are applied when player stats change.
 */
public class ScalingManager {
    private static final String STAT_PROFILE_PREFIX = "stat_";

    private static boolean isStatProfile(ResourceLocation profileId) {
        return profileId.getPath().contains(STAT_PROFILE_PREFIX);
    }

    private static ResourceLocation findStatProfileForStat(ResourceLocation statId) {
        String statPath = statId.getPath();
        for (ResourceLocation id : ScalingProfileManager.getInstance().getAll().keySet()) {
            if (isStatProfile(id) && id.getPath().endsWith(statPath)) {
                return id;
            }
        }
        return ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, STAT_PROFILE_PREFIX + statPath);
    }

    public static ScalingManager getInstance() {
        return ScalingManagerHolder.INSTANCE;
    }

    /**
     * Recalculate and apply all stat scaling modifiers for a player.
     */
    public void recalculateAll(ServerPlayer player) {
        PlayerSoulData soulData = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        removeAllStatModifiers(player);

        for (Map.Entry<ResourceLocation, ScalingProfile> entry : ScalingProfileManager.getInstance().getAll().entrySet()) {
            if (!isStatProfile(entry.getKey())) continue;

            CatalystModifierHelper.applyModifiersForStat(player, entry.getKey(), entry.getValue(), soulData);
        }
    }

    /**
     * Recalculate modifiers for a specific stat only.
     */
    public void recalculateStat(ServerPlayer player, ResourceLocation statId) {
        PlayerSoulData soulData = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        ResourceLocation profileId = findStatProfileForStat(statId);

        ScalingProfile profile = ScalingProfileManager.getInstance().get(profileId);
        if (profile == null) return;

        removeStatModifiersForProfile(player, profileId, profile);
        CatalystModifierHelper.applyModifiersForStat(player, profileId, profile, soulData);
    }

    private void removeAllStatModifiers(ServerPlayer player) {
        for (Map.Entry<ResourceLocation, ScalingProfile> entry : ScalingProfileManager.getInstance().getAll().entrySet()) {
            if (!isStatProfile(entry.getKey())) continue;
            removeStatModifiersForProfile(player, entry.getKey(), entry.getValue());
        }
    }

    private void removeStatModifiersForProfile(ServerPlayer player, ResourceLocation profileId, ScalingProfile profile) {
        for (ScalingEntry scalingEntry : profile.entries()) {
            ResourceLocation modifierId = CatalystModifierHelper.makeModifierIdForStat(profileId, scalingEntry);
            CatalystModifierHelper.removeModifier(player, scalingEntry.target(), modifierId);
        }
    }

    private static class ScalingManagerHolder {
        static final ScalingManager INSTANCE = new ScalingManager();
    }
}
