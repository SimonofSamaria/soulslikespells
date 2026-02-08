package net.simonofsamaria.soulslikespells.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

/**
 * Handles catalyst scaling when equipment changes.
 * Sets ThreadLocal so ItemAttributeModifierEvent can add modifiers through the vanilla/NeoForge flow.
 * Stats change (level up, respec) is handled by CatalystAttributeHandler.
 */
public class CatalystEventHandler {

    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemAttributeModifierHandler.setEquipmentEntity(player);
            // Modifiers flow via ItemAttributeModifierEvent during vanilla's forEachModifier.
        }
    }
}
