package com.arcanearts.mana;

import com.arcanearts.registry.ModAttachments;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.player.Player;

/**
 * Мана игрока. Хранится как attachment, сериализуется с игроком,
 * копируется при смерти. Синхронизация на клиент через ManaSyncPacket.
 */
public class PlayerMana {
    public static final int BASE_MAX_MANA = 100;

    public static final Codec<PlayerMana> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.INT.optionalFieldOf("mana", 0).forGetter(m -> m.mana),
            Codec.INT.optionalFieldOf("max_mana", BASE_MAX_MANA).forGetter(m -> m.maxMana),
            Codec.INT.optionalFieldOf("flight_ticks", 0).forGetter(m -> m.flightTicks)
    ).apply(inst, PlayerMana::fromCodec));

    private int mana;
    private int maxMana;
    private int flightTicks;
    private boolean dirty;

    public PlayerMana() {
        this.maxMana = BASE_MAX_MANA;
        this.mana = BASE_MAX_MANA;
    }

    private PlayerMana(int mana, int maxMana, int flightTicks) {
        this.mana = Math.min(mana, maxMana);
        this.maxMana = maxMana;
        this.flightTicks = flightTicks;
    }

    private static PlayerMana fromCodec(int mana, int maxMana, int flightTicks) {
        return new PlayerMana(mana, maxMana, flightTicks);
    }

    public static PlayerMana get(Player player) {
        return player.getData(ModAttachments.PLAYER_MANA);
    }

    public int mana() { return mana; }
    public int maxMana() { return maxMana; }
    public int flightTicks() { return flightTicks; }

    public void setMana(int value) {
        this.mana = Math.clamp(value, 0, maxMana);
        markDirty();
    }

    public void setMaxMana(int value) {
        this.maxMana = Math.max(1, value);
        this.mana = Math.min(this.mana, this.maxMana);
        markDirty();
    }

    public void addMaxMana(int bonus) {
        setMaxMana(this.maxMana + bonus);
    }

    public boolean tryConsume(int amount) {
        if (mana < amount) return false;
        mana -= amount;
        markDirty();
        return true;
    }

    public void regen(int amount) {
        if (mana < maxMana) {
            mana = Math.min(maxMana, mana + amount);
            markDirty();
        }
    }

    public void setFlightTicks(int ticks) {
        this.flightTicks = ticks;
        markDirty();
    }

    public void tickFlight() {
        if (flightTicks > 0) {
            flightTicks--;
            if (flightTicks % 20 == 0) markDirty();
        }
    }

    public boolean isDirty() { return dirty; }
    public void markDirty() { dirty = true; }
    public void clearDirty() { dirty = false; }
}
