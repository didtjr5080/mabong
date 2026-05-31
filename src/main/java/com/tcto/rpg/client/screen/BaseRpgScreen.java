package com.tcto.rpg.client.screen;

import com.tcto.rpg.TCToRPG;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class BaseRpgScreen extends Screen {
    private final String title;
    private final ResourceLocation backgroundTexture;
    private final int imageWidth;
    private final int imageHeight;

    protected BaseRpgScreen(String title) {
        this(title, null, 0, 0);
    }

    protected BaseRpgScreen(String title, String backgroundTexture, int imageWidth, int imageHeight) {
        super(Component.literal(title));
        this.title = title;
        this.backgroundTexture = backgroundTexture == null
            ? null
            : ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/screens/" + backgroundTexture);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    protected int panelX() {
        return imageWidth > 0 ? (width - imageWidth) / 2 : 20;
    }

    protected int panelY() {
        return imageHeight > 0 ? (height - imageHeight) / 2 : 20;
    }

    protected Button commandButton(String label, String command, int x, int y, int buttonWidth) {
        return Button.builder(Component.literal(label), button -> runCommand(command)).bounds(x, y, buttonWidth, 20).build();
    }

    protected Button closeButton(String label, int x, int y, int buttonWidth) {
        return Button.builder(Component.literal(label), button -> minecraft.setScreen(null)).bounds(x, y, buttonWidth, 20).build();
    }

    protected void runCommand(String command) {
        if (minecraft == null || minecraft.player == null || minecraft.player.connection == null) {
            return;
        }
        String normalized = command.startsWith("/") ? command.substring(1) : command;
        minecraft.player.displayClientMessage(Component.literal("[TCToRPG UI] /" + normalized), false);
        minecraft.player.connection.sendCommand(normalized);
    }

    protected String currentPlayerName() {
        if (minecraft == null || minecraft.player == null) {
            return "stone_0401";
        }
        return minecraft.player.getGameProfile().getName();
    }

    protected void drawLabel(GuiGraphics guiGraphics, String text, int x, int y, int color) {
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    protected void drawSlot(GuiGraphics guiGraphics, int x, int y, int size, String label, String value) {
        guiGraphics.fill(x, y, x + size, y + size, 0xAA050505);
        guiGraphics.hLine(x, x + size, y, 0xFF8B8069);
        guiGraphics.hLine(x, x + size, y + size, 0xFF4A4337);
        guiGraphics.vLine(x, y, y + size, 0xFF8B8069);
        guiGraphics.vLine(x + size, y, y + size, 0xFF4A4337);
        guiGraphics.drawCenteredString(font, label, x + size / 2, y + 4, 0xD8D1B8);
        if (value != null && !value.isBlank()) {
            guiGraphics.drawCenteredString(font, value, x + size / 2, y + size - 12, 0xFFFFFF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        int titleY = 20;
        if (backgroundTexture != null) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            guiGraphics.blit(backgroundTexture, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
            titleY = y + 12;
        }
        guiGraphics.drawCenteredString(font, title, width / 2, titleY, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}

