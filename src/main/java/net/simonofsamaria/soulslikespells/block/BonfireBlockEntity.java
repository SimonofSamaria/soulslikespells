package net.simonofsamaria.soulslikespells.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.simonofsamaria.soulslikespells.data.PlayerSoulData;
import net.simonofsamaria.soulslikespells.gui.bonfire.BonfireMenu;
import net.simonofsamaria.soulslikespells.registry.ModAttachments;
import net.simonofsamaria.soulslikespells.registry.ModBlockEntities;
import net.simonofsamaria.soulslikespells.registry.ModStatTypes;
import org.jetbrains.annotations.Nullable;

public class BonfireBlockEntity extends BlockEntity implements MenuProvider {

    public BonfireBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BONFIRE.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.soulslikespells.bonfire");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        PlayerSoulData soulData = player.getData(ModAttachments.PLAYER_SOUL_DATA.get());

        ContainerData data = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case BonfireMenu.DATA_SOUL_LEVEL -> soulData.getSoulLevel();
                    case BonfireMenu.DATA_EXPERIENCE -> soulData.getExperience();
                    case BonfireMenu.DATA_VIGOR -> soulData.getStatLevel(ModStatTypes.VIGOR_ID);
                    case BonfireMenu.DATA_MIND -> soulData.getStatLevel(ModStatTypes.MIND_ID);
                    case BonfireMenu.DATA_ENDURANCE -> soulData.getStatLevel(ModStatTypes.ENDURANCE_ID);
                    case BonfireMenu.DATA_STRENGTH -> soulData.getStatLevel(ModStatTypes.STRENGTH_ID);
                    case BonfireMenu.DATA_DEXTERITY -> soulData.getStatLevel(ModStatTypes.DEXTERITY_ID);
                    case BonfireMenu.DATA_INTELLIGENCE -> soulData.getStatLevel(ModStatTypes.INTELLIGENCE_ID);
                    case BonfireMenu.DATA_FAITH -> soulData.getStatLevel(ModStatTypes.FAITH_ID);
                    case BonfireMenu.DATA_ARCANE -> soulData.getStatLevel(ModStatTypes.ARCANE_ID);
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                // Read-only on server side
            }

            @Override
            public int getCount() {
                return BonfireMenu.DATA_COUNT;
            }
        };

        return new BonfireMenu(containerId, playerInventory, this.getBlockPos(), data);
    }
}
