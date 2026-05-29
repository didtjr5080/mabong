package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.server.skill.SkillCastService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class CastSkillPacket {
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
}

