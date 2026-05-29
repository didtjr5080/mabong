package com.tcto.rpg.common.network.packet;

import com.tcto.rpg.client.screen.ScreenDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class OpenShopScreenPacket {
    private final String shopId;

    public OpenShopScreenPacket(String shopId) {
        this.shopId = shopId;
    }

    public OpenShopScreenPacket(FriendlyByteBuf buf) {
        this.shopId = buf.readUtf(64);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(shopId, 64);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> ScreenDispatcher.open("shop"));
        ctx.setPacketHandled(true);
    }
}

