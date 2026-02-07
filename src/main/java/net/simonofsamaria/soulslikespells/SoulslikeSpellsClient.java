package net.simonofsamaria.soulslikespells;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.simonofsamaria.soulslikespells.gui.bonfire.BonfireScreen;
import net.simonofsamaria.soulslikespells.gui.overlay.SoulBarOverlay;
import net.simonofsamaria.soulslikespells.registry.ModMenuTypes;

@Mod(value = SoulslikeSpells.MODID, dist = Dist.CLIENT)
public class SoulslikeSpellsClient {

    public SoulslikeSpellsClient(IEventBus modEventBus, ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::registerScreens);
        modEventBus.addListener(this::registerGuiLayers);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.BONFIRE_MENU.get(), BonfireScreen::new);
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        SoulBarOverlay.register(event);
    }
}
