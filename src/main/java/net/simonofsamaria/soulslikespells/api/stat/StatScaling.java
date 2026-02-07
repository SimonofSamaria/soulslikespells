package net.simonofsamaria.soulslikespells.api.stat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Defines a single scaling relationship between a stat type and an attribute.
 * For example: Intelligence -> spell_power with a specific curve.
 */
public record StatScaling(
        ResourceLocation statId,
        ResourceLocation targetAttribute,
        AttributeModifier.Operation operation,
        ScalingCurve curve
) {}
