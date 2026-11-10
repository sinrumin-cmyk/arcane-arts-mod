package com.arcanearts.entity;

import com.arcanearts.registry.ModEntities;
import com.arcanearts.registry.ModParticles;
import com.arcanearts.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Магический болт: быстро летит по прямой, светится и оставляет шлейф.
 * Может быть огненным (взрыв при попадании).
 */
public class MagicBoltEntity extends Projectile {

    private static final EntityDataAccessor<Integer> DATA_COLOR =
            SynchedEntityData.defineId(MagicBoltEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(MagicBoltEntity.class, EntityDataSerializers.INT); // 0 = арканный, 1 = огненный

    private float damage = 5.0F;
    private float explosionRadius = 0.0F;
    private boolean fire = false;

    public MagicBoltEntity(EntityType<? extends MagicBoltEntity> type, Level level) {
        super(type, level);
    }

    public MagicBoltEntity(Level level, LivingEntity owner, float damage, float explosionRadius, boolean fire, int color) {
        this(ModEntities.MAGIC_BOLT.get(), level);
        this.setOwner(owner);
        this.setPos(owner.getX(), owner.getEyeY() - 0.15D, owner.getZ());
        this.damage = damage;
        this.explosionRadius = explosionRadius;
        this.fire = fire;
        this.entityData.set(DATA_COLOR, color);
        this.entityData.set(DATA_VARIANT, fire ? 1 : 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_COLOR, 0x9A5CFF);
        builder.define(DATA_VARIANT, 0);
    }

    public int getColor() {
        return this.entityData.get(DATA_COLOR);
    }

    public boolean isFireVariant() {
        return this.entityData.get(DATA_VARIANT) == 1;
    }

    @Override
    public void tick() {
        super.tick();

        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
        }
        if (!this.isAlive()) {
            return;
        }

        Vec3 motion = this.getDeltaMovement();
        this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);

        // Шлейф (на клиенте — плавно и без сетевого трафика)
        if (this.level().isClientSide) {
            spawnTrail();
        }

        if (this.tickCount > 100) {
            this.discard();
        }
    }

    private void spawnTrail() {
        int c = getColor();
        float r = ((c >> 16) & 0xFF) / 255.0F;
        float g = ((c >> 8) & 0xFF) / 255.0F;
        float b = (c & 0xFF) / 255.0F;

        this.level().addParticle(ModParticles.spark(r, g, b, 0.5F), getX(), getY(), getZ(), 0, 0, 0);
        if (this.tickCount % 2 == 0) {
            this.level().addParticle(ModParticles.rune(r, g, b, 0.35F), getX(), getY(), getZ(), 0, 0.02, 0);
        }
        if (isFireVariant()) {
            this.level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0, 0, 0);
            if (this.tickCount % 3 == 0) {
                this.level().addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(), 0, 0, 0);
            }
            this.level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0.0, 0.01, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();
            DamageSource source = this.damageSources().indirectMagic(this, owner == null ? this : owner);
            target.hurt(source, this.damage);
            if (this.fire) {
                target.igniteForSeconds(5);
            }
        }
    }

    @Override
    public void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide && this.level() instanceof ServerLevel server) {
            if (this.explosionRadius > 0.0F) {
                server.explode(this.getOwner(), this.getX(), this.getY(), this.getZ(),
                        this.explosionRadius, this.fire, Level.ExplosionInteraction.TNT);
            }

            // Взрыв искр в точке попадания
            int c = getColor();
            float r = ((c >> 16) & 0xFF) / 255.0F;
            float g = ((c >> 8) & 0xFF) / 255.0F;
            float b = (c & 0xFF) / 255.0F;
            ParticleHelper.burst(server, position(), ModParticles.spark(r, g, b, 0.7F), 18, 0.3, server.random);
            ParticleHelper.burst(server, position(), ModParticles.rune(r, g, b, 0.8F), 6, 0.15, server.random);
            if (isFireVariant()) {
                server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 20, 0.25, 0.25, 0.25, 0.06);
                server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 10, 0.2, 0.2, 0.2, 0.04);
                server.playSound(null, getX(), getY(), getZ(),
                        SoundEvents.FIRE_IGNITE, SoundSource.PLAYERS, 0.9F, 0.9F);
            } else {
                server.playSound(null, getX(), getY(), getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.4F);
            }
            this.discard();
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("BoltDamage");
        this.explosionRadius = tag.getFloat("BoltExplosion");
        this.fire = tag.getBoolean("BoltFire");
        this.entityData.set(DATA_COLOR, tag.getInt("BoltColor"));
        this.entityData.set(DATA_VARIANT, this.fire ? 1 : 0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BoltDamage", this.damage);
        tag.putFloat("BoltExplosion", this.explosionRadius);
        tag.putBoolean("BoltFire", this.fire);
        tag.putInt("BoltColor", getColor());
    }
}
