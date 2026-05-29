package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class ShowFloatingDamagePacket {
    private final String text;
    private final int durationTicks;

    public ShowFloatingDamagePacket(String text, int durationTicks) {
        this.text = text;
        this.durationTicks = durationTicks;
    }

    public ShowFloatingDamagePacket(FriendlyByteBuf buf) {
        this.text = buf.readUtf(32);
        this.durationTicks = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(text, 32);
        buf.writeVarInt(durationTicks);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.addFloatingDamage(text, durationTicks));
        ctx.setPacketHandled(true);
    }
}

