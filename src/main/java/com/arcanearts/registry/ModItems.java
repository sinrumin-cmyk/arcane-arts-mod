package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import com.arcanearts.item.SpellTomeItem;
import com.arcanearts.item.WandItem;
import com.arcanearts.item.WandTier;
import com.arcanearts.spell.Spell;
import com.arcanearts.spell.SpellRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModItems {
    private ModItems() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ArcaneArts.MODID);

    // ==================== Посохи ====================
    public static final DeferredItem<Item> APPRENTICE_WAND = ITEMS.registerItem("apprentice_wand",
            props -> new WandItem(WandTier.APPRENTICE, props));
    public static final DeferredItem<Item> MYSTIC_WAND = ITEMS.registerItem("mystic_wand",
            props -> new WandItem(WandTier.MYSTIC, props));
    public static final DeferredItem<Item> ARCHMAGE_WAND = ITEMS.registerItem("archmage_wand",
            props -> new WandItem(WandTier.ARCHMAGE, props));

    // ==================== Материалы ====================
    public static final DeferredItem<Item> MANA_DUST = ITEMS.registerItem("mana_dust", Item::new);
    public static final DeferredItem<Item> MANA_CRYSTAL = ITEMS.registerItem("mana_crystal",
            props -> new Item(props) {
                @Override
                public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.arcanearts.mana_crystal").withStyle(ChatFormatting.AQUA));
                    super.appendHoverText(stack, ctx, tooltip, flag);
                }
            });

    // ==================== Тома (spell path = имя тома без "_tome") ====================
    public static final DeferredItem<Item> FIREBALL_TOME   = tome("fireball_tome");
    public static final DeferredItem<Item> LIGHTNING_TOME  = tome("lightning_tome");
    public static final DeferredItem<Item> FROST_TOME      = tome("frost_tome");
    public static final DeferredItem<Item> METEOR_TOME     = tome("meteor_tome");
    public static final DeferredItem<Item> SINGULARITY_TOME = tome("singularity_tome");
    public static final DeferredItem<Item> BLINK_TOME     = tome("blink_tome");
    public static final DeferredItem<Item> SHIELD_TOME    = tome("shield_tome");
    public static final DeferredItem<Item> HEAL_TOME      = tome("heal_tome");
    public static final DeferredItem<Item> HASTE_TOME     = tome("haste_tome");
    public static final DeferredItem<Item> FLIGHT_TOME    = tome("flight_tome");

    private static DeferredItem<Item> tome(String tomeName) {
        String spellPath = tomeName.substring(0, tomeName.length() - "_tome".length());
        return ITEMS.registerItem(tomeName, props -> new SpellTomeItem(() -> SpellRegistry.get(ArcaneArts.id(spellPath)), props));
    }

    // ==================== Блоки (BlockItem) ====================
    public static void registerBlockItems() {
        ITEMS.registerSimpleBlockItem(ModBlocks.ARCANE_ALTAR);
        ITEMS.registerSimpleBlockItem(ModBlocks.MANA_ORE);
        ITEMS.registerSimpleBlockItem(ModBlocks.MANA_CRYSTAL_BLOCK);
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
