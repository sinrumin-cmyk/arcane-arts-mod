package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.WandTier;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Щит маны: поглощение урона и сопротивление. */
public class ManaShieldSpell extends Spell {

    public ManaShieldSpell() {
        super(ArcaneArts.id("shield"), 35, 300);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        int duration = 200;
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 3 + (int) tier.power()));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1));

        // Круг приземления + восходящие рунические кольца
        ParticleHelper.ring(level, player.position(), ModParticles.ring(0.4F, 0.85F, 1.0F, 1.2F), 18, 1.4, 0.05);
        ParticleHelper.ring(level, player.position(), ModParticles.rune(0.55F, 0.9F, 1.0F, 1.0F), 8, 1.0, 2.5);
        ParticleHelper.sphere(level, player.position().add(0, 1, 0),
                ModParticles.spark(0.65F, 0.95F, 1.0F, 0.8F), 24, 1.2, level.random);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(), 8, 0.5, 0.9, 0.5, 0.05);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.3F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 0.9F);
        return true;
    }
}
