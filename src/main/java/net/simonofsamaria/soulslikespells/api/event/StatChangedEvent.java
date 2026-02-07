package net.simonofsamaria.soulslikespells.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * Fired after a stat has been changed (by command, respec, or any means).
 * Not cancellable - the change has already occurred.
 */
public class StatChangedEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation statId;
    private final int oldValue;
    private final int newValue;

    public StatChangedEvent(ServerPlayer player, ResourceLocation statId, int oldValue, int newValue) {
        this.player = player;
        this.statId = statId;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public ServerPlayer getPlayer() { return player; }
    public ResourceLocation getStatId() { return statId; }
    public int getOldValue() { return oldValue; }
    public int getNewValue() { return newValue; }
}
