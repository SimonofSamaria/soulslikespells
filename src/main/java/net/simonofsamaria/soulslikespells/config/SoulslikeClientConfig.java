package net.simonofsamaria.soulslikespells.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SoulslikeClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_STAT_TOOLTIPS = BUILDER
            .comment("Show detailed stat scaling tooltips in the bonfire UI")
            .define("gui.showStatTooltips", true);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
