package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.server.stat.StatAllocationService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class AllocateStatPacket {
    private final String statId;
    private final int amount;

    public AllocateStatPacket(String statId, int amount) {
        this.statId = statId;
        this.amount = amount;
    }

    public AllocateStatPacket(FriendlyByteBuf buf) {
        this.statId = buf.readUtf(16);
        this.amount = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(statId, 16);
        buf.writeInt(amount);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> StatAllocationService.tryAllocate(player, statId, amount));
        }
        ctx.setPacketHandled(true);
    }
}

