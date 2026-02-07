package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.api.stat.StatType;

public class ModRegistries {
    public static final ResourceKey<Registry<StatType>> STAT_TYPE_KEY =
            ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "stat_type"));

    public static final Registry<StatType> STAT_TYPE_REGISTRY = new RegistryBuilder<>(STAT_TYPE_KEY)
            .sync(true)
            .create();

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(STAT_TYPE_REGISTRY);
    }
}
