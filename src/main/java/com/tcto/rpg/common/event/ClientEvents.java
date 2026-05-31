package com.tcto.rpg.common.event;

import com.tcto.rpg.client.hud.CustomChatHud;
import com.tcto.rpg.client.hud.ClientRpgState;
import com.tcto.rpg.client.hud.RpgHudOverlay;
import com.tcto.rpg.client.keybind.ClientKeybinds;
import com.tcto.rpg.client.screen.ScreenDispatcher;
import com.tcto.rpg.common.config.ClientUiConfig;
import com.tcto.rpg.common.network.ModNetwork;
import com.tcto.rpg.common.network.packet.CastSkillPacket;
import com.tcto.rpg.common.network.packet.OpenScreenRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.Set;

public class ClientEvents {
    private static final Set<net.minecraft.resources.ResourceLocation> HIDDEN_VANILLA_LAYERS = Set.of(
        VanillaGuiLayers.PLAYER_HEALTH,
        VanillaGuiLayers.FOOD_LEVEL,
        VanillaGuiLayers.ARMOR_LEVEL,
        VanillaGuiLayers.EXPERIENCE_BAR,
        VanillaGuiLayers.EXPERIENCE_LEVEL,
        VanillaGuiLayers.AIR_LEVEL,
        VanillaGuiLayers.VEHICLE_HEALTH
    );

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        ClientRpgState.tickClient();
        CustomChatHud.tick();
        ClientKeybinds.consumeCastSlots().forEach(slotIndex -> {
            if (slotIndex == 5) {
                Minecraft.getInstance().player.displayClientMessage(Component.literal("[TCToRPG] 궁극기 사용 요청"), true);
            }
            ModNetwork.CHANNEL.sendToServer(new CastSkillPacket(slotIndex));
        });
        ClientKeybinds.consumeScreenRequests().forEach(screenId -> {
            ModNetwork.CHANNEL.sendToServer(new OpenScreenRequestPacket(screenId));
            ScreenDispatcher.open(screenId);
        });
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        RpgHudOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (HIDDEN_VANILLA_LAYERS.contains(event.getName())) {
            event.setCanceled(true);
        }
        if (ClientUiConfig.HIDE_VANILLA_CHAT.get() && VanillaGuiLayers.CHAT.equals(event.getName())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onClientChatReceived(ClientChatReceivedEvent event) {
        CustomChatHud.add(event.getMessage());
    }
}

