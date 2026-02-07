package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SoulslikeSpells.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SOULSLIKE_TAB =
            CREATIVE_TABS.register("soulslike_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.soulslikespells"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> ModItems.BONFIRE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BONFIRE.get());
                    })
                    .build()
            );
}
