package net.simonofsamaria.soulslikespells.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SoulslikeCommonConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Level cost formula: cost = a*level^3 + b*level^2 + c*level + d
    // Cubic (a) + quadratic (b) + linear (c): smooth growth, ~12k XP total for 0→40.
    public static final ModConfigSpec.DoubleValue LEVEL_COST_A = BUILDER
            .comment("Cubic coefficient for level-up cost formula (smooth late-game acceleration)")
            .defineInRange("levelCost.a", 0.001, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue LEVEL_COST_B = BUILDER
            .comment("Quadratic coefficient (late-game penalty); cost grows faster at high levels")
            .defineInRange("levelCost.b", 0.02, 0.0, 100.0);

    public static final ModConfigSpec.DoubleValue LEVEL_COST_C = BUILDER
            .comment("Linear coefficient; keeps growth smooth, each level adds ~c more than previous")
            .defineInRange("levelCost.c", 10.0, 0.0, 1000.0);

    public static final ModConfigSpec.DoubleValue LEVEL_COST_D = BUILDER
            .comment("Base cost at level 0")
            .defineInRange("levelCost.d", 80.0, -10000.0, 10000.0);

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

    public static final ModConfigSpec.ConfigValue<String> RESPEC_ITEM = BUILDER
            .comment("Item ID consumed for respec (e.g. minecraft:diamond). Use empty string or set respec.amount to 0 for free respec.")
            .define("respec.item", "minecraft:diamond");

    public static final ModConfigSpec.IntValue RESPEC_AMOUNT = BUILDER
            .comment("Amount of respec item consumed per respec. Set to 0 for free respec.")
            .defineInRange("respec.amount", 1, 0, 64);

    public static final ModConfigSpec SPEC = BUILDER.build();

    /** Returns the item configured for respec consumption; falls back to diamond if invalid. */
    public static Item getRespecItem() {
        String id = RESPEC_ITEM.get();
        if (id == null || id.isBlank()) return Items.DIAMOND;
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            if (BuiltInRegistries.ITEM.containsKey(rl)) {
                return BuiltInRegistries.ITEM.get(rl);
            }
        } catch (Exception ignored) {}
        return Items.DIAMOND;
    }

    public static int getRespecAmount() {
        return RESPEC_AMOUNT.get();
    }
}
