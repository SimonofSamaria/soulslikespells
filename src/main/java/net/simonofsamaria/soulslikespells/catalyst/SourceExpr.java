package net.simonofsamaria.soulslikespells.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Source expression for scaling input. Supports single value or composite (avg, min, sum).
 * For avg/min, source_max stays the same as single stat (0..99).
 */
public sealed interface SourceExpr permits SourceExpr.Single, SourceExpr.Sum, SourceExpr.Min, SourceExpr.Avg {

    /** Single stat or attribute. */
    record Single(ResourceLocation id) implements SourceExpr {}

    /** Sum of multiple sources. source_max = sum of each's max (e.g. 198 for two stats). */
    record Sum(List<ResourceLocation> ids) implements SourceExpr {}

    /** Minimum of multiple sources. source_max unchanged (e.g. 99). */
    record Min(List<ResourceLocation> ids) implements SourceExpr {}

    /** Average of multiple sources. source_max unchanged (e.g. 99). */
    record Avg(List<ResourceLocation> ids) implements SourceExpr {}

    Codec<SourceExpr> CODEC = Codec.either(
            ResourceLocation.CODEC.xmap(Single::new, s -> ((Single) s).id()),
            Codec.either(
                    RecordCodecBuilder.<Sum>create(inst -> inst.group(
                            ResourceLocation.CODEC.listOf().fieldOf("sum").forGetter(Sum::ids)
                    ).apply(inst, Sum::new)),
                    Codec.either(
                            RecordCodecBuilder.<Min>create(inst -> inst.group(
                                    ResourceLocation.CODEC.listOf().fieldOf("min").forGetter(Min::ids)
                            ).apply(inst, Min::new)),
                            RecordCodecBuilder.<Avg>create(inst -> inst.group(
                                    ResourceLocation.CODEC.listOf().fieldOf("avg").forGetter(Avg::ids)
                            ).apply(inst, Avg::new))
                    )
            )
    ).xmap(
            either -> either.map(
                    s -> (SourceExpr) s,
                    e -> e.map(
                            sum -> (SourceExpr) sum,
                            inner -> inner.map(min -> (SourceExpr) min, avg -> (SourceExpr) avg)
                    )
            ),
            expr -> expr instanceof Single s
                    ? com.mojang.datafixers.util.Either.left(s)
                    : com.mojang.datafixers.util.Either.right(
                            expr instanceof Sum sum ? com.mojang.datafixers.util.Either.left(sum)
                                    : expr instanceof Min min ? com.mojang.datafixers.util.Either.right(com.mojang.datafixers.util.Either.left(min))
                                    : com.mojang.datafixers.util.Either.right(com.mojang.datafixers.util.Either.right((Avg) expr))
                    )
    );

    /** Stable string for modifier ID. */
    default String toModifierIdPart() {
        return switch (this) {
            case Single s -> s.id().getPath();
            case Sum s -> "sum_" + s.ids().stream().map(ResourceLocation::getPath).collect(Collectors.joining("_"));
            case Min m -> "min_" + m.ids().stream().map(ResourceLocation::getPath).collect(Collectors.joining("_"));
            case Avg a -> "avg_" + a.ids().stream().map(ResourceLocation::getPath).collect(Collectors.joining("_"));
        };
    }
}
