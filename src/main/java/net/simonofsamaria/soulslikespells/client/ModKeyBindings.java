package net.simonofsamaria.soulslikespells.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.gui.SoulStatsScreen;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = SoulslikeSpells.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyBindings {

    public static KeyMapping OPEN_SOUL_STATS;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        OPEN_SOUL_STATS = new KeyMapping(
                "key.soulslikespells.open_soul_stats",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "key.categories.soulslikespells"
        );
        event.register(OPEN_SOUL_STATS);
    }

    @EventBusSubscriber(modid = SoulslikeSpells.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class KeyHandler {
        @SubscribeEvent
        public static void onClientTick(ClientTickEvent.Post event) {
            Minecraft mc = Minecraft.getInstance();
            while (OPEN_SOUL_STATS != null && OPEN_SOUL_STATS.consumeClick()) {
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new SoulStatsScreen());
                }
            }
        }
    }
}
