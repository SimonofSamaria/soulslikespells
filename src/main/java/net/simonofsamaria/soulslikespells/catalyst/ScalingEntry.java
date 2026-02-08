package net.simonofsamaria.soulslikespells.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.List;
import java.util.Optional;

/**
 * Single scaling entry within a ScalingProfile.
 * Maps a source (single or composite) to a target attribute with curve and multiplier.
 *
 * <p>Source: string (single) or object (composite).
 * <ul>
 *   <li>Single: "soulslikespells:mind" (SLS stat or attribute)</li>
 *   <li>avg: {"avg": ["soulslikespells:faith", "soulslikespells:intelligence"]}</li>
 *   <li>min: {"min": ["soulslikespells:faith", "soulslikespells:intelligence"]}</li>
 *   <li>sum: {"sum": ["soulslikespells:faith", "soulslikespells:intelligence"]} (source_max = sum of max)</li>
 * </ul>
 * For avg/min, source_max stays 99.
 */
public record ScalingEntry(
        SourceExpr source,
        ResourceLocation target,
        String curve,
        double multiplier,
        double sourceMax,
        AttributeModifier.Operation operation,
        List<CurveMath.CurveNode> curveNodes
) {
    public static final Codec<AttributeModifier.Operation> OPERATION_CODEC = Codec.STRING.xmap(
            s -> switch (s.toUpperCase()) {
                case "ADD_VALUE" -> AttributeModifier.Operation.ADD_VALUE;
                case "ADD_MULTIPLIED_BASE" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                case "ADD_MULTIPLIED_TOTAL" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                default -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            },
            op -> op.name()
    );

    private static final Codec<CurveMath.CurveNode> CURVE_NODE_CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.DOUBLE.fieldOf("threshold").forGetter(CurveMath.CurveNode::threshold),
                    Codec.DOUBLE.fieldOf("percentage").forGetter(CurveMath.CurveNode::percentage)
            ).apply(inst, CurveMath.CurveNode::new)
    );

    public static final Codec<ScalingEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    SourceExpr.CODEC.fieldOf("source").forGetter(ScalingEntry::source),
                    ResourceLocation.CODEC.fieldOf("target").forGetter(ScalingEntry::target),
                    Codec.STRING.fieldOf("curve").forGetter(ScalingEntry::curve),
                    Codec.DOUBLE.optionalFieldOf("multiplier", 1.0).forGetter(ScalingEntry::multiplier),
                    Codec.DOUBLE.optionalFieldOf("source_max", 99.0).forGetter(ScalingEntry::sourceMax),
                    OPERATION_CODEC.optionalFieldOf("operation", AttributeModifier.Operation.ADD_MULTIPLIED_BASE).forGetter(ScalingEntry::operation),
                    CURVE_NODE_CODEC.listOf().optionalFieldOf("curve_nodes").forGetter(e -> e.curveNodes().isEmpty() ? Optional.empty() : Optional.of(e.curveNodes()))
            ).apply(instance, (source, target, curve, multiplier, sourceMax, operation, curveNodesOpt) ->
                    new ScalingEntry(source, target, curve, multiplier, sourceMax, operation,
                            curveNodesOpt.orElse(List.of())))
    );
}
