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
import net.minecraft.world.phys.Vec3;

/** Исцеление: мгновенно восстанавливает здоровье и даёт регенерацию. */
public class HealSpell extends Spell {

    public HealSpell() {
        super(ArcaneArts.id("heal"), 30, 100);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        player.heal(8.0F * tier.power());
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 4, 0));

        Vec3 center = player.position().add(0, 1, 0);
        ParticleHelper.sphere(level, center, ModParticles.spark(0.3F, 1.0F, 0.5F, 0.6F), 26, 1.3, level.random);
        for (int i = 0; i < 14; i++) {
            level.sendParticles(ParticleTypes.HEART,
                    player.getX() + (level.random.nextDouble() - 0.5) * 1.6,
                    player.getY() + 1.2 + level.random.nextDouble() * 1.2,
                    player.getZ() + (level.random.nextDouble() - 0.5) * 1.6, 1, 0, 0.02, 0, 0);
        }
        ParticleHelper.spiral(level, player.position(), ModParticles.rune(0.5F, 1.0F, 0.7F, 0.8F), 16, 0.9, 2.5, 2.5);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.9F, 1.6F);
        return true;
    }
}
