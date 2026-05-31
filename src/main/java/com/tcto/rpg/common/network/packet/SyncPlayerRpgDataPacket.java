package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import com.tcto.rpg.common.network.NetworkContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class SyncPlayerRpgDataPacket implements CustomPacketPayload {
    public static final Type<SyncPlayerRpgDataPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("tctorpg", "sync_player_rpg_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPlayerRpgDataPacket> STREAM_CODEC =
        StreamCodec.ofMember((packet, buf) -> packet.encode(buf), SyncPlayerRpgDataPacket::new);

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

    public static void handle(SyncPlayerRpgDataPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientRpgState.updateFromServer(packet.dataTag));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

