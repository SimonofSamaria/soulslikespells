package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.gui.bonfire.BonfireMenu;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, SoulslikeSpells.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<BonfireMenu>> BONFIRE_MENU =
            MENU_TYPES.register("bonfire", () ->
                    IMenuTypeExtension.create(BonfireMenu::fromNetwork)
            );
}
