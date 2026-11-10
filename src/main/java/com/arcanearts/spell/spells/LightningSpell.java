package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.SpellRegistry;
import com.arcanearts.spell.WandTier;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.random.RandomGenerator;

/** Молния небес: удар в точку прицела + цепь по 3 ближайшим целям. */
public class LightningSpell extends Spell {

    public LightningSpell() {
        super(ArcaneArts.id("lightning"), 28, 30);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        Vec3 target = SpellRegistry.aimVec(player, 40);

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(target.x, target.y, target.z);
            bolt.setCause(player);
            level.addFreshEntity(bolt);
        }

        level.playSound(null, target.x, target.y, target.z,
                SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 2.0F, 0.6F);

        // Цепная молния: до 3 ближайших существ вокруг точки удара
        List<LivingEntity> around = level.getEntitiesOfClass(LivingEntity.class,
                new AABB(target.add(-6, -3, -6), target.add(6, 6, 6)),
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        RandomGenerator rnd = level.random;
        int chained = 0;
        Vec3 prev = target;
        for (LivingEntity e : around) {
            if (chained >= 3) break;
            // визуальная дуга от предыдущей точки к цели
            ParticleHelper.line(level, prev, e.position(),
                    ModParticles.spark(0.65F, 0.85F, 1.0F, 0.5F), 6);
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, e.getX(), e.getY() + e.getBbHeight() * 0.5, e.getZ(),
                    10, 0.3, 0.4, 0.3, 0.15);
            e.hurt(level.damageSources().indirectMagic(bolt != null ? bolt : player, player), 5.0F * tier.power());
            prev = e.position();
            chained++;
        }

        // Проводник энергии от неба
        for (int i = 0; i < 8; i++) {
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    target.x + (rnd.nextDouble() - 0.5) * 2,
                    target.y + 10 + rnd.nextDouble() * 14,
                    target.z + (rnd.nextDouble() - 0.5) * 2, 2, 0, -0.6, 0, 0.05);
        }
        return true;
    }
}
