package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.hud.ClientRpgState;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SyncStatusEffectsPacket implements CustomPacketPayload {
    public static final Type<SyncStatusEffectsPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("tctorpg", "sync_status_effects"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatusEffectsPacket> STREAM_CODEC =
        StreamCodec.ofMember((packet, buf) -> packet.encode(buf), SyncStatusEffectsPacket::new);

    private final List<String> effects;

    public SyncStatusEffectsPacket(List<String> effects) {
        this.effects = effects;
    }

    public SyncStatusEffectsPacket(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.effects = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            effects.add(buf.readUtf(64));
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(effects.size());
        for (String effect : effects) {
            buf.writeUtf(effect, 64);
        }
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ClientRpgState.updateStatusEffects(effects));
        ctx.setPacketHandled(true);
    }

    public static void handle(SyncStatusEffectsPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> ClientRpgState.updateStatusEffects(packet.effects));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

