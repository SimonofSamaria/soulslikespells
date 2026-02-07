package net.simonofsamaria.soulslikespells.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;

public class ModNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(SoulslikeSpells.MODID).versioned("1.0.0");

        // C2S: Client confirms level-up (pending allocations)
        registrar.playToServer(
                ConfirmLevelUpPayload.TYPE,
                ConfirmLevelUpPayload.STREAM_CODEC,
                ConfirmLevelUpPayload::handle
        );

        // S2C: Server syncs soul data to client
        registrar.playToClient(
                SyncSoulDataPayload.TYPE,
                SyncSoulDataPayload.STREAM_CODEC,
                SyncSoulDataPayload::handle
        );

        // C2S: Client requests to open respec (diamond check)
        registrar.playToServer(
                RespecRequestPayload.TYPE,
                RespecRequestPayload.STREAM_CODEC,
                RespecRequestPayload::handle
        );

        // S2C: Server allows respec
        registrar.playToClient(
                RespecAllowedPayload.TYPE,
                RespecAllowedPayload.STREAM_CODEC,
                RespecAllowedPayload::handle
        );

        // S2C: Server denies respec
        registrar.playToClient(
                RespecDeniedPayload.TYPE,
                RespecDeniedPayload.STREAM_CODEC,
                RespecDeniedPayload::handle
        );

        // C2S: Client applies respec (consumes diamond)
        registrar.playToServer(
                RespecApplyPayload.TYPE,
                RespecApplyPayload.STREAM_CODEC,
                RespecApplyPayload::handle
        );
    }
}
