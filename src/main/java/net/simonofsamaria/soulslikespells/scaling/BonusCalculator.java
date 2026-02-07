package net.simonofsamaria.soulslikespells.scaling;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.api.stat.StatScaling;

import java.util.Optional;

/**
 * Utility class for calculating and applying attribute modifier bonuses from stat scalings.
 */
public final class BonusCalculator {
    private BonusCalculator() {}

    /**
     * Apply a modifier to a player's attribute based on a scaling relationship and stat level.
     */
    public static void applyModifier(ServerPlayer player, StatScaling scaling, int statLevel) {
        double bonus = scaling.curve().calculateBonus(statLevel);
        ResourceLocation modifierId = getModifierId(scaling);

        Optional<Holder.Reference<Attribute>> holderOpt = BuiltInRegistries.ATTRIBUTE.getHolder(
                ResourceKey.create(Registries.ATTRIBUTE, scaling.targetAttribute())
        );

        holderOpt.ifPresent(holder -> {
            AttributeInstance instance = player.getAttribute(holder);
            if (instance != null) {
                instance.removeModifier(modifierId);
                if (bonus > 0) {
                    instance.addTransientModifier(new AttributeModifier(
                            modifierId, bonus, scaling.operation()
                    ));
                }
            }
        });
    }

    /**
     * Remove a modifier from a player's attribute.
     */
    public static void removeModifier(ServerPlayer player, StatScaling scaling) {
        ResourceLocation modifierId = getModifierId(scaling);

        Optional<Holder.Reference<Attribute>> holderOpt = BuiltInRegistries.ATTRIBUTE.getHolder(
                ResourceKey.create(Registries.ATTRIBUTE, scaling.targetAttribute())
        );

        holderOpt.ifPresent(holder -> {
            AttributeInstance instance = player.getAttribute(holder);
            if (instance != null) {
                instance.removeModifier(modifierId);
            }
        });
    }

    /**
     * Generate a unique modifier ID for a scaling relationship.
     */
    public static ResourceLocation getModifierId(StatScaling scaling) {
        String path = scaling.statId().getPath() + "_to_" +
                scaling.targetAttribute().getNamespace() + "_" +
                scaling.targetAttribute().getPath();
        return ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, path);
    }
}
