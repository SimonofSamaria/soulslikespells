package net.simonofsamaria.soulslikespells.api.stat;

import java.util.List;

/**
 * A piecewise linear scaling curve, Dark Souls style.
 * Defines how much bonus each allocated point provides within each level range.
 */
public record ScalingCurve(List<Breakpoint> breakpoints) {

    public record Breakpoint(int level, double bonusPerPoint) {}

    /**
     * Calculate the total accumulated bonus for a given stat level.
     * Uses piecewise linear interpolation between breakpoints.
     */
    public double calculateBonus(int statLevel) {
        if (breakpoints.isEmpty() || statLevel <= 0) return 0.0;

        double total = 0.0;
        for (int i = 0; i < breakpoints.size() - 1; i++) {
            Breakpoint current = breakpoints.get(i);
            Breakpoint next = breakpoints.get(i + 1);

            if (statLevel <= current.level()) break;

            int rangeStart = current.level();
            int rangeEnd = Math.min(statLevel, next.level());
            int pointsInRange = rangeEnd - rangeStart;

            if (pointsInRange > 0) {
                total += pointsInRange * current.bonusPerPoint();
            }
        }

        // Handle levels beyond the last breakpoint
        Breakpoint last = breakpoints.getLast();
        if (statLevel > last.level() && last.bonusPerPoint() > 0) {
            total += (statLevel - last.level()) * last.bonusPerPoint();
        }

        return total;
    }
}
