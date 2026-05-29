package com.tcto.rpg.client.hud;

import com.tcto.rpg.TCToRPG;
import com.tcto.rpg.common.config.ClientUiConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public final class ResourceBarRenderer {
    private ResourceBarRenderer() {
    }

    public static void render(GuiGraphics guiGraphics) {
        int x = ClientUiConfig.SKILL_HUD_X.get();
        int y = ClientUiConfig.SKILL_HUD_Y.get();
        drawBar(guiGraphics, x, y, HudLayoutConfig.RESOURCE_WIDTH, HudLayoutConfig.RESOURCE_HEIGHT,
            ClientRpgState.hp(), ClientRpgState.maxHp(),
            texture("hud/hp_bar_bg.png"), texture("hud/hp_bar_fill.png"), "HP");
        drawBar(guiGraphics, x, y + HudLayoutConfig.RESOURCE_HEIGHT + HudLayoutConfig.RESOURCE_GAP,
            HudLayoutConfig.RESOURCE_WIDTH, HudLayoutConfig.RESOURCE_HEIGHT,
            ClientRpgState.mp(), ClientRpgState.maxMp(),
            texture("hud/mp_bar_bg.png"), texture("hud/mp_bar_fill.png"), "MP");
        drawBar(guiGraphics, x, y + (HudLayoutConfig.RESOURCE_HEIGHT + HudLayoutConfig.RESOURCE_GAP) * 2,
            HudLayoutConfig.RESOURCE_WIDTH, HudLayoutConfig.RESOURCE_HEIGHT,
            ClientRpgState.stamina(), ClientRpgState.maxStamina(),
            texture("hud/stamina_bar_bg.png"), texture("hud/stamina_bar_fill.png"), "ST");
    }

    private static void drawBar(GuiGraphics guiGraphics, int x, int y, int width, int height,
                                int value, int max, ResourceLocation bg, ResourceLocation fill, String label) {
        int filled = max > 0 ? (int) Math.round(width * (value / (double) max)) : 0;
        guiGraphics.blit(bg, x, y, 0, 0, width, height, 256, 256);
        if (filled > 0) {
            guiGraphics.blit(fill, x, y, 0, 0, filled, height, 256, 256);
        }
        guiGraphics.drawString(net.minecraft.client.Minecraft.getInstance().font, label + " " + value + "/" + max, x, y - 9, 0xFFFFFF, true);
    }

    private static ResourceLocation texture(String path) {
        return ResourceLocation.fromNamespaceAndPath(TCToRPG.MODID, "textures/gui/" + path);
    }
}

