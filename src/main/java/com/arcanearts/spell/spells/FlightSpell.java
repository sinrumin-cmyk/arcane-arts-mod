package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.mana.PlayerMana;
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

/** Полёт: 20 секунд свободного полёта, затем мягкое падение. */
public class FlightSpell extends Spell {

    public FlightSpell() {
        super(ArcaneArts.id("flight"), 50, 400);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        int seconds = (int) (12 + 8 * tier.power());
        PlayerMana mana = PlayerMana.get(player);
        mana.setFlightTicks(seconds * 20);

        var abilities = player.getAbilities();
        boolean wasFlying = abilities.flying;
        abilities.mayfly = true;
        abilities.flying = true;
        player.onUpdateAbilities();

        // Мягкое приземление после полёта
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, seconds * 20 + 100, 0));

        // Эффект взлёта: вихри и облачная подушка
        ParticleHelper.ring(level, player.position(), ModParticles.ring(0.5F, 0.75F, 1.0F, 1.4F), 24, 1.8, 0.05);
        ParticleHelper.spiral(level, player.position(), ModParticles.spark(0.6F, 0.85F, 1.0F, 0.7F), 20, 0.9, 4, 3);
        level.sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.2, player.getZ(), 20, 0.5, 0.1, 0.5, 0.02);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.5F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 1.2F, 0.8F);

        if (!wasFlying) {
            player.setDeltaMovement(player.getDeltaMovement().add(0, 0.6, 0)); // лёгкий подскок
        }
        return true;
    }
}
