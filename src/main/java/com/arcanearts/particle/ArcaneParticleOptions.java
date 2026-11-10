package com.arcanearts.particle;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Опции кастомной частицы: цвет (r,g,b) и масштаб.
 * Используется всеми тремя частицами мода (spark / rune / ring).
 */
public class ArcaneParticleOptions implements ParticleOptions {
    private final ParticleType<ArcaneParticleOptions> type;
    private final float r;
    private final float g;
    private final float b;
    private final float scale;

    public ArcaneParticleOptions(ParticleType<ArcaneParticleOptions> type, float r, float g, float b, float scale) {
        this.type = type;
        this.r = r;
        this.g = g;
        this.b = b;
        this.scale = scale;
    }

    /** MapCodec, привязанный к конкретному типу частицы */
    public static MapCodec<ArcaneParticleOptions> codecFor(ParticleType<ArcaneParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.FLOAT.fieldOf("r").forGetter(o -> o.r),
                Codec.FLOAT.fieldOf("g").forGetter(o -> o.g),
                Codec.FLOAT.fieldOf("b").forGetter(o -> o.b),
                Codec.FLOAT.fieldOf("scale").forGetter(o -> o.scale)
        ).apply(inst, (r, g, b, s) -> new ArcaneParticleOptions(type, r, g, b, s)));
    }

    public float r() { return r; }
    public float g() { return g; }
    public float b() { return b; }
    public float scale() { return scale; }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void writeToStream(RegistryFriendlyByteBuf buffer) {
        buffer.writeFloat(r);
        buffer.writeFloat(g);
        buffer.writeFloat(b);
        buffer.writeFloat(scale);
    }
}
