package net.simonofsamaria.soulslikespells.catalyst;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModDataMaps;

import java.util.List;

/**
 * Applies catalyst scaling modifiers when player stats change (level up, respec).
 * Equipment change uses ItemAttributeModifierEvent for the vanilla modifier flow.
 */
public final class CatalystAttributeHandler {

    private CatalystAttributeHandler() {}

    /**
     * Recalculate and apply catalyst modifiers. Call when stats change.
     */
    public static void applyCatalystModifiers(ServerPlayer player) {
        removeAllCatalystModifiers(player);

        PlayerSoulData soulData = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());

        for (EquipmentSlot slot : List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND)) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;

            ResourceLocation profileId = stack.getItemHolder().getData(ModDataMaps.ITEM_SCALING_PROFILE);
            if (profileId == null) continue;

            ScalingProfile profile = ScalingProfileManager.getInstance().get(profileId);
            if (profile == null) continue;

            CatalystModifierHelper.applyModifiersForStack(player, stack, slot, profileId, profile, soulData);
        }
    }

    private static void removeAllCatalystModifiers(ServerPlayer player) {
        for (var entry : ScalingProfileManager.getInstance().getAll().entrySet()) {
            for (ScalingEntry scalingEntry : entry.getValue().entries()) {
                CatalystModifierHelper.removeModifier(player, scalingEntry.target(),
                        CatalystModifierHelper.makeModifierId(entry.getKey(), scalingEntry, EquipmentSlot.MAINHAND));
                CatalystModifierHelper.removeModifier(player, scalingEntry.target(),
                        CatalystModifierHelper.makeModifierId(entry.getKey(), scalingEntry, EquipmentSlot.OFFHAND));
            }
        }
    }
}
