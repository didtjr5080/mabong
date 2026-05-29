package com.tcto.rpg.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;

public final class ModNetwork {
    public static final CompatibilityChannel CHANNEL = new CompatibilityChannel();

    private ModNetwork() {
    }

    public static void register(IEventBus modBus) {
    }

    public static final class CompatibilityChannel {
        public void sendToServer(Object packet) {
        }

        public void sendToPlayer(ServerPlayer player, Object packet) {
        }
    }
}

