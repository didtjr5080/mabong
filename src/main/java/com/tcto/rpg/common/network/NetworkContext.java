package com.tcto.rpg.common.network;

import net.minecraft.server.level.ServerPlayer;

public class NetworkContext {
    public ServerPlayer getSender() {
        return null;
    }

    public void enqueueWork(Runnable runnable) {
        runnable.run();
    }

    public void setPacketHandled(boolean packetHandled) {
    }
}
