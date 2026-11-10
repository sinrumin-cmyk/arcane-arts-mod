package com.arcanearts.spell;

import com.arcanearts.ArcaneArts;
import com.arcanearts.spell.spells.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Реестр всех заклинаний. Класс инициализируется при первом обращении.
 */
public final class SpellRegistry {
    private SpellRegistry() {}

    private static final Map<ResourceLocation, Spell> SPELLS = new LinkedHashMap<>();

    // ==================== Заклинания (id, стоимость маны, перезарядка в тиках) ====================
    public static final Spell ARCANE_BOLT   = register(new ArcaneBoltSpell());
    public static final Spell FIREBALL      = register(new FireballSpell());
    public static final Spell LIGHTNING     = register(new LightningSpell());
    public static final Spell FROST_NOVA    = register(new FrostNovaSpell());
    public static final Spell METEOR        = register(new MeteorSpell());
    public static final Spell SINGULARITY   = register(new SingularitySpell());
    public static final Spell BLINK         = register(new BlinkSpell());
    public static final Spell MANA_SHIELD   = register(new ManaShieldSpell());
    public static final Spell HEAL          = register(new HealSpell());
    public static final Spell HASTE         = register(new HasteSpell());
    public static final Spell FLIGHT        = register(new FlightSpell());

    private static Spell register(Spell spell) {
        SPELLS.put(spell.id(), spell);
        return spell;
    }

    public static Spell get(ResourceLocation id) {
        return SPELLS.get(id);
    }

    public static Spell byPath(String path) {
        return get(ArcaneArts.id(path));
    }

    public static Collection<Spell> all() {
        return SPELLS.values();
    }

    public static List<ResourceLocation> allIds() {
        return new ArrayList<>(SPELLS.keySet());
    }

    /** Точка прицела игрока (не дальше maxDist) */
    public static Vec3 aimVec(ServerPlayer player, double maxDist) {
        HitResult hit = player.pick(maxDist, 1.0F, false);
        if (hit.getType() == HitResult.Type.MISS) {
            return player.getEyePosition().add(player.getLookAngle().scale(maxDist));
        }
        return hit.getLocation();
    }
}
