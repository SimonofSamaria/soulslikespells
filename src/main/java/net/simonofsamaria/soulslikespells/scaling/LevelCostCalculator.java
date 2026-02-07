package net.simonofsamaria.soulslikespells.scaling;

import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;

/**
 * Calculates the vanilla XP cost to level up (soul level).
 * cost(level) = floor(a * level^3 + b * level^2 + c * level + d)
 * Supports both smooth curves (d>0) and DS3-style curves (d<0: early levels cheap, then ramp).
 */
public final class LevelCostCalculator {
    private LevelCostCalculator() {}

    /**
     * Calculate the experience cost to go from the given level to level+1.
     */
    public static int getCost(int currentLevel) {
        double a = SoulslikeCommonConfig.LEVEL_COST_A.getAsDouble();
        double b = SoulslikeCommonConfig.LEVEL_COST_B.getAsDouble();
        double c = SoulslikeCommonConfig.LEVEL_COST_C.getAsDouble();
        double d = SoulslikeCommonConfig.LEVEL_COST_D.getAsDouble();

        double cost = a * Math.pow(currentLevel, 3) + b * Math.pow(currentLevel, 2) + c * currentLevel + d;
        return Math.max(1, (int) Math.floor(cost));
    }

    /**
     * Calculate total experience needed to reach a target level from level 1.
     */
    public static long getTotalCostToLevel(int targetLevel) {
        long total = 0;
        for (int i = 1; i < targetLevel; i++) {
            total += getCost(i);
        }
        return total;
    }
}
