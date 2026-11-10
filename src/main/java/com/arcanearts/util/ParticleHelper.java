package com.arcanearts.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.random.RandomGenerator;

/**
 * Хелперы для рисования фигур из частиц на серверной стороне.
 */
public final class ParticleHelper {
    private ParticleHelper() {}

    /** Кольцо частиц в горизонтальной плоскости */
    public static void ring(ServerLevel level, Vec3 center, ParticleOptions options, int points, double radius, double yOffset) {
        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2 * i) / points;
            double px = center.x + Math.cos(angle) * radius;
            double py = center.y + yOffset;
            double pz = center.z + Math.sin(angle) * radius;
            level.sendParticles(options, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    /** Вертикальная спираль (столб) */
    public static void spiral(ServerLevel level, Vec3 center, ParticleOptions options, int points, double radius, double height, double turns) {
        for (int i = 0; i < points; i++) {
            double t = (double) i / points;
            double angle = Math.PI * 2 * turns * t;
            double px = center.x + Math.cos(angle) * radius;
            double pz = center.z + Math.sin(angle) * radius;
            double py = center.y + t * height;
            level.sendParticles(options, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    /** Сфера частиц вокруг точки */
    public static void sphere(ServerLevel level, Vec3 center, ParticleOptions options, int points, double radius, RandomGenerator random) {
        for (int i = 0; i < points; i++) {
            double phi = Math.acos(2.0 * random.nextDouble() - 1.0);
            double theta = Math.PI * 2 * random.nextDouble();
            double px = center.x + radius * Math.sin(phi) * Math.cos(theta);
            double py = center.y + radius * Math.sin(phi) * Math.sin(theta);
            double pz = center.z + radius * Math.cos(phi);
            level.sendParticles(options, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    /** Линия частиц между двумя точками */
    public static void line(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions options, int points) {
        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            double px = from.x + (to.x - from.x) * t;
            double py = from.y + (to.y - from.y) * t;
            double pz = from.z + (to.z - from.z) * t;
            level.sendParticles(options, px, py, pz, 1, 0, 0, 0, 0);
        }
    }

    /** Вспышка из точки в случайных направлениях */
    public static void burst(ServerLevel level, Vec3 center, ParticleOptions options, int count, double speed, RandomGenerator random) {
        for (int i = 0; i < count; i++) {
            double dx = random.nextDouble() * 2 - 1;
            double dy = random.nextDouble() * 2 - 1;
            double dz = random.nextDouble() * 2 - 1;
            level.sendParticles(options, center.x, center.y, center.z, 1, dx * speed, dy * speed, dz * speed, 0.02);
        }
    }
}
