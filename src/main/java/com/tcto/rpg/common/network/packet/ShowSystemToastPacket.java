package com.tcto.rpg.common.network.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class ShowSystemToastPacket {
    private final String message;

    public ShowSystemToastPacket(String message) {
        this.message = message;
    }

    public ShowSystemToastPacket(FriendlyByteBuf buf) {
        this.message = buf.readUtf(128);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(message, 128);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal(message), false);
            }
        });
        ctx.setPacketHandled(true);
    }
}

