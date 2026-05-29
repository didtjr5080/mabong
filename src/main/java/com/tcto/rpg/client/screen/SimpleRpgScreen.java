package com.tcto.rpg.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SimpleRpgScreen extends Screen {
    private final String screenId;

    public SimpleRpgScreen(String screenId) {
        super(Component.literal("TCToRPG"));
        this.screenId = screenId;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(font, "Screen: " + screenId, width / 2, height / 2, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

