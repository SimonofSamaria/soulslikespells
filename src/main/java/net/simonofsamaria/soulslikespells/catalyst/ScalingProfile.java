package net.simonofsamaria.soulslikespells.catalyst;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * Data-driven catalyst scaling profile.
 * Defines how an item (staff/catalyst) scales its attributes based on player stats.
 */
public record ScalingProfile(List<ScalingEntry> entries) {
    public static final Codec<ScalingProfile> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ScalingEntry.CODEC.listOf().fieldOf("entries").forGetter(ScalingProfile::entries)
            ).apply(instance, ScalingProfile::new)
    );
}
