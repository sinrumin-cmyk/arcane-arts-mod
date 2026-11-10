package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.entity.MagicBoltEntity;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.WandTier;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Огненный шар — летит медленнее, но взрывается и поджигает. */
public class FireballSpell extends Spell {

    public FireballSpell() {
        super(ArcaneArts.id("fireball"), 22, 18);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        MagicBoltEntity ball = new MagicBoltEntity(level, player, 3.0F * tier.power(), 1.6F * tier.power(), true, 0xFF7B24);
        ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.2F, 1.0F);
        level.addFreshEntity(ball);

        Vec3 muzzle = player.getEyePosition().add(player.getLookAngle().scale(1.2D));
        level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 12, 0.2, 0.2, 0.2, 0.05);
        ParticleHelper.burst(level, muzzle, ModParticles.spark(1.0F, 0.45F, 0.1F, 0.9F), 8, 0.3, level.random);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 0.8F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_IGNITE, SoundSource.PLAYERS, 0.8F, 0.7F);
        return true;
    }
}
