package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class SyncPlayerRpgDataPacket {
    private final CompoundTag dataTag;

    public SyncPlayerRpgDataPacket(CompoundTag dataTag) {
        this.dataTag = dataTag;
    }

    public SyncPlayerRpgDataPacket(FriendlyByteBuf buf) {
        this.dataTag = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(dataTag);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.updateFromServer(dataTag));
        ctx.setPacketHandled(true);
    }
}

