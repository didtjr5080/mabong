package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.server.skill.SkillEquipService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class EquipSkillPacket {
    private final int slotIndex;
    private final String skillId;

    public EquipSkillPacket(int slotIndex, String skillId) {
        this.slotIndex = slotIndex;
        this.skillId = skillId;
    }

    public EquipSkillPacket(FriendlyByteBuf buf) {
        this.slotIndex = buf.readInt();
        this.skillId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
        buf.writeUtf(skillId, 64);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> SkillEquipService.tryEquip(player, slotIndex, skillId));
        }
        ctx.setPacketHandled(true);
    }
}

