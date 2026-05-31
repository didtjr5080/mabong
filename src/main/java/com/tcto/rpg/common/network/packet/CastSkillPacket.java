package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.server.skill.SkillCastService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Supplier;

public class CastSkillPacket implements CustomPacketPayload {
    public static final Type<CastSkillPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath("tctorpg", "cast_skill"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastSkillPacket> STREAM_CODEC =
        StreamCodec.ofMember((packet, buf) -> packet.encode(buf), CastSkillPacket::new);

    private final int slotIndex;

    public CastSkillPacket(int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public CastSkillPacket(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> SkillCastService.tryCast(player, slotIndex));
        }
        ctx.setPacketHandled(true);
    }

    public static void handle(CastSkillPacket packet, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ctx.enqueueWork(() -> SkillCastService.tryCast(player, packet.slotIndex));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

