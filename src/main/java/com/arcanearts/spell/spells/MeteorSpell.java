package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.entity.MeteorEntity;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.SpellRegistry;
import com.arcanearts.spell.WandTier;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

/** Метеор: камень из небес обрушивается на точку прицела. */
public class MeteorSpell extends Spell {

    public MeteorSpell() {
        super(ArcaneArts.id("meteor"), 60, 120);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        Vec3 target = SpellRegistry.aimVec(player, 64);
        Vec3 spawn = target.add(0, 30, 0);

        MeteorEntity meteor = new MeteorEntity(level, player, 5.0F * tier.power());
        meteor.setPos(spawn.x, spawn.y, spawn.z);
        Vec3 dir = target.subtract(spawn).normalize();
        meteor.setDeltaMovement(dir.scale(1.5D));
        meteor.setYRot(player.getYRot());
        level.addFreshEntity(meteor);

        // Круг призыва под целью
        for (int ring = 0; ring < 3; ring++) {
            ParticleHelper.ring(level, target, ModParticles.ring(1.0F, 0.6F, 0.15F, 1.0F + ring * 0.6F),
                    16, 1.5 + ring * 0.8, 0.1 + ring * 0.1);
        }
        ParticleHelper.line(level, spawn, target, ModParticles.spark(1.0F, 0.7F, 0.2F, 1.4F), 14);

        level.playSound(null, target.x, target.y, target.z,
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5F, 0.3F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.2F, 0.4F);
        return true;
    }
}
