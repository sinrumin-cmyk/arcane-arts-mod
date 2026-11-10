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

/** Ускорение: скорость, спешка и прыгучесть. */
public class HasteSpell extends Spell {

    public HasteSpell() {
        super(ArcaneArts.id("haste"), 25, 200);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        int duration = 400;
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1 + (int) (tier.power() * 0.5)));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 1));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 1));

        ParticleHelper.ring(level, player.position(), ModParticles.ring(1.0F, 0.9F, 0.25F, 1.0F), 20, 1.6, 0.05);
        ParticleHelper.spiral(level, player.position(), ModParticles.spark(1.0F, 0.95F, 0.3F, 0.5F), 24, 0.8, 3, 3);
        level.sendParticles(ParticleTypes.CRIT,
                player.getX(), player.getY() + 1, player.getZ(), 16, 0.4, 0.8, 0.4, 0.1);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.8F);
        return true;
    }
}
