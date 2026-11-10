package com.arcanearts.item;

import com.arcanearts.registry.ModComponents;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.SpellManager;
import com.arcanearts.spell.SpellRegistry;
import com.arcanearts.spell.WandTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Волшебный посох: хранит выученные заклинания (data components),
 * ПКМ — каст выбранного, Shift+ПКМ — переключение.
 */
public class WandItem extends Item {

    private final WandTier tier;

    public WandItem(WandTier tier, Item.Properties props) {
        super(applyDefaults(props));
        this.tier = tier;
    }

    private static Item.Properties applyDefaults(Item.Properties props) {
        return props
                .component(ModComponents.LEARNED_SPELLS, List.of(SpellRegistry.ARCANE_BOLT.id()))
                .component(ModComponents.SELECTED_SPELL, SpellRegistry.ARCANE_BOLT.id());
    }

    public WandTier tier() {
        return tier;
    }

    // ==================== Компоненты ====================

    public static List<ResourceLocation> getLearned(ItemStack stack) {
        return stack.getOrDefault(ModComponents.LEARNED_SPELLS, List.of());
    }

    public static boolean knows(ItemStack stack, Spell spell) {
        return getLearned(stack).contains(spell.id());
    }

    public static void learn(ItemStack stack, Spell spell) {
        List<ResourceLocation> learned = new ArrayList<>(getLearned(stack));
        if (!learned.contains(spell.id())) {
            learned.add(spell.id());
            stack.set(ModComponents.LEARNED_SPELLS, List.copyOf(learned));
        }
    }

    public static ResourceLocation getSelectedId(ItemStack stack) {
        return stack.getOrDefault(ModComponents.SELECTED_SPELL, SpellRegistry.ARCANE_BOLT.id());
    }

    public static Spell getSelectedSpell(ItemStack stack) {
        return SpellRegistry.get(getSelectedId(stack));
    }

    public static void setSelected(ItemStack stack, Spell spell) {
        stack.set(ModComponents.SELECTED_SPELL, spell.id());
    }

    public static List<Spell> getLearnedSpells(ItemStack stack) {
        List<Spell> spells = new ArrayList<>();
        for (ResourceLocation id : getLearned(stack)) {
            Spell s = SpellRegistry.get(id);
            if (s != null) spells.add(s);
        }
        return spells;
    }

    // ==================== Использование ====================

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                Spell next = cycleSelected(stack);
                if (next != null) {
                    player.displayClientMessage(
                            Component.translatable("message.arcanearts.selected", next.name())
                                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6F, 1.6F);
                }
                return InteractionResult.SUCCESS;
            }
            Spell selected = getSelectedSpell(stack);
            if (selected != null) {
                SpellManager.cast(serverPlayer, server, stack, selected);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Переключить выбранное заклинание на следующее выученное */
    public static Spell cycleSelected(ItemStack stack) {
        List<Spell> learned = getLearnedSpells(stack);
        if (learned.isEmpty()) return null;
        Spell current = getSelectedSpell(stack);
        int idx = 0;
        if (current != null) {
            idx = learned.indexOf(current);
            idx = (idx + 1) % learned.size();
        }
        Spell next = learned.get(idx);
        setSelected(stack, next);
        return next;
    }

    // ==================== Подсказка ====================

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        Spell selected = getSelectedSpell(stack);
        if (selected != null) {
            tooltip.add(Component.translatable("tooltip.arcanearts.selected", selected.name())
                    .withStyle(ChatFormatting.GOLD));
        }
        tooltip.add(Component.translatable(tier.tierNameKey()).withStyle(ChatFormatting.AQUA));
        // Список выученных заклинаний
        List<Spell> learned = getLearnedSpells(stack);
        if (!learned.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.arcanearts.learned", learned.size())
                    .withStyle(ChatFormatting.GRAY));
            for (Spell s : learned) {
                String mark = s == selected ? "» " : "  ";
                tooltip.add(Component.literal(mark).append(s.name()).withStyle(ChatFormatting.DARK_PURPLE));
            }
        }
        tooltip.add(Component.translatable("tooltip.arcanearts.wand_hint").withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, ctx, tooltip, flag);
    }
}
