package net.simonofsamaria.soulslikespells.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Stub for future catalyst weapon implementation.
 * Catalysts will scale spell power based on the player's stat allocation.
 * Different catalysts favor different stat combinations (INT, FTH, ARC).
 */
public class CatalystItem extends Item {

    public CatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.soulslikespells.catalyst.tooltip"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // TODO Phase 9: Implement catalyst scaling logic
    // - Read player's INT/FTH/ARC stats
    // - Apply scaling to spell power based on catalyst type
    // - Integrate with Iron's Spells casting system
}
