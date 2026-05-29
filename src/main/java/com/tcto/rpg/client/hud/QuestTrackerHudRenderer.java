package com.tcto.rpg.client.hud;

import net.minecraft.client.gui.GuiGraphics;

public final class QuestTrackerHudRenderer {
    private QuestTrackerHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        int x = HudLayoutConfig.QUEST_X;
        int y = HudLayoutConfig.QUEST_Y;
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "Job: " + ClientRpgState.jobId(), x, y, 0xFFFFFF, true);
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "Level: " + ClientRpgState.level(), x, y + 10, 0xFFFFFF, true);
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, "Quest: (placeholder)", x, y + 20, 0xAAAAAA, true);
    }
}

