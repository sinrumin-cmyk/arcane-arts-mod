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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** Телепорт: мгновенный прыжок по взгляду. */
public class BlinkSpell extends Spell {

    public BlinkSpell() {
        super(ArcaneArts.id("blink"), 15, 15);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        Vec3 start = player.position();
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(12 + tier.power() * 2));

        BlockHitResult hit = level.clip(new ClipContext(eye, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 target = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();

        // Поднимаем точку, пока не освободится место
        Vec3 landing = target.subtract(player.getLookAngle().scale(0.4));
        for (int i = 0; i < 16 && !level.noCollision(player,
                player.getBoundingBox().move(landing.x, landing.y, landing.z)); i++) {
            landing = landing.add(0, 1, 0);
        }

        double dx = landing.x - start.x;
        double dz = landing.z - start.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 0.5) return false;

        player.teleportTo(level, landing.x, landing.y, landing.z, player.getYRot(), player.getXRot());
        player.resetFallDistance();
        player.setDeltaMovement(player.getDeltaMovement().multiply(1, 0, 1));

        // Шлейф телепорта между точками
        ParticleHelper.line(level, start.add(0, 1, 0), landing.add(0, 1, 0),
                ModParticles.spark(0.8F, 0.3F, 0.9F, 0.8F), 10);
        level.sendParticles(ParticleTypes.PORTAL, start.x, start.y + 1, start.z, 40, 0.3, 0.6, 0.3, 0.4);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, landing.x, landing.y + 1, landing.z, 30, 0.3, 0.6, 0.3, 0.4);
        ParticleHelper.spiral(level, landing.add(0, 0, 0), ModParticles.rune(0.75F, 0.25F, 0.9F, 0.9F), 10, 0.7, 2.4, 2);

        level.playSound(null, start.x, start.y, start.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.4F);
        level.playSound(null, landing.x, landing.y, landing.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.8F);
        return true;
    }
}
