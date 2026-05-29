package com.tcto.rpg.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public final class FloatingDamageRenderer {
    private FloatingDamageRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        List<String> entries = ClientRpgState.floatingDamageTexts();
        if (entries.isEmpty()) {
            return;
        }
        int centerX = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;
        int centerY = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 - 20;
        for (int i = 0; i < entries.size(); i++) {
            guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, entries.get(i), centerX, centerY - i * 10, 0xFFFF55);
        }
    }
}

