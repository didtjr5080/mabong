package com.tcto.rpg.common.network;

import com.tcto.rpg.common.network.packet.CastSkillPacket;
import com.tcto.rpg.common.network.packet.SyncCooldownPacket;
import com.tcto.rpg.common.network.packet.SyncPlayerRpgDataPacket;
import com.tcto.rpg.common.network.packet.SyncStatusEffectsPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    public static final CompatibilityChannel CHANNEL = new CompatibilityChannel();

    private ModNetwork() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(ModNetwork::registerPayloads);
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToServer(CastSkillPacket.TYPE, CastSkillPacket.STREAM_CODEC, CastSkillPacket::handle);
        registrar.playToClient(SyncPlayerRpgDataPacket.TYPE, SyncPlayerRpgDataPacket.STREAM_CODEC, SyncPlayerRpgDataPacket::handle);
        registrar.playToClient(SyncCooldownPacket.TYPE, SyncCooldownPacket.STREAM_CODEC, SyncCooldownPacket::handle);
        registrar.playToClient(SyncStatusEffectsPacket.TYPE, SyncStatusEffectsPacket.STREAM_CODEC, SyncStatusEffectsPacket::handle);
    }

    public static final class CompatibilityChannel {
        public void sendToServer(Object packet) {
            if (packet instanceof CustomPacketPayload payload) {
                PacketDistributor.sendToServer(payload);
            }
        }

        public void sendToPlayer(ServerPlayer player, Object packet) {
            if (packet instanceof CustomPacketPayload payload) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }
}

