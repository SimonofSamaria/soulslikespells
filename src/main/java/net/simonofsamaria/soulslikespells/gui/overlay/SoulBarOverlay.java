package net.simonofsamaria.soulslikespells.gui.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.config.SoulslikeClientConfig;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;

/**
 * HUD overlay that shows the player's current soul count and level.
 */
public class SoulBarOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 12;
    private static final int BG_COLOR = 0x88000000;
    private static final int BORDER_COLOR = 0xFF8B7355;
    private static final int TEXT_COLOR = 0xFFD4AF37;

    public static void register(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "soul_bar"),
                new SoulBarOverlay()
        );
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!SoulslikeClientConfig.SHOW_SOUL_BAR.getAsBoolean()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerSoulData data = mc.player.getData(ModAttachments.PLAYER_SOUL_DATA.get());

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int offsetX = SoulslikeClientConfig.SOUL_BAR_X.getAsInt();
        int offsetY = SoulslikeClientConfig.SOUL_BAR_Y.getAsInt();

        int x = (screenWidth / 2) + offsetX - (BAR_WIDTH / 2);
        int y = screenHeight - offsetY;

        // Background
        graphics.fill(x - 1, y - 1, x + BAR_WIDTH + 1, y + BAR_HEIGHT + 1, BORDER_COLOR);
        graphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, BG_COLOR);

        // Soul count text
        String text = "SL " + data.getSoulLevel() + " | " + data.getExperience();
        int textWidth = mc.font.width(text);
        graphics.drawString(mc.font, text,
                x + (BAR_WIDTH - textWidth) / 2,
                y + 2,
                TEXT_COLOR, true);
    }
}
