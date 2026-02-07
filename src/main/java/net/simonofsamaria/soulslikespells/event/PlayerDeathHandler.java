package net.simonofsamaria.soulslikespells.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;

/**
 * Handles death penalty - optionally drops experience on death (Soulslike style).
 */
public class PlayerDeathHandler {

    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!SoulslikeCommonConfig.DEATH_PENALTY_ENABLED.getAsBoolean()) return;

        PlayerSoulData data = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        double ratio = SoulslikeCommonConfig.DEATH_PENALTY_RATIO.getAsDouble();
        int droppedExp = (int) (data.getExperience() * ratio);

        if (droppedExp > 0) {
            data.setExperience(data.getExperience() - droppedExp);
            SoulslikeSpells.LOGGER.debug("{} died and lost {} experience", player.getName().getString(), droppedExp);
            // TODO Phase 9: Spawn a recoverable soul orb at death location
        }
    }
}
