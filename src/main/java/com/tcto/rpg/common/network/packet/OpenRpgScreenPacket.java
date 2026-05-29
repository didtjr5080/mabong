package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.screen.ScreenDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class OpenRpgScreenPacket {
    private final String screenId;

    public OpenRpgScreenPacket(String screenId) {
        this.screenId = screenId;
    }

    public OpenRpgScreenPacket(FriendlyByteBuf buf) {
        this.screenId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(screenId, 64);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ScreenDispatcher.open(screenId);
            }
        });
        ctx.setPacketHandled(true);
    }
}

