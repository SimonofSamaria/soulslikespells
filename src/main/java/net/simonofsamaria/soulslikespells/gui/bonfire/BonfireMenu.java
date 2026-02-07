package net.simonofsamaria.soulslikespells.gui.bonfire;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.simonofsamaria.soulslikespells.registry.ModMenuTypes;

public class BonfireMenu extends AbstractContainerMenu {

    public static final int DATA_SOUL_LEVEL = 0;
    public static final int DATA_EXPERIENCE = 1;
    public static final int DATA_MIND = 2;
    public static final int DATA_DEXTERITY = 3;
    public static final int DATA_INTELLIGENCE = 4;
    public static final int DATA_FAITH = 5;
    public static final int DATA_ARCANE = 6;
    public static final int DATA_COUNT = 7;

    private final ContainerData data;
    private final BlockPos pos;

    /**
     * Client-side constructor called from network packet.
     */
    public static BonfireMenu fromNetwork(int containerId, Inventory inv, RegistryFriendlyByteBuf buf) {
        return new BonfireMenu(containerId, inv, buf.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    /**
     * Server-side constructor with live data.
     */
    public BonfireMenu(int containerId, Inventory inv, BlockPos pos, ContainerData data) {
        super(ModMenuTypes.BONFIRE_MENU.get(), containerId);
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
    }

    public int getSoulLevel() { return data.get(DATA_SOUL_LEVEL); }
    public int getExperience() { return data.get(DATA_EXPERIENCE); }
    public int getMind() { return data.get(DATA_MIND); }
    public int getDexterity() { return data.get(DATA_DEXTERITY); }
    public int getIntelligence() { return data.get(DATA_INTELLIGENCE); }
    public int getFaith() { return data.get(DATA_FAITH); }
    public int getArcane() { return data.get(DATA_ARCANE); }
    public int getStatValue(int dataIndex) { return data.get(dataIndex); }
    public BlockPos getPos() { return pos; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No inventory slots
    }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
