package net.simonofsamaria.soulslikespells;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.simonofsamaria.soulslikespells.command.SoulslikeCommands;
import net.simonofsamaria.soulslikespells.config.SoulslikeClientConfig;
import net.simonofsamaria.soulslikespells.config.SoulslikeCommonConfig;
import net.simonofsamaria.soulslikespells.event.AttributeEventHandler;
import net.simonofsamaria.soulslikespells.event.PlayerDeathHandler;
import net.simonofsamaria.soulslikespells.network.ModNetworking;
import net.simonofsamaria.soulslikespells.registry.*;
import org.slf4j.Logger;

@Mod(SoulslikeSpells.MODID)
public class SoulslikeSpells {
    public static final String MODID = "soulslikespells";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SoulslikeSpells(IEventBus modEventBus, ModContainer modContainer) {
        // Register all deferred registers to the mod event bus
        ModStatTypes.STAT_TYPES.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);

        // Register custom registry
        modEventBus.addListener(ModRegistries::registerRegistries);
        modEventBus.addListener(net.simonofsamaria.soulslikespells.registry.ModDataMaps::register);

        // Register network handlers
        modEventBus.addListener(ModNetworking::register);

        // Register game event listeners
        NeoForge.EVENT_BUS.addListener(SoulslikeCommands::register);
        NeoForge.EVENT_BUS.addListener(AttributeEventHandler::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(AttributeEventHandler::onPlayerRespawn);
        NeoForge.EVENT_BUS.addListener(AttributeEventHandler::onPlayerChangeDimension);
        NeoForge.EVENT_BUS.addListener(PlayerDeathHandler::onPlayerDeath);
        NeoForge.EVENT_BUS.addListener(net.simonofsamaria.soulslikespells.event.CatalystEventHandler::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(net.simonofsamaria.soulslikespells.event.ItemAttributeModifierHandler::onItemAttributeModifier);
        NeoForge.EVENT_BUS.addListener((AddReloadListenerEvent event) ->
                event.addListener(new net.simonofsamaria.soulslikespells.catalyst.ScalingProfileReloadListener()));

        // Register configs
        modContainer.registerConfig(ModConfig.Type.COMMON, SoulslikeCommonConfig.SPEC);
        modContainer.registerConfig(ModConfig.Type.CLIENT, SoulslikeClientConfig.SPEC);

        LOGGER.info("SoulslikeSpells initialized - Prepare to die.");
    }
}
