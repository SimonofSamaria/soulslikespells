package net.simonofsamaria.soulslikespells.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SoulslikeClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_SOUL_BAR = BUILDER
            .comment("Show the soul/experience bar on the HUD")
            .define("hud.showSoulBar", true);

    public static final ModConfigSpec.IntValue SOUL_BAR_X = BUILDER
            .comment("X position of the soul bar on screen (offset from center)")
            .defineInRange("hud.soulBarX", 0, -1000, 1000);

    public static final ModConfigSpec.IntValue SOUL_BAR_Y = BUILDER
            .comment("Y position of the soul bar on screen (offset from bottom)")
            .defineInRange("hud.soulBarY", 50, 0, 1000);

    public static final ModConfigSpec.BooleanValue SHOW_STAT_TOOLTIPS = BUILDER
            .comment("Show detailed stat scaling tooltips in the bonfire UI")
            .define("gui.showStatTooltips", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
