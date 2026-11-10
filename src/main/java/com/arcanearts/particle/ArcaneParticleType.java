package com.arcanearts.particle;

import com.mojang.brigadier.StringReader;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.MapCodec;

/**
 * Тип частицы с цветом и масштабом. Сетевые данные: float r,g,b + float scale.
 */
public class ArcaneParticleType extends ParticleType<ArcaneParticleOptions> {

    private static final ParticleOptions.Deserializer<ArcaneParticleOptions> DESERIALIZER =
            new ParticleOptions.Deserializer<>() {
                @Override
                public ArcaneParticleOptions fromCommand(ParticleType<ArcaneParticleOptions> type, StringReader reader)
                        throws CommandSyntaxException {
                    reader.expect(' ');
                    float r = (float) reader.readDouble();
                    reader.expect(' ');
                    float g = (float) reader.readDouble();
                    reader.expect(' ');
                    float b = (float) reader.readDouble();
                    reader.expect(' ');
                    float scale = (float) reader.readDouble();
                    return new ArcaneParticleOptions(type, r, g, b, scale);
                }

                @Override
                public ArcaneParticleOptions fromNetwork(ParticleType<ArcaneParticleOptions> type, RegistryFriendlyByteBuf buf) {
                    return new ArcaneParticleOptions(type, buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
                }
            };

    private final MapCodec<ArcaneParticleOptions> codec = ArcaneParticleOptions.codecFor(this);

    public ArcaneParticleType() {
        super(false, DESERIALIZER);
    }

    @Override
    public MapCodec<ArcaneParticleOptions> codec() {
        return codec;
    }
}
