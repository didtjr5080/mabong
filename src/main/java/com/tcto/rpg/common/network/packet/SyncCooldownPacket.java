package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class SyncCooldownPacket {
    private final int[] slots;
    private final int[] remainingTicks;

    public SyncCooldownPacket(int[] slots, int[] remainingTicks) {
        this.slots = slots;
        this.remainingTicks = remainingTicks;
    }

    public SyncCooldownPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.slots = new int[size];
        this.remainingTicks = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = buf.readVarInt();
            remainingTicks[i] = buf.readVarInt();
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(slots.length);
        for (int i = 0; i < slots.length; i++) {
            buf.writeVarInt(slots[i]);
            buf.writeVarInt(remainingTicks[i]);
        }
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.updateCooldowns(slots, remainingTicks));
        ctx.setPacketHandled(true);
    }
}

