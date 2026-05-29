package com.tcto.rpg.client.hud;

import com.tcto.rpg.TCToRPG;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class BossHudRenderer {
    private BossHudRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        if (ClientRpgState.bossName().isEmpty() || ClientRpgState.bossMaxHp() <= 0) {
            return;
        }
        int x = HudLayoutConfig.BOSS_X;
        int y = HudLayoutConfig.BOSS_Y;
        int width = HudLayoutConfig.BOSS_WIDTH;
        int height = HudLayoutConfig.BOSS_HEIGHT;
        int current = ClientRpgState.bossHp();
        int max = ClientRpgState.bossMaxHp();
        int filled = max > 0 ? (int) Math.round(width * (current / (double) max)) : 0;

        guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, ClientRpgState.bossName(), x + width / 2, y - 10, 0xFFFFFF);
        guiGraphics.blit(texture("hud/boss_bar.png"), x, y, 0, 0, width, height, 256, 256);
        guiGraphics.fill(x, y, x + filled, y + height, 0x55FF0000);

        if (!ClientRpgState.patternWarning().isEmpty()) {
            guiGraphics.drawCenteredString(net.minecraft.client.Minecraft.getInstance().font, ClientRpgState.patternWarning(), x + width / 2, y + 14, 0xFFFF55);
        }
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/" + path);
    }
}

