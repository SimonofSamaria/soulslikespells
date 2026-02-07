package net.simonofsamaria.soulslikespells.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SoulslikeCommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Level cost formula: cost = a*level^3 + b*level^2 + c*level + d
    public static final ModConfigSpec.DoubleValue LEVEL_COST_A = BUILDER
            .comment("Cubic coefficient for level-up cost formula (DS3: 0.02)")
            .defineInRange("levelCost.a", 0.02, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LEVEL_COST_B = BUILDER
            .comment("Quadratic coefficient for level-up cost formula (DS3: 3.06)")
            .defineInRange("levelCost.b", 3.06, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue LEVEL_COST_C = BUILDER
            .comment("Linear coefficient for level-up cost formula (DS3: 105.6)")
            .defineInRange("levelCost.c", 105.6, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue LEVEL_COST_D = BUILDER
            .comment("Constant term for level-up cost formula (DS3: -895)")
            .defineInRange("levelCost.d", -895.0, -10000.0, 10000.0);

    public static final ModConfigSpec.IntValue MAX_SOUL_LEVEL = BUILDER
            .comment("Maximum soul level a player can reach")
            .defineInRange("maxSoulLevel", 802, 1, 10000);

    public static final ModConfigSpec.BooleanValue DEATH_PENALTY_ENABLED = BUILDER
            .comment("Whether players drop experience on death (Soulslike penalty)")
            .define("deathPenalty.enabled", false);

    public static final ModConfigSpec.DoubleValue DEATH_PENALTY_RATIO = BUILDER
            .comment("Fraction of experience dropped on death (0.0 to 1.0)")
            .defineInRange("deathPenalty.ratio", 1.0, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue SCALING_GLOBAL_MULTIPLIER = BUILDER
            .comment("Global multiplier applied to all scaling bonuses")
            .defineInRange("scaling.globalMultiplier", 1.0, 0.0, 10.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
