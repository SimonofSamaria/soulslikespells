package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.block.BonfireBlockEntity;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SoulslikeSpells.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BonfireBlockEntity>> BONFIRE =
            BLOCK_ENTITIES.register("bonfire", () ->
                    BlockEntityType.Builder.of(BonfireBlockEntity::new, ModBlocks.BONFIRE.get())
                            .build(null)
            );
}
