package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.entity.MagicBoltEntity;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.WandTier;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Арканный болт — быстрый магический снаряд. Базовое заклинание всех посохов. */
public class ArcaneBoltSpell extends Spell {

    public ArcaneBoltSpell() {
        super(ArcaneArts.id("arcane_bolt"), 8, 6);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        MagicBoltEntity bolt = new MagicBoltEntity(level, player, 5.0F * tier.power(), 0.0F, false, tier.boltColor());
        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.4F, 0.5F);
        level.addFreshEntity(bolt);

        // Вспышка у кончика посоха
        Vec3 muzzle = player.getEyePosition().add(player.getLookAngle().scale(1.2D));
        float r = ((tier.boltColor() >> 16) & 0xFF) / 255F;
        float g = ((tier.boltColor() >> 8) & 0xFF) / 255F;
        float b = (tier.boltColor() & 0xFF) / 255F;
        ParticleHelper.burst(level, muzzle, ModParticles.spark(r, g, b, 0.5F), 10, 0.35, level.random);
        ParticleHelper.burst(level, muzzle, ModParticles.rune(r, g, b, 0.8F), 3, 0.1, level.random);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.5F, 1.6F);
        return true;
    }
}
