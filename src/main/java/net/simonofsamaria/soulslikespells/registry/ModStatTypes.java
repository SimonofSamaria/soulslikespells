package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.api.stat.StatType;

public class ModStatTypes {
    public static final DeferredRegister<StatType> STAT_TYPES =
            DeferredRegister.create(ModRegistries.STAT_TYPE_KEY, SoulslikeSpells.MODID);

    public static final DeferredHolder<StatType, StatType> VIGOR =
            STAT_TYPES.register("vigor", () -> new StatType(99, 0, 0, 0));

    public static final DeferredHolder<StatType, StatType> MIND =
            STAT_TYPES.register("mind", () -> new StatType(99, 0, 16, 0));

    public static final DeferredHolder<StatType, StatType> ENDURANCE =
            STAT_TYPES.register("endurance", () -> new StatType(99, 0, 32, 0));

    public static final DeferredHolder<StatType, StatType> STRENGTH =
            STAT_TYPES.register("strength", () -> new StatType(99, 0, 48, 0));

    public static final DeferredHolder<StatType, StatType> DEXTERITY =
            STAT_TYPES.register("dexterity", () -> new StatType(99, 0, 64, 0));

    public static final DeferredHolder<StatType, StatType> INTELLIGENCE =
            STAT_TYPES.register("intelligence", () -> new StatType(99, 0, 80, 0));

    public static final DeferredHolder<StatType, StatType> FAITH =
            STAT_TYPES.register("faith", () -> new StatType(99, 0, 96, 0));

    public static final DeferredHolder<StatType, StatType> ARCANE =
            STAT_TYPES.register("arcane", () -> new StatType(99, 0, 112, 0));

    // Convenience ID constants
    public static final ResourceLocation VIGOR_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "vigor");
    public static final ResourceLocation MIND_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "mind");
    public static final ResourceLocation ENDURANCE_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "endurance");
    public static final ResourceLocation STRENGTH_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "strength");
    public static final ResourceLocation DEXTERITY_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "dexterity");
    public static final ResourceLocation INTELLIGENCE_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "intelligence");
    public static final ResourceLocation FAITH_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "faith");
    public static final ResourceLocation ARCANE_ID = ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "arcane");
}
