package com.arcanearts.spell.spells;

import com.arcanearts.ArcaneArts;
import com.arcanearts.entity.SingularityEntity;
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

/** Сингулярность: чёрная дыра, затягивающая врагов, затем имплодирует. */
public class SingularitySpell extends Spell {

    public SingularitySpell() {
        super(ArcaneArts.id("singularity"), 70, 200);
    }

    @Override
    public boolean cast(ServerPlayer player, ServerLevel level, WandTier tier) {
        Vec3 target = SpellRegistry.aimVec(player, 24);

        SingularityEntity hole = new SingularityEntity(level, player, 3.0F * tier.power());
        hole.setPos(target.x, target.y, target.z);
        level.addFreshEntity(hole);

        // Расходящееся искажение при открытии
        for (int i = 0; i < 30; i++) {
            double a = level.random.nextDouble() * Math.PI * 2;
            double r = 1 + level.random.nextDouble() * 3;
            level.sendParticles(ModParticles.ring(0.6F, 0.2F, 0.9F, 1.2F),
                    target.x + Math.cos(a) * r, target.y + level.random.nextDouble() * 2, target.z + Math.sin(a) * r,
                    1, 0, 0, 0, 0);
        }

        level.playSound(null, target.x, target.y, target.z,
                SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.6F, 1.4F);
        return true;
    }
}
