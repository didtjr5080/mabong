package com.tcto.rpg.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class OpenNpcInteractionPacket {
    private final int npcId;

    public OpenNpcInteractionPacket(int npcId) {
        this.npcId = npcId;
    }

    public OpenNpcInteractionPacket(FriendlyByteBuf buf) {
        this.npcId = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(npcId);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                // TODO: server-side NPC validation and dialogue open.
            });
        }
        ctx.setPacketHandled(true);
    }
}

