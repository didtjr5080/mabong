package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.screen.ScreenDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class OpenQuestScreenPacket {
    private final String questId;

    public OpenQuestScreenPacket(String questId) {
        this.questId = questId;
    }

    public OpenQuestScreenPacket(FriendlyByteBuf buf) {
        this.questId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId, 64);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ScreenDispatcher.open("quest"));
        ctx.setPacketHandled(true);
    }
}

