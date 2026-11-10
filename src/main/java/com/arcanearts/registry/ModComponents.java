package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModComponents {
    private ModComponents() {}

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ArcaneArts.MODID);

    /** Список выученных заклинаний на посохе */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> LEARNED_SPELLS =
            COMPONENTS.register("learned_spells", () -> DataComponentType.<List<ResourceLocation>>builder()
                    .persistent(ResourceLocation.CODEC.listOf())
                    .networkSynchronized(ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()))
                    .build());

    /** Выбранное заклинание */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> SELECTED_SPELL =
            COMPONENTS.register("selected_spell", () -> DataComponentType.<ResourceLocation>builder()
                    .persistent(ResourceLocation.CODEC)
                    .networkSynchronized(ResourceLocation.STREAM_CODEC)
                    .build());

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
