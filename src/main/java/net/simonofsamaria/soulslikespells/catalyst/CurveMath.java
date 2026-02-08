package net.simonofsamaria.soulslikespells.catalyst;

import java.util.List;

/**
 * Utility for curve-based interpolation.
 * Uses CurveNode(threshold, percentage) for piecewise linear curves.
 */
public final class CurveMath {

    public record CurveNode(double threshold, double percentage) {}

    private CurveMath() {}

    /**
     * Linear interpolation between curve nodes.
     * input is normalized (0..1) representing stat level as fraction of max.
     * Returns the interpolated percentage value.
     */
    public static double lerp(List<CurveNode> nodes, double input) {
        if (nodes == null || nodes.isEmpty()) return 0.0;
        if (input <= 0) return nodes.getFirst().percentage();

        for (int i = 0; i < nodes.size() - 1; i++) {
            CurveNode current = nodes.get(i);
            CurveNode next = nodes.get(i + 1);

            if (input <= next.threshold()) {
                double t = (next.threshold() - current.threshold()) > 0
                        ? (input - current.threshold()) / (next.threshold() - current.threshold())
                        : 1.0;
                return current.percentage() + t * (next.percentage() - current.percentage());
            }
        }

        return nodes.getLast().percentage();
    }

    /** Standard: linear growth. */
    public static final List<CurveNode> STANDARD = List.of(
            new CurveNode(0.0, 0.0),
            new CurveNode(1.0, 1.0)
    );

    /** Early bloom: fast growth early, slows later. */
    public static final List<CurveNode> EARLY_BLOOM = List.of(
            new CurveNode(0.0, 0.0),
            new CurveNode(0.3, 0.6),
            new CurveNode(0.6, 0.85),
            new CurveNode(1.0, 1.0)
    );

    /** Late: slow early, accelerates later. */
    public static final List<CurveNode> LATE = List.of(
            new CurveNode(0.0, 0.0),
            new CurveNode(0.3, 0.15),
            new CurveNode(0.6, 0.45),
            new CurveNode(1.0, 1.0)
    );

    /** Soft cap around 60%: strong early, diminishing returns after. */
    public static final List<CurveNode> SOFT_CAP_60 = List.of(
            new CurveNode(0.0, 0.0),
            new CurveNode(0.3, 0.5),
            new CurveNode(0.6, 0.85),
            new CurveNode(0.8, 0.92),
            new CurveNode(1.0, 1.0)
    );

    public static List<CurveNode> getCurve(String curveId) {
        return switch (curveId != null ? curveId : "standard") {
            case "early_bloom" -> EARLY_BLOOM;
            case "late" -> LATE;
            case "soft_cap_60" -> SOFT_CAP_60;
            default -> STANDARD;
        };
    }
}
