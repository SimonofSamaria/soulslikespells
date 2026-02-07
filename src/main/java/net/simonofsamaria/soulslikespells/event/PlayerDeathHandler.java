package net.simonofsamaria.soulslikespells.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;
import net.simonofsamaria.soulslikespells.util.VanillaExperienceHelper;

/**
 * Handles optional death penalty - deducts vanilla XP on death (Soulslike style).
 * Vanilla already drops XP orbs on death; this adds an additional deduction from totalExperience.
 */
public class PlayerDeathHandler {

    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!SoulslikeCommonConfig.DEATH_PENALTY_ENABLED.getAsBoolean()) return;

        double ratio = SoulslikeCommonConfig.DEATH_PENALTY_RATIO.getAsDouble();
        int currentXp = VanillaExperienceHelper.getExperience(player);
        int droppedExp = (int) (currentXp * ratio);

        if (droppedExp > 0) {
            VanillaExperienceHelper.setExperience(player, currentXp - droppedExp);
            SoulslikeSpells.LOGGER.debug("{} died and lost {} experience", player.getName().getString(), droppedExp);
        }
    }
}
