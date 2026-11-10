package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import com.arcanearts.entity.MagicBoltEntity;
import com.arcanearts.entity.MeteorEntity;
import com.arcanearts.entity.SingularityEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ArcaneArts.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MagicBoltEntity>> MAGIC_BOLT =
            ENTITIES.register("magic_bolt", () -> EntityType.Builder.<MagicBoltEntity>of(MagicBoltEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("magic_bolt"));

    public static final DeferredHolder<EntityType<?>, EntityType<MeteorEntity>> METEOR =
            ENTITIES.register("meteor", () -> EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, MobCategory.MISC)
                    .sized(2.0F, 2.0F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .fireImmune()
                    .build("meteor"));

    public static final DeferredHolder<EntityType<?>, EntityType<SingularityEntity>> SINGULARITY =
            ENTITIES.register("singularity", () -> EntityType.Builder.<SingularityEntity>of(SingularityEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.2F)
                    .clientTrackingRange(10)
                    .updateInterval(2)
                    .build("singularity"));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
