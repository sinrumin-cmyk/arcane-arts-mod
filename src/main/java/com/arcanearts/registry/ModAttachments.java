package com.arcanearts.registry;

import com.arcanearts.ArcaneArts;
import com.arcanearts.mana.PlayerMana;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    private ModAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ArcaneArts.MODID);

    /** Мана игрока: runtime данные, сериализуются сериализатором мода */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerMana>> PLAYER_MANA =
            ATTACHMENTS.register("player_mana", () -> AttachmentType.builder(PlayerMana::new)
                    .persistent(PlayerMana.CODEC)
                    .copyOnDeath()
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}
