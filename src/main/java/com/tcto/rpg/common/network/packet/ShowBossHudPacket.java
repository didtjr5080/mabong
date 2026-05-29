package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class ShowBossHudPacket {
    private final String bossName;
    private final int currentHp;
    private final int maxHp;

    public ShowBossHudPacket(String bossName, int currentHp, int maxHp) {
        this.bossName = bossName;
        this.currentHp = currentHp;
        this.maxHp = maxHp;
    }

    public ShowBossHudPacket(FriendlyByteBuf buf) {
        this.bossName = buf.readUtf(64);
        this.currentHp = buf.readVarInt();
        this.maxHp = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(bossName, 64);
        buf.writeVarInt(currentHp);
        buf.writeVarInt(maxHp);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.updateBossHud(bossName, currentHp, maxHp));
        ctx.setPacketHandled(true);
    }
}

