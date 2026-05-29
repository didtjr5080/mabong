package com.tcto.rpg.client.screen;

import com.tcto.rpg.TCToRPG;
import net.minecraft.client.gui.GuiGraphics;
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

