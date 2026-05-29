package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncEffectPacket {
    private final List<String> effects;

    public SyncEffectPacket(List<String> effects) {
        this.effects = effects;
    }

    public SyncEffectPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.effects = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            effects.add(buf.readUtf(64));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(effects.size());
        for (String effect : effects) {
            buf.writeUtf(effect, 64);
        }
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.updateStatusEffects(effects));
        ctx.setPacketHandled(true);
    }
}

