package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.server.job.JobAdvanceService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class RequestAdvancementPacket {
    private final String targetJobId;

    public RequestAdvancementPacket(String targetJobId) {
        this.targetJobId = targetJobId;
    }

    public RequestAdvancementPacket(FriendlyByteBuf buf) {
        this.targetJobId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(targetJobId, 64);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> JobAdvanceService.tryAdvance(player, targetJobId));
        }
        ctx.setPacketHandled(true);
    }
}

