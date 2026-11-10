package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import com.arcanearts.particle.ArcaneParticleOptions;
import com.arcanearts.particle.ArcaneParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    private ModParticles() {}

    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ArcaneArts.MODID);

    /** Магическая искра — светящаяся точка (тинтуется цветом) */
    public static final DeferredHolder<ParticleType<?>, ArcaneParticleType> ARCANE_SPARK =
            PARTICLES.register("arcane_spark", ArcaneParticleType::new);

    /** Руна — вращающийся глиф (тинтуется цветом) */
    public static final DeferredHolder<ParticleType<?>, ArcaneParticleType> ARCANE_RUNE =
            PARTICLES.register("arcane_rune", ArcaneParticleType::new);

    /** Кольцо — мягкое светящееся кольцо (тинтуется цветом) */
    public static final DeferredHolder<ParticleType<?>, ArcaneParticleType> ARCANE_RING =
            PARTICLES.register("arcane_ring", ArcaneParticleType::new);

    public static ArcaneParticleOptions spark(float r, float g, float b, float scale) {
        return new ArcaneParticleOptions(ARCANE_SPARK.get(), r, g, b, scale);
    }

    public static ArcaneParticleOptions rune(float r, float g, float b, float scale) {
        return new ArcaneParticleOptions(ARCANE_RUNE.get(), r, g, b, scale);
    }

    public static ArcaneParticleOptions ring(float r, float g, float b, float scale) {
        return new ArcaneParticleOptions(ARCANE_RING.get(), r, g, b, scale);
    }

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}
