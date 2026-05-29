package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.common.network.ModNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.Set;
import java.util.function.Supplier;

public class OpenScreenRequestPacket {
    private static final Set<String> ALLOWED = Set.of(
        "character",
        "stats",
        "skills",
        "equipment",
        "job",
        "quest",
        "shop",
        "npc",
        "admin"
    );

    private final String screenId;

    public OpenScreenRequestPacket(String screenId) {
        this.screenId = screenId;
    }

    public OpenScreenRequestPacket(FriendlyByteBuf buf) {
        this.screenId = buf.readUtf(32);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(screenId, 32);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null && ALLOWED.contains(screenId)) {
            ctx.enqueueWork(() -> ModNetwork.CHANNEL.sendToPlayer(player, new OpenRpgScreenPacket(screenId)));
        }
        ctx.setPacketHandled(true);
    }
}

