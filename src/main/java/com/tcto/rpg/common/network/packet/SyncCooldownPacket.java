package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class SyncCooldownPacket implements CustomPacketPayload {
    public static final Type<SyncCooldownPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("tctorpg", "sync_cooldown"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCooldownPacket> STREAM_CODEC =
        StreamCodec.ofMember((packet, buf) -> packet.encode(buf), SyncCooldownPacket::new);

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

    public static void handle(SyncCooldownPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientRpgState.updateCooldowns(packet.slots, packet.remainingTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

