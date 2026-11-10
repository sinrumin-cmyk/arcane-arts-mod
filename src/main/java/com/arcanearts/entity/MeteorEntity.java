package com.arcanearts.entity;

import com.arcanearts.network.ModNetwork;
import com.arcanearts.registry.ModEntities;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.random.RandomGenerator;

/**
 * Метеор: падает с неба на цель, при ударе — эпичный взрыв,
 * огненные кольца и тряска камеры у близких игроков.
 */
public class MeteorEntity extends Projectile {

    private float power = 5.0F;
    private boolean impacted = false;

    public MeteorEntity(EntityType<? extends MeteorEntity> type, Level level) {
        super(type, level);
    }

    public MeteorEntity(Level level, LivingEntity owner, float power) {
        this(ModEntities.METEOR.get(), level);
        this.setOwner(owner);
        this.power = power;
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        // нет синхронизированных данных
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.impacted) {
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                this.onHit(hitresult);
            }
        }
        if (!this.isAlive()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);

        if (this.level().isClientSide) {
            spawnTrail();
        }

        if (this.tickCount > 400) {
            detonate();
        }
    }

    private void spawnTrail() {
        RandomGenerator rnd = this.level().getRandom();
        double x = getX(), y = getY(), z = getZ();

        // Огненный хвост
        Vec3 back = this.getDeltaMovement().normalize().scale(-0.8);
        for (int i = 0; i < 4; i++) {
            double sx = x + back.x * rnd.nextDouble() + (rnd.nextDouble() - 0.5) * 1.2;
            double sy = y + back.y * rnd.nextDouble() + (rnd.nextDouble() - 0.5) * 1.2;
            double sz = z + back.z * rnd.nextDouble() + (rnd.nextDouble() - 0.5) * 1.2;
            this.level().addParticle(ParticleTypes.FLAME, sx, sy, sz, 0, 0.05, 0);
        }
        this.level().addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
        this.level().addParticle(ParticleTypes.LAVA, x, y, z, 0, 0, 0);
        for (int i = 0; i < 2; i++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            this.level().addParticle(ModParticles.spark(1.0F, 0.6F + rnd.nextFloat() * 0.3F, 0.2F, 1.0F),
                    x + Math.cos(a) * 1.4, y + (rnd.nextDouble() - 0.5) * 1.4, z + Math.sin(a) * 1.4, 0, 0, 0);
        }
        if (this.tickCount % 4 == 0) {
            this.level().addParticle(ModParticles.ring(1.0F, 0.5F, 0.15F, 1.6F), x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void onHit(HitResult result) {
        if (this.impacted) return;
        if (!this.level().isClientSide) {
            detonate();
        }
    }

    private void detonate() {
        this.impacted = true;
        if (this.level() instanceof ServerLevel server) {
            float explosionPower = 5.5F * (power / 5.0F);
            server.explode(this.getOwner(), getX(), getY(), getZ(), explosionPower, true, Level.ExplosionInteraction.TNT);

            // Эпичные расходящиеся кольца + сфера искр
            for (int ring = 0; ring < 3; ring++) {
                ParticleHelper.ring(server, position(),
                        ModParticles.ring(1.0F, 0.65F - ring * 0.15F, 0.15F, 1.6F + ring * 0.9F),
                        24, 1.5 + ring * 1.6, 0.3);
            }
            ParticleHelper.burst(server, position(), ModParticles.spark(1.0F, 0.55F, 0.1F, 1.1F), 60, 0.8, server.random);
            ParticleHelper.sphere(server, position(), ModParticles.rune(1.0F, 0.6F, 0.1F, 1.3F), 20, 2.5, server.random);
            server.sendParticles(ParticleTypes.LAVA, getX(), getY(), getZ(), 24, 2.0, 1.5, 2.0, 0.1);
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 60, 1.8, 1.2, 1.8, 0.12);
            server.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), 8, 1.5, 0.8, 1.5, 0.05);

            // Тряска камеры у всех в радиусе
            ModNetwork.sendFx(server, getX(), getY(), getZ(), ModNetwork.FX_METEOR, 64.0D);

            server.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 0.7F);
            server.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.PLAYERS, 1.6F, 0.4F);
        }
        this.discard();
    }

    public float power() {
        return this.power;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        // Метеор пролетает сквозь мелочь, взрывается об блоки и крупных существ
        return super.canHitEntity(entity) && entity.getBoundingBox().getSize() >= 0.5F;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.power = tag.getFloat("MeteorPower");
        this.impacted = tag.getBoolean("MeteorImpacted");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("MeteorPower", this.power);
        tag.putBoolean("MeteorImpacted", this.impacted);
    }
}
