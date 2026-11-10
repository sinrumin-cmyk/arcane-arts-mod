package com.arcanearts.spell;

import com.arcanearts.item.WandItem;
import com.arcanearts.mana.PlayerMana;
import com.arcanearts.network.CooldownsPacket;
import com.arcanearts.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Серверная логика каста: проверки маны, перезарядки и выученности.
 */
public final class SpellManager {
    private SpellManager() {}

    /** player UUID -> spell id -> конец перезарядки (gameTime) */
    private static final Map<UUID, Map<ResourceLocation, Long>> COOLDOWNS = new HashMap<>();

    public static void cast(ServerPlayer player, ServerLevel level, ItemStack wand, Spell spell) {
        if (spell == null) return;
        if (!(wand.getItem() instanceof WandItem wandItem)) return;

        if (!WandItem.knows(wand, spell)) {
            player.displayClientMessage(
                    Component.translatable("message.arcanearts.not_learned").withStyle(ChatFormatting.RED), true);
            return;
        }

        long now = level.getGameTime();
        Map<ResourceLocation, Long> cds = cooldowns(player);
        Long until = cds.get(spell.id());
        if (until != null && until > now) {
            player.displayClientMessage(
                    Component.translatable("message.arcanearts.cooldown",
                            Component.literal(String.format("%.1f", (until - now) / 20.0)))
                            .withStyle(ChatFormatting.GOLD), true);
            return;
        }

        PlayerMana mana = PlayerMana.get(player);
        int cost = Math.max(1, Math.round(spell.manaCost() * wandItem.tier().costMultiplier()));
        if (mana.mana() < cost) {
            player.displayClientMessage(
                    Component.translatable("message.arcanearts.no_mana", cost).withStyle(ChatFormatting.RED), true);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.5F);
            return;
        }

        boolean success = spell.cast(player, level, wandItem.tier());
        if (success) {
            mana.tryConsume(cost);
            int cd = Math.max(1, Math.round(spell.cooldownTicks() * wandItem.tier().cooldownMultiplier()));
            cds.put(spell.id(), now + cd);
            // предметная перезарядка — посох темнеет на хотбаре
            player.getCooldowns().addCooldown(wand.getItem(), cd);
            // синхронизация перезарядок для HUD
            ModNetwork.sendTo(player, CooldownsPacket.active(now, cds));
        }
    }

    private static Map<ResourceLocation, Long> cooldowns(ServerPlayer player) {
        return COOLDOWNS.computeIfAbsent(player.getUUID(), u -> new HashMap<>());
    }

    public static void clear(UUID playerId) {
        COOLDOWNS.remove(playerId);
    }

    /** Все активные перезарядки игрока (для отправки при входе) */
    public static CooldownsPacket activeCooldownsPacket(ServerPlayer player, long now) {
        Map<ResourceLocation, Long> cds = COOLDOWNS.get(player.getUUID());
        return cds == null ? new CooldownsPacket(java.util.List.of()) : CooldownsPacket.active(now, cds);
    }
}
