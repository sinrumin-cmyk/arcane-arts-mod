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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Сингулярность: чёрная дыра. 5 секунд затягивает всё вокруг,
 * затем имплодирует со взрывом тёмной энергии.
 */
public class SingularityEntity extends Projectile {

    public static final double PULL_RADIUS = 8.0D;
    public static final int LIFETIME_TICKS = 100;

    private float power = 3.0F;

    public SingularityEntity(EntityType<? extends SingularityEntity> type, Level level) {
        super(type, level);
    }

    public SingularityEntity(Level level, LivingEntity owner, float power) {
        this(ModEntities.SINGULARITY.get(), level);
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

        this.setDeltaMovement(Vec3.ZERO);

        if (this.level().isClientSide) {
            spawnVisuals();
        } else if (this.level() instanceof ServerLevel server) {
            pullEntities(server);

            // Периодический урон в ядре
            if (this.tickCount % 20 == 0) {
                List<LivingEntity> core = server.getEntitiesOfClass(LivingEntity.class,
                        getBoundingBox().inflate(3.0D),
                        e -> e != this.getOwner() && e.isAlive());
                for (LivingEntity e : core) {
                    var owner = this.getOwner();
                    e.hurt(this.damageSources().indirectMagic(this,
                            owner == null ? this : owner), 3.0F * (power / 3.0F));
                    e.fallDistance = 0;
                }
            }
        }

        if (this.tickCount >= LIFETIME_TICKS) {
            implode();
        }
    }

    private void pullEntities(ServerLevel server) {
        AABB box = getBoundingBox().inflate(PULL_RADIUS);
        List<Entity> list = server.getEntities(this, box,
                e -> e.isAlive() && e.isPushable() && !(e instanceof Projectile));

        Vec3 center = position().add(0, 0.5, 0);
        for (Entity e : list) {
            Vec3 toCenter = center.subtract(e.position().add(0, e.getBbHeight() * 0.5, 0));
            double dist = toCenter.length();
            if (dist < 0.4D || dist > PULL_RADIUS) continue;

            double strength = (1.0D - dist / PULL_RADIUS) * 0.14D;
            if (e instanceof Player) strength *= 0.5D; // игроков тянет слабее
            e.setDeltaMovement(e.getDeltaMovement()
                    .add(toCenter.normalize().scale(strength))
                    .add(0, 0.01D, 0)); // лёгкая компенсация гравитации
            e.fallDistance = Math.max(0, e.fallDistance - 1.5F);
        }
    }

    private void spawnVisuals() {
        RandomGenerator rnd = this.level().getRandom();
        double x = getX(), y = getY() + 0.5, z = getZ();

        // Аккреционный диск: частицы по спирали, втягивающиеся внутрь
        for (int i = 0; i < 3; i++) {
            double a = rnd.nextDouble() * Math.PI * 2;
            double r = 1.2 + rnd.nextDouble() * (PULL_RADIUS * 0.45);
            double px = x + Math.cos(a) * r;
            double pz = z + Math.sin(a) * r;
            double py = y + (rnd.nextDouble() - 0.5) * 1.5;
            Vec3 dir = new Vec3(x - px, y - py, z - pz).normalize().scale(0.35);
            this.level().addParticle(ModParticles.spark(0.55F, 0.25F, 0.85F, 0.5F), px, py, pz, dir.x, dir.y, dir.z);
        }
        if (this.tickCount % 2 == 0) {
            double a = (this.tickCount * 0.3D);
            this.level().addParticle(ModParticles.rune(0.45F, 0.15F, 0.7F, 1.0F),
                    x + Math.cos(a) * 1.6, y + Math.sin(a) * 0.6 + 0.4, z + Math.sin(a) * 1.6, 0, 0, 0);
            this.level().addParticle(ModParticles.rune(0.45F, 0.15F, 0.7F, 1.0F),
                    x + Math.cos(a + Math.PI) * 1.6, y - Math.sin(a) * 0.6 - 0.4, z + Math.sin(a + Math.PI) * 1.6, 0, 0, 0);
        }
        // Тёмное ядро
        this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        if (this.tickCount % 6 == 0) {
            this.level().addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0, 0.3, 0);
            this.level().addParticle(ParticleTypes.PORTAL, x, y + 0.5, z, 0, 0, 0);
        }
        // Гул нарастает
        if (this.tickCount % 30 == 0) {
            this.level().playLocalSound(x, y, z, SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.8F,
                    1.4F - (float) this.tickCount / LIFETIME_TICKS * 0.8F);
        }
    }

    private void implode() {
        if (this.level() instanceof ServerLevel server) {
            // Взрыв тёмной энергии
            var owner = this.getOwner();
            List<LivingEntity> hit = server.getEntitiesOfClass(LivingEntity.class,
                    getBoundingBox().inflate(4.5D),
                    e -> e != owner && e.isAlive());
            for (LivingEntity e : hit) {
                e.hurt(this.damageSources().indirectMagic(this,
                        owner == null ? this : owner), 8.0F * (power / 3.0F));
                Vec3 dir = position().subtract(e.position()).normalize();
                e.push(dir.x * 1.2, 0.6, dir.z * 1.2); // выброс из ядра
            }

            ParticleHelper.sphere(server, position().add(0, 0.5, 0),
                    ModParticles.spark(0.7F, 0.3F, 1.0F, 1.2F), 40, 2.5, server.random);
            ParticleHelper.ring(server, position(), ModParticles.ring(0.55F, 0.2F, 0.9F, 2.0F), 26, 3.5, 0.5);
            ParticleHelper.burst(server, position().add(0, 0.5, 0),
                    ModParticles.rune(0.4F, 0.1F, 0.6F, 1.4F), 16, 0.4, server.random);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 4, 0.6, 0.6, 0.6, 0);
            server.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 0.5, getZ(), 60, 1.0, 1.0, 1.0, 0.8);

            ModNetwork.sendFx(server, getX(), getY(), getZ(), ModNetwork.FX_SINGULARITY, 48.0D);

            server.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.6F, 1.7F);
            server.playSound(null, getX(), getY(), getZ(),
                    SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.7F, 0.5F);
        }
        this.discard();
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void onHit(HitResult result) {
        // Сингулярность не испытывает столкновений
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.power = tag.getFloat("HolePower");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("HolePower", this.power);
    }
}
