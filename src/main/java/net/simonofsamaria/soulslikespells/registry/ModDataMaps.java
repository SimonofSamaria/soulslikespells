package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

/**
 * Data maps for attaching data-driven values to registry objects.
 */
public class ModDataMaps {

    /**
     * Attaches a ScalingProfile ID to items (catalysts, staffs).
     * Values in: soulslikespells/data_maps/item/scaling_profile.json
     */
    public static final DataMapType<Item, ResourceLocation> ITEM_SCALING_PROFILE = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(SoulslikeSpells.MODID, "scaling_profile"),
            Registries.ITEM,
            ResourceLocation.CODEC
    ).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ITEM_SCALING_PROFILE);
    }
}
