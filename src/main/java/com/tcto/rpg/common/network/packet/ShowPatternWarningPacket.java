package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class ShowPatternWarningPacket {
    private final String text;
    private final int durationTicks;

    public ShowPatternWarningPacket(String text, int durationTicks) {
        this.text = text;
        this.durationTicks = durationTicks;
    }

    public ShowPatternWarningPacket(FriendlyByteBuf buf) {
        this.text = buf.readUtf(128);
        this.durationTicks = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(text, 128);
        buf.writeVarInt(durationTicks);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.updatePatternWarning(text, durationTicks));
        ctx.setPacketHandled(true);
    }
}

