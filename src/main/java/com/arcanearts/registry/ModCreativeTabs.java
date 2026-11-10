package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    private ModCreativeTabs() {}

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArcaneArts.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARCANE_TAB =
            TABS.register("arcane", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.arcanearts"))
                    .icon(() -> new ItemStack(ModItems.MYSTIC_WAND.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.APPRENTICE_WAND.get());
                        output.accept(ModItems.MYSTIC_WAND.get());
                        output.accept(ModItems.ARCHMAGE_WAND.get());
                        output.accept(ModItems.MANA_DUST.get());
                        output.accept(ModItems.MANA_CRYSTAL.get());
                        output.accept(ModBlocks.MANA_ORE.get().asItem());
                        output.accept(ModBlocks.MANA_CRYSTAL_BLOCK.get().asItem());
                        output.accept(ModBlocks.ARCANE_ALTAR.get().asItem());
                        output.accept(ModItems.FIREBALL_TOME.get());
                        output.accept(ModItems.LIGHTNING_TOME.get());
                        output.accept(ModItems.FROST_TOME.get());
                        output.accept(ModItems.METEOR_TOME.get());
                        output.accept(ModItems.SINGULARITY_TOME.get());
                        output.accept(ModItems.BLINK_TOME.get());
                        output.accept(ModItems.SHIELD_TOME.get());
                        output.accept(ModItems.HEAL_TOME.get());
                        output.accept(ModItems.HASTE_TOME.get());
                        output.accept(ModItems.FLIGHT_TOME.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
