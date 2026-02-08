package net.simonofsamaria.soulslikespells.event;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.simonofsamaria.soulslikespells.catalyst.CatalystModifierHelper;
import net.simonofsamaria.soulslikespells.catalyst.ScalingEntry;
import net.simonofsamaria.soulslikespells.catalyst.ScalingProfile;
import net.simonofsamaria.soulslikespells.catalyst.ScalingProfileManager;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModDataMaps;

import java.util.Optional;

/**
 * Adds catalyst scaling modifiers via ItemAttributeModifierEvent.
 * Uses ThreadLocal to obtain player context when the event is fired during equipment change.
 * Modifiers flow through the vanilla/NeoForge path and are applied/removed automatically.
 */
public class ItemAttributeModifierHandler {

    /**
     * Set by LivingEquipmentChangeEvent; cleared when no longer needed.
     * Only valid when ItemAttributeModifierEvent is fired during equipment processing.
     */
    private static final ThreadLocal<LivingEntity> EQUIPMENT_ENTITY = new ThreadLocal<>();

    public static void setEquipmentEntity(LivingEntity entity) {
        EQUIPMENT_ENTITY.set(entity);
    }

    public static void clearEquipmentEntity() {
        EQUIPMENT_ENTITY.remove();
    }

    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        LivingEntity entity = EQUIPMENT_ENTITY.get();
        if (!(entity instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        // Only add when stack is actually equipped (mainhand or offhand)
        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) return;

        var profileId = stack.getItemHolder().getData(ModDataMaps.ITEM_SCALING_PROFILE);
        if (profileId == null) return;

        ScalingProfile profile = ScalingProfileManager.getInstance().get(profileId);
        if (profile == null) return;

        PlayerSoulData soulData = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());
        EquipmentSlot slot = player.getMainHandItem() == stack ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        EquipmentSlotGroup group = slot == EquipmentSlot.MAINHAND ? EquipmentSlotGroup.MAINHAND : EquipmentSlotGroup.OFFHAND;

        for (ScalingEntry entry : profile.entries()) {
            double normalized = CatalystModifierHelper.getSourceValueNormalized(player, soulData, entry.source(), entry.sourceMax());
            double bonus = CatalystModifierHelper.computeBonus(entry, normalized);
            if (bonus <= 0) continue;

            Optional<Holder.Reference<net.minecraft.world.entity.ai.attributes.Attribute>> holderOpt =
                    CatalystModifierHelper.getAttributeHolder(entry.target());
            holderOpt.ifPresent(holder -> {
                ResourceLocation modifierId = CatalystModifierHelper.makeModifierId(profileId, entry, slot);
                AttributeModifier modifier = CatalystModifierHelper.buildModifier(modifierId, bonus, entry.operation());
                event.addModifier(holder, modifier, group);
            });
        }
    }
}
