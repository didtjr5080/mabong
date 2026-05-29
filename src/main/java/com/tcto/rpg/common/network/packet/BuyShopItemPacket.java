package com.tcto.rpg.common.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.tcto.rpg.common.network.NetworkContext;

import java.util.function.Supplier;

public class BuyShopItemPacket {
    private final String shopId;
    private final String itemId;
    private final int amount;

    public BuyShopItemPacket(String shopId, String itemId, int amount) {
        this.shopId = shopId;
        this.itemId = itemId;
        this.amount = amount;
    }

    public BuyShopItemPacket(FriendlyByteBuf buf) {
        this.shopId = buf.readUtf(64);
        this.itemId = buf.readUtf(64);
        this.amount = buf.readVarInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(shopId, 64);
        buf.writeUtf(itemId, 64);
        buf.writeVarInt(amount);
    }

    public void handle(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player != null) {
            ctx.enqueueWork(() -> {
                // TODO: server-side shop validation and purchase.
            });
        }
        ctx.setPacketHandled(true);
    }
}

