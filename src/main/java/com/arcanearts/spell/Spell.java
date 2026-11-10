package com.arcanearts.spell;

import com.arcanearts.ArcaneArts;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Базовый класс заклинания.
 */
public abstract class Spell {
    protected final ResourceLocation id;
    protected final int manaCost;
    protected final int cooldownTicks;

    public Spell(ResourceLocation id, int manaCost, int cooldownTicks) {
        this.id = id;
        this.manaCost = manaCost;
        this.cooldownTicks = cooldownTicks;
    }

    /** Применить заклинание. @return true — успешно */
    public abstract boolean cast(ServerPlayer player, ServerLevel level, WandTier tier);

    public ResourceLocation id() { return id; }
    public int manaCost() { return manaCost; }
    public int cooldownTicks() { return cooldownTicks; }

    public String path() { return id.getPath(); }

    public Component name() {
        return Component.translatable("spell.arcanearts." + path());
    }

    public Component description() {
        return Component.translatable("spell.arcanearts." + path() + ".desc");
    }

    /** Текстура иконки: assets/arcanearts/textures/gui/spell/<path>.png */
    public ResourceLocation iconTexture() {
        return ArcaneArts.id("textures/gui/spell/" + path() + ".png");
    }
}
