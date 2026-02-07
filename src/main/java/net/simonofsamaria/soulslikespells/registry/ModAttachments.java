package net.simonofsamaria.soulslikespells.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;

import java.util.function.Supplier;

public class ModAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SoulslikeSpells.MODID);

    public static final Supplier<AttachmentType<PlayerSoulData>> PLAYER_SOUL_DATA =
            ATTACHMENT_TYPES.register("player_soul_data", () ->
                    AttachmentType.builder(PlayerSoulData::new)
                            .serialize(PlayerSoulData.CODEC)
                            .copyOnDeath()
                            .build()
            );

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
