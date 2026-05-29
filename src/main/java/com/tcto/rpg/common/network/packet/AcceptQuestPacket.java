package com.tcto.rpg.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class AcceptQuestPacket {
    private final String questId;

    public AcceptQuestPacket(String questId) {
        this.questId = questId;
    }

    public AcceptQuestPacket(FriendlyByteBuf buf) {
        this.questId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(questId, 64);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                // TODO: server-side quest accept validation.
            });
        }
        ctx.setPacketHandled(true);
    }
}

