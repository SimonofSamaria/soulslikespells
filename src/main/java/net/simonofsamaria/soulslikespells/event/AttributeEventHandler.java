package net.simonofsamaria.soulslikespells.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.service.PlayerStatService;

/**
 * Handles player lifecycle events to recalculate attribute modifiers.
 */
public class AttributeEventHandler {

    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SoulslikeSpells.LOGGER.debug("Recalculating modifiers for {} on login", serverPlayer.getName().getString());
            PlayerStatService.recalculateAndSync(serverPlayer);
        }
    }

    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SoulslikeSpells.LOGGER.debug("Recalculating modifiers for {} on respawn", serverPlayer.getName().getString());
            PlayerStatService.recalculateAndSync(serverPlayer);
        }
    }

    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            SoulslikeSpells.LOGGER.debug("Recalculating modifiers for {} on dimension change", serverPlayer.getName().getString());
            PlayerStatService.recalculateAndSync(serverPlayer);
        }
    }
}
