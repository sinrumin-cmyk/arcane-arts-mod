package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import com.arcanearts.block.ArcaneAltarBlock;
import com.arcanearts.block.ManaOreBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    private ModBlocks() {}

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ArcaneArts.MODID);

    public static final DeferredBlock<Block> ARCANE_ALTAR = BLOCKS.register("arcane_altar",
            () -> new ArcaneAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 5)
                    .noOcclusion()
                    .sound(SoundType.AMETHYST)));

    public static final DeferredBlock<Block> MANA_ORE = BLOCKS.register("mana_ore",
            () -> new ManaOreBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(3.0F, 3.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 7)
                    .sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> MANA_CRYSTAL_BLOCK = BLOCKS.register("mana_crystal_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_CYAN)
                    .strength(5.0F, 1200.0F)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 15)
                    .sound(SoundType.AMETHYST)));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
