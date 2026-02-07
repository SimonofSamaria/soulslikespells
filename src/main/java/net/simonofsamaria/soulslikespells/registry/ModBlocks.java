package net.simonofsamaria.soulslikespells.registry;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.simonofsamaria.soulslikespells.SoulslikeSpells;
import net.simonofsamaria.soulslikespells.block.BonfireBlock;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SoulslikeSpells.MODID);

    public static final DeferredBlock<BonfireBlock> BONFIRE = BLOCKS.register("bonfire",
            () -> new BonfireBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.STONE)
                    .lightLevel(state -> 15)
                    .noOcclusion()
            ));
}
