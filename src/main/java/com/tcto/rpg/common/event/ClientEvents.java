package com.tcto.rpg.common.event;

import com.tcto.rpg.client.hud.ClientRpgState;
import com.tcto.rpg.client.hud.RpgHudOverlay;
import com.tcto.rpg.client.keybind.ClientKeybinds;
import com.tcto.rpg.client.screen.ScreenDispatcher;
import com.tcto.rpg.common.network.ModNetwork;
import com.tcto.rpg.common.network.packet.CastSkillPacket;
import com.tcto.rpg.common.network.packet.OpenScreenRequestPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class ClientEvents {
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        ClientRpgState.tickClient();
        ClientKeybinds.consumeCastSlots().forEach(slotIndex ->
            ModNetwork.CHANNEL.sendToServer(new CastSkillPacket(slotIndex))
        );
        ClientKeybinds.consumeScreenRequests().forEach(screenId -> {
            ModNetwork.CHANNEL.sendToServer(new OpenScreenRequestPacket(screenId));
            ScreenDispatcher.open(screenId);
        });
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        RpgHudOverlay.render(event.getGuiGraphics());
    }
}

