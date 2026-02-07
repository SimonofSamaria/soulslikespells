package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SoulslikeSpells.MODID);

    public static final DeferredItem<BlockItem> BONFIRE = ITEMS.register("bonfire",
            () -> new BlockItem(ModBlocks.BONFIRE.get(), new Item.Properties()));

    // TODO Phase 9: Register catalyst items
    // TODO Phase 9: Register ring items (Curios)
}
