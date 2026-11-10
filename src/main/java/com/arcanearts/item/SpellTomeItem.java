package com.arcanearts.item;

import com.arcanearts.spell.Spell;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Supplier;

/**
 * Том заклинания: изучается на Алтаре Арканы (ПКМ с томом в руке).
 */
public class SpellTomeItem extends Item {

    private final Supplier<Spell> spell;

    public SpellTomeItem(Supplier<Spell> spell, Item.Properties props) {
        super(props);
        this.spell = spell;
    }

    public Spell spell() {
        return spell.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        Spell s = spell();
        if (s != null) {
            tooltip.add(Component.translatable("tooltip.arcanearts.tome", s.name())
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(s.description().copy().withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.arcanearts.tome_cost", s.manaCost())
                    .withStyle(ChatFormatting.BLUE));
        }
        tooltip.add(Component.translatable("tooltip.arcanearts.tome_hint").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, ctx, tooltip, flag);
    }
}
