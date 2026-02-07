package net.simonofsamaria.soulslikespells.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

public class ModNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SoulslikeSpells.MODID).versioned("1.0.0");

        // C2S: Client requests to level up a stat
        registrar.playToServer(
                LevelUpStatPayload.TYPE,
                LevelUpStatPayload.STREAM_CODEC,
                LevelUpStatPayload::handle
        );

        // S2C: Server syncs soul data to client
        registrar.playToClient(
                SyncSoulDataPayload.TYPE,
                SyncSoulDataPayload.STREAM_CODEC,
                SyncSoulDataPayload::handle
        );

        // C2S: Client requests respec
        registrar.playToServer(
                RespecPayload.TYPE,
                RespecPayload.STREAM_CODEC,
                RespecPayload::handle
        );
    }
}
