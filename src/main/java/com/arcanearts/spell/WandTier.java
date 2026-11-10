package com.arcanearts.spell;

import net.minecraft.resources.ResourceLocation;

/**
 * Тир посоха: влияет на силу, стоимость и перезарядку заклинаний.
 */
public enum WandTier {
    /** Посох ученика */
    APPRENTICE(1.00F, 1.00F, 1.00F, 0x9A5CFF),
    /** Посох мистика */
    MYSTIC(1.35F, 0.85F, 0.70F, 0x37E8FF),
    /** Посох архимага */
    ARCHMAGE(1.80F, 0.70F, 0.45F, 0xFFD75E);

    private final float power;
    private final float costMultiplier;
    private final float cooldownMultiplier;
    private final int boltColor;

    WandTier(float power, float costMultiplier, float cooldownMultiplier, int boltColor) {
        this.power = power;
        this.costMultiplier = costMultiplier;
        this.cooldownMultiplier = cooldownMultiplier;
        this.boltColor = boltColor;
    }

    /** Множитель урона/силы */
    public float power() { return power; }

    /** Множитель стоимости маны */
    public float costMultiplier() { return costMultiplier; }

    /** Множитель перезарядки */
    public float cooldownMultiplier() { return cooldownMultiplier; }

    /** Цвет болтов посоха по умолчанию (ARGB) */
    public int boltColor() { return boltColor; }

    public String tierNameKey() { return "tier.arcanearts." + name().toLowerCase(); }
}
