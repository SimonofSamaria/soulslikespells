package net.simonofsamaria.soulslikespells.catalyst;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;

import java.util.List;
import java.util.Optional;

/**
 * Shared logic for computing and applying catalyst scaling modifiers.
 * Used by both ItemAttributeModifierEvent (via NeoForge item modifier flow) and
 * CatalystAttributeHandler (stats change refresh).
 *
 * <p>Source can be SLS stat (PlayerSoulData) or any attribute; target is always an attribute.
 */
public final class CatalystModifierHelper {

    /** Path prefix only; namespace is applied in fromNamespaceAndPath. */
    private static final String MODIFIER_PREFIX = "catalyst_";

    private CatalystModifierHelper() {}

    private static final String STAT_MODIFIER_PREFIX = "stat_";

    /**
     * Generates a unique modifier ID for a scaling entry and slot (catalyst).
     */
    public static ResourceLocation makeModifierId(ResourceLocation profileId, ScalingEntry entry, EquipmentSlot slot) {
        String path = MODIFIER_PREFIX + profileId.getNamespace() + "_" + profileId.getPath() + "_"
                + entry.source().toModifierIdPart() + "_" + entry.target().getPath() + "_" + slot.name().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, path);
    }

    /**
     * Generates a unique modifier ID for stat scaling (no slot).
     */
    public static ResourceLocation makeModifierIdForStat(ResourceLocation profileId, ScalingEntry entry) {
        String path = STAT_MODIFIER_PREFIX + profileId.getPath() + "_" + entry.source().toModifierIdPart() + "_" + entry.target().getPath();
        return ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, path);
    }

    private static double getSourceValueRaw(ServerPlayer player, PlayerSoulData soulData, ResourceLocation id) {
        Optional<Holder.Reference<Attribute>> attrHolder = getAttributeHolder(id);
        return attrHolder
                .map(h -> player.getAttributeValue(h))
                .orElse((double) soulData.getStatLevel(id));
    }

    private static double evaluateSourceExpr(ServerPlayer player, PlayerSoulData soulData, SourceExpr expr) {
        return switch (expr) {
            case SourceExpr.Single s -> getSourceValueRaw(player, soulData, s.id());
            case SourceExpr.Sum s -> s.ids().stream()
                    .mapToDouble(id -> getSourceValueRaw(player, soulData, id))
                    .sum();
            case SourceExpr.Min m -> m.ids().stream()
                    .mapToDouble(id -> getSourceValueRaw(player, soulData, id))
                    .min().orElse(0);
            case SourceExpr.Avg a -> a.ids().isEmpty() ? 0 : a.ids().stream()
                    .mapToDouble(id -> getSourceValueRaw(player, soulData, id))
                    .average().orElse(0);
        };
    }

    /**
     * Resolves source value (single or composite), normalized to 0..1.
     */
    public static double getSourceValueNormalized(ServerPlayer player, PlayerSoulData soulData,
                                                 SourceExpr source, double sourceMax) {
        double rawValue = evaluateSourceExpr(player, soulData, source);
        if (rawValue <= 0 || sourceMax <= 0) return 0;
        return Math.min(1.0, rawValue / sourceMax);
    }

    /**
     * Computes the bonus value for a scaling entry based on normalized source (0..1).
     */
    public static double computeBonus(ScalingEntry entry, double normalizedInput) {
        if (normalizedInput <= 0) return 0;
        List<CurveMath.CurveNode> curve = !entry.curveNodes().isEmpty()
                ? entry.curveNodes() : CurveMath.getCurve(entry.curve());
        double percentage = CurveMath.lerp(curve, normalizedInput);
        return percentage * entry.multiplier();
    }

    /**
     * Adds catalyst modifiers to the given player for the specified stack and slot.
     * Used when stats change (level up, respec) - equipment change uses ItemAttributeModifierEvent.
     */
    public static void applyModifiersForStack(ServerPlayer player, ItemStack stack, EquipmentSlot slot,
                                              ResourceLocation profileId, ScalingProfile profile, PlayerSoulData soulData) {
        for (ScalingEntry entry : profile.entries()) {
            double normalized = getSourceValueNormalized(player, soulData, entry.source(), entry.sourceMax());
            double bonus = computeBonus(entry, normalized);
            if (bonus <= 0) continue;

            getAttributeHolder(entry.target()).ifPresent(holder -> {
                AttributeInstance instance = player.getAttribute(holder);
                if (instance != null) {
                    ResourceLocation modifierId = makeModifierId(profileId, entry, slot);
                    instance.removeModifier(modifierId);
                    instance.addTransientModifier(new AttributeModifier(modifierId, bonus, entry.operation()));
                }
            });
        }
    }

    /**
     * Removes all catalyst modifiers for a scaling entry from the player.
     */
    public static void removeModifier(ServerPlayer player, ResourceLocation targetAttr, ResourceLocation modifierId) {
        getAttributeHolder(targetAttr).ifPresent(holder -> {
            AttributeInstance instance = player.getAttribute(holder);
            if (instance != null) instance.removeModifier(modifierId);
        });
    }

    public static Optional<Holder.Reference<Attribute>> getAttributeHolder(ResourceLocation targetAttr) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(
                ResourceKey.create(Registries.ATTRIBUTE, targetAttr)
        );
    }

    /**
     * Builds an AttributeModifier for a scaling entry; used by ItemAttributeModifierEvent.
     */
    public static AttributeModifier buildModifier(ResourceLocation modifierId, double bonus, AttributeModifier.Operation operation) {
        return new AttributeModifier(modifierId, bonus, operation);
    }

    /**
     * Applies stat-based modifiers (Mind, Dexterity etc.) to the player.
     * Used when stats change; profiles are loaded from scaling_profiles/stat_*.json.
     */
    public static void applyModifiersForStat(ServerPlayer player, ResourceLocation profileId,
                                             ScalingProfile profile, PlayerSoulData soulData) {
        for (ScalingEntry entry : profile.entries()) {
            double normalized = getSourceValueNormalized(player, soulData, entry.source(), entry.sourceMax());
            double bonus = computeBonus(entry, normalized);
            if (bonus <= 0) continue;

            getAttributeHolder(entry.target()).ifPresent(holder -> {
                AttributeInstance instance = player.getAttribute(holder);
                if (instance != null) {
                    ResourceLocation modifierId = makeModifierIdForStat(profileId, entry);
                    instance.removeModifier(modifierId);
                    instance.addTransientModifier(new AttributeModifier(modifierId, bonus, entry.operation()));
                }
            });
        }
    }
}
