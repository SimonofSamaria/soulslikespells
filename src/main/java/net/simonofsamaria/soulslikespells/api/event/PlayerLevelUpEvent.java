package net.simonofsamaria.soulslikespells.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when a player levels up a stat at the bonfire.
 * Can be cancelled to prevent the level up.
 */
public class PlayerLevelUpEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final ResourceLocation statId;
    private final int previousLevel;
    private final int newLevel;
    private final int newSoulLevel;

    public PlayerLevelUpEvent(ServerPlayer player, ResourceLocation statId, int previousLevel, int newLevel, int newSoulLevel) {
        this.player = player;
        this.statId = statId;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.newSoulLevel = newSoulLevel;
    }

    public ServerPlayer getPlayer() { return player; }
    public ResourceLocation getStatId() { return statId; }
    public int getPreviousLevel() { return previousLevel; }
    public int getNewLevel() { return newLevel; }
    public int getNewSoulLevel() { return newSoulLevel; }
}
