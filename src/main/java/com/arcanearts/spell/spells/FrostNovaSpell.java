package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.WandTier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Ледяная нова: кольцо холода вокруг мага — урон и замедление. */
public class FrostNovaSpell extends Spell {

    public FrostNovaSpell() {
        super(ArcaneArts.id("frost"), 24, 40);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        double radius = 5.5D + tier.power();
        Vec3 center = player.position();

        // Расширяющиеся кольца льда
        for (double r = 1.0; r <= radius; r += 1.2) {
            int pts = (int) (10 + r * 4);
            for (int i = 0; i < pts; i++) {
                double a = Math.PI * 2 * i / pts;
                level.sendParticles(ParticleTypes.SNOWFLAKE,
                        center.x + Math.cos(a) * r,
                        center.y + 0.4 + level.random.nextDouble() * 0.4,
                        center.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
            }
        }
        for (int i = 0; i < 40; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double rr = level.random.nextDouble() * radius;
            level.sendParticles(ModParticles.spark(0.4F, 0.8F, 1.0F, 0.7F),
                    center.x + Math.cos(a) * rr, center.y + 0.2, center.z + Math.sin(a) * rr,
                    1, 0, 0.05, 0, 0);
        }
        // Ледяная коруна над головой
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12;
            level.sendParticles(ModParticles.rune(0.55F, 0.85F, 1.0F, 0.8F),
                    center.x + Math.cos(a) * 0.8, center.y + 1.8, center.z + Math.sin(a) * 0.8, 1, 0, 0, 0, 0);
        }

        List<LivingEntity> hit = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.add(-radius, -1.5, -radius), center.add(radius, 3, radius)),
                e -> e != player && e.isAlive());
        for (LivingEntity e : hit) {
            e.hurt(level.damageSources().indirectMagic(player, player), 6.0F * tier.power());
            e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            e.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 1));
            e.setTicksFrozen(140);
        }

        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.4F, 0.4F);
        level.playSound(null, center.x, center.y, center.z,
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 0.5F);
        return true;
    }
}
