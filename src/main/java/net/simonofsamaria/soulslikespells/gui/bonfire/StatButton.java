package net.simonofsamaria.soulslikespells.gui.bonfire;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A button in the bonfire screen for leveling up a specific stat.
 */
public class StatButton extends Button {

    private final ResourceLocation statId;

    public StatButton(int x, int y, int width, int height, Component message, OnPress onPress, ResourceLocation statId) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.statId = statId;
    }

    public ResourceLocation getStatId() {
        return statId;
    }
}
